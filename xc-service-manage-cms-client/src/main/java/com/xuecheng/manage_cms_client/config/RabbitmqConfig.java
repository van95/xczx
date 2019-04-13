package com.xuecheng.manage_cms_client.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {
    //队列bean的名称
    public static final String QUEUE_CMS_POSTPAGE = "queue_cms_postpage";
    //交换机的名称
    public static final String EX_ROUTING_CMS_POSTPAGE="ex_routing_cms_postpage";

    @Value("${xuecheng.mq.queue}")
    private String queue_cms_postpage_name;

    @Value("${xuecheng.mq.routingkey}")
    private String routingkey;

    @Bean(QUEUE_CMS_POSTPAGE)
    public Queue QUEUE_CMS_POSTPAGE(){
        return new Queue(queue_cms_postpage_name);
    }

    @Bean(EX_ROUTING_CMS_POSTPAGE)
    public Exchange EX_ROUTING_CMS_POSTPAGE(){
        return ExchangeBuilder.fanoutExchange(EX_ROUTING_CMS_POSTPAGE).durable(true).build();
    }

    @Bean
    public Binding bind(@Qualifier(QUEUE_CMS_POSTPAGE)Queue queue,@Qualifier(EX_ROUTING_CMS_POSTPAGE)Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(routingkey).noargs();
    }
}
