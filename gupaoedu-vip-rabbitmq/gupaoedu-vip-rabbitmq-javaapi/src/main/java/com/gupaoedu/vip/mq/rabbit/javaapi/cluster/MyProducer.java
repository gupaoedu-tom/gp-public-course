package com.gupaoedu.vip.mq.rabbit.javaapi.cluster;

import com.rabbitmq.client.*;

/**
 * 消息生产者
 */
public class MyProducer {
    private final static String EXCHANGE_NAME = "GP_SIMPLE_EXCHANGE_FORM_JAVAAPI";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();

        // 设置集群中每个节点的链接地址和端口
        Address[] addresses = new Address[]{
                new Address("192.168.8.144", 5672),
                new Address("192.168.8.145", 5672),
                new Address("192.168.8.146", 5672)
        };
        // 虚拟机
        factory.setVirtualHost("/");
        // 用户
        factory.setUsername("admin");
        factory.setPassword("123456");

        // 建立连接
        Connection conn = factory.newConnection(addresses);
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

