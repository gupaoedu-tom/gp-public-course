package com.gupaoedu.vip.mq.rabbit.javaapi.simple;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 消息生产者
 */
public class MyProducer {
    private final static String EXCHANGE_NAME = "GP_SIMPLE_EXCHANGE_FORM_JAVAAPI";

    public static void main(String[] args) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        // 连接IP
        factory.setHost("192.168.8.147");
        // 连接端口
        factory.setPort(5672);
        // 虚拟机
        factory.setVirtualHost("/");
        // 用户
        factory.setUsername("admin");

        factory.setPassword("123456");

        // 建立连接
        Connection conn = factory.newConnection();
        // 创建消息通道
        Channel channel = conn.createChannel();

        // 发送消息
        String msg = "Hello world, Rabbit MQ";

        // String exchange, String routingKey, BasicProperties props, byte[] body
        channel.basicPublish(EXCHANGE_NAME, "gupao.best", null, msg.getBytes());

        channel.close();
        conn.close();
    }
}

