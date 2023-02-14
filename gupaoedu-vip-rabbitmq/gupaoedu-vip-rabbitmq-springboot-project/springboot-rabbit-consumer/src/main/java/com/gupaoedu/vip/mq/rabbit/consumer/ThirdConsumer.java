package com.gupaoedu.vip.mq.rabbit.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;


@Component
@PropertySource("classpath:gupaomq.properties")
@RabbitListener(queues = "${com.gupaoedu.thirdqueue}", containerFactory="rabbitListenerContainerFactory")
public class ThirdConsumer {
    @RabbitHandler
    public void process(String msg){
        System.out.println("Third Queue received msg : " + msg);
    }
}
