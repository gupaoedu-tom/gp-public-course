package com.gupaoedu.vip.mq.rabbit.javaapi.simple;

import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * 消息消费者
 */
public class MyConsumer {
    private final static String EXCHANGE_NAME = "GP_SIMPLE_EXCHANGE_FORM_JAVAAPI";
    private final static String QUEUE_NAME = "GP_SIMPLE_QUEUE_FORM_JAVAAPI";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();

        // 连接IP
        factory.setHost("192.168.8.147");

        // 默认监听端口
        factory.setPort(5672);
        // 虚拟机
        factory.setVirtualHost("/");

        // 设置访问的用户
        factory.setUsername("admin");
        factory.setPassword("123456");
        // 建立连接
        Connection conn = factory.newConnection();
        // 创建消息通道
        Channel channel = conn.createChannel();

        // 声明交换机
        // String exchange, String type, boolean durable, boolean autoDelete, Map<String, Object> arguments
        channel.exchangeDeclare(EXCHANGE_NAME,"direct",false, false, null);


        // 声明队列
        // String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" Waiting for message....");

        // 绑定队列和交换机
        channel.queueBind(QUEUE_NAME,EXCHANGE_NAME,"gupao.best");

        // 创建消费者
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println("Received message : '" + msg + "'");
                //消费者标记，第一次请求获得，后续保持不变
                System.out.println("consumerTag : " + consumerTag );
                //投递消息的序号
                System.out.println("deliveryTag : " + envelope.getDeliveryTag() );
            }
        };

        // 开始获取消息
        // String queue, boolean autoAck, Consumer callback
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }
}

