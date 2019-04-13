import com.xuecheng.auth.UcenterAuthApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

@SpringBootTest(classes = UcenterAuthApplication.class)
@RunWith(SpringRunner.class)
public class AuthTest {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired


    @Test
    public void testBcrypt(){

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String xcWebApp = encoder.encode("XcWebApp");
        System.out.println(xcWebApp);
    }

    public void test1() throws UnsupportedEncodingException {
//        stringRedisTemplate.boundHashOps("123").put("1","2");
//        stringRedisTemplate.boundValueOps("123")
        String a = "XcWebApp:XcWebApp";
        byte[] encode = Base64Utils.encode(a.getBytes());
        String b = new String(encode);
        String c = Base64Utils.encodeToString(a.getBytes());
        String d = Base64Utils.encodeToString(a.getBytes("utf-8"));
        System.out.println(b);
    }
}
