import com.xuecheng.TestRabbitmqApplication;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = TestRabbitmqApplication.class)
@RunWith(SpringRunner.class)
public class ConsumerTest {


    @Autowired
    private RabbitTemplate rabbitTemplate;



}
