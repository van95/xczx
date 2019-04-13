package com.xuecheng.rabbitmq;


import com.xuecheng.TestRabbitmqApplication;
import com.xuecheng.rabbitmq.config.RabbitmqConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = TestRabbitmqApplication.class)
@RunWith(SpringRunner.class)
public class ProducerTest {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void testProducer() {

        String msg = "send to email";
        for (int i = 0; i < 5; i++) {

            rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_TOPICS_INFORM, "inform.email", msg);
            System.out.println(msg);
        }




    }
}
