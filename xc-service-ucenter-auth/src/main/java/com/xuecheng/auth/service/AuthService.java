package com.xuecheng.auth.service;


import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    RestTemplate restTemplate;


    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;

    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        // 获取token
        AuthToken authToken = getToken(username, password, clientId, clientSecret);
        if (authToken == null) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        // 存入redis
        boolean bool = saveToRedis(authToken);
        if (!bool) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }

        return authToken;
    }

    public ResponseResult logout() {
        // 删除redis


        return null;
    }

    private boolean saveToRedis(AuthToken token) {
        // 令牌名称
        String key = "user_token:" + token.getAccess_token();
        // 保存到令牌到redis

        stringRedisTemplate.boundValueOps(key).set(JSON.toJSONString(token), tokenValiditySeconds, TimeUnit.SECONDS);
        // 获取过期时间
        Long expire = stringRedisTemplate.getExpire(key);
        // 如果没过期则存入redis成功
        return expire > 0;
    }

    // 从redis中获取令牌,jwt完整令牌
    public String getJwtFromRedis(String token) {
        // 令牌名称
        String key = "user_token:" + token;
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value != null) {
            AuthToken authToken = JSON.parseObject(value, AuthToken.class);
            return authToken.getJwt_token();
        }
        return null;
    }

    private AuthToken getToken(String username, String password, String clientId, String clientSecret) {
        // 选中认证服务的地址
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        if (serviceInstance == null) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_AUTHSERVER_NOTFOUND);
        }
        String URL = serviceInstance.getUri().toString() + "/auth/oauth/token";
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        // 授权方式
        formData.add("grant_type", "password");
        // 账号
        formData.add("username", username);
        // 密码
        formData.add("password", password);
        // 定义头
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Authorization", httpbasic(clientId, clientSecret));

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(formData, header);

        // 指定 restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });
        // http请求spring security的申请令牌接口
        ResponseEntity<Map> exchange = restTemplate.exchange(URL, HttpMethod.POST, httpEntity, Map.class);
        Map<String, String> body = exchange.getBody();
        if (body == null || body.get("access_token") == null || body.get("refresh_token") == null || body.get("jti") == null) {
            if (body != null && body.get("error_description") != null) {
                if ("坏的凭证".equals(body.get("error_description"))) {
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                }
                if ("UserDetailsService returned null, which is an interface contract violation"
                        .equals(body.get("error_description"))) {
                    ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                }
            }
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        AuthToken authToken = new AuthToken();
        authToken.setAccess_token(body.get("jti"));
        authToken.setRefresh_token(body.get("refresh_token"));
        authToken.setJwt_token(body.get("access_token"));

        return authToken;


    }

    private String httpbasic(String clientId, String clientSecret) {
        // 将客户端id和客户端密码拼接，按“客户端id:客户端密码”
        String string = clientId + ":" + clientSecret;
        // 进行base64编码
        return "Basic " + Base64Utils.encodeToString(string.getBytes());
    }


    public boolean delToken(String token) {
        // 令牌名称
        String key = "user_token:" + token;
        stringRedisTemplate.delete(key);
        return true;
    }
}
