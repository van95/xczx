package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import com.xuecheng.govern.gateway.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


@Component
public class LoginFilter extends ZuulFilter {
    @Autowired
    AuthService authService;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1; // int值来定义过滤器的执行顺序，数值越小优先级越高
    }

    @Override
    public boolean shouldFilter() {
        return true; // 该过滤器要执行
    }

    @Override
    public Object run() throws ZuulException {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();

        // 没有cookie
        String tokenFromCookie = authService.getTokenFromCookie(request);
        if (tokenFromCookie == null) {
            auth_denied();
            return null;
        }
        // 没有jwt令牌
        String jwt = authService.getJwtFromHeader(request);
        if (jwt == null) {
            auth_denied();
            return null;
        }
        // redis过期
        long expire = authService.getExpire(tokenFromCookie);
        if (expire < 0) {
            auth_denied();
            return null;
        }

        return null;
    }

    private void auth_denied() {
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        HttpServletResponse response = currentContext.getResponse();
        String authorization = request.getHeader("Authorization");


        if (StringUtils.isEmpty(authorization)) {

            currentContext.setSendZuulResponse(false); // 拒绝访问
            currentContext.setResponseStatusCode(200); // 状态码
            ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
            String jsonString = JSON.toJSONString(responseResult);
            currentContext.setResponseBody(jsonString);
            response.setContentType("application/json;charset=utf-8");
        }
    }
}
