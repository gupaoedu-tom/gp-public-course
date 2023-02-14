package com.gupaoedu.vip.mq.rabbit.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gupaoedu.vip.mq.rabbit.entity.Merchant;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;


@Component
@PropertySource("classpath:gupaomq.properties")
public class RabbitSender {

    @Value("${com.gupaoedu.directexchange}")
    private String directExchange;

    @Value("${com.gupaoedu.topicexchange}")
    private String topicExchange;

    @Value("${com.gupaoedu.fanoutexchange}")
    private String fanoutExchange;

    @Value("${com.gupaoedu.directroutingkey}")
    private String directRoutingKey;

    @Value("${com.gupaoedu.topicroutingkey1}")
    private String topicRoutingKey1;

    @Value("${com.gupaoedu.topicroutingkey2}")
    private String topicRoutingKey2;


    // 自定义的模板，所有的消息都会转换成JSON发送
    @Autowired
    AmqpTemplate gupaoTemplate;

    public void send() throws JsonProcessingException {
        Merchant merchant =  new Merchant(1001,"a direct msg : 中原镖局","汉中省解放路266号");
        gupaoTemplate.convertAndSend(directExchange, directRoutingKey, merchant);

        gupaoTemplate.convertAndSend(topicExchange, topicRoutingKey1, "a topic msg : shanghai.gupao.teacher");
        gupaoTemplate.convertAndSend(topicExchange, topicRoutingKey2, "a topic msg : changsha.gupao.student");

        // 发送JSON字符串
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(merchant);
        System.out.println(json);
        gupaoTemplate.convertAndSend(fanoutExchange,"", json);
    }


}
