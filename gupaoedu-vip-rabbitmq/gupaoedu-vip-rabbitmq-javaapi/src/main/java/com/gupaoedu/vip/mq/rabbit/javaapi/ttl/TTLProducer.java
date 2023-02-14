package com.gupaoedu.vip.mq.rabbit.javaapi.ttl;

import com.gupaoedu.vip.mq.rabbit.javaapi.util.ResourceUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 消息生产者，通过TTL测试死信队列
 */
public class TTLProducer {

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(ResourceUtil.getKey("rabbitmq.uri"));

        // 建立连接
        Connection conn = factory.newConnection();
        // 创建消息通道
        Channel channel = conn.createChannel();

        String msg = "Hello world, Rabbit MQ, DLX MSG";

        // 通过队列属性设置消息过期时间
        Map<String, Object> argss = new HashMap<String, Object>();
        argss.put("x-message-ttl",6000);

        // 声明队列（默认交换机AMQP default，Direct）
        // String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        channel.queueDeclare("TEST_TTL_QUEUE", false, false, false, argss);

        Map<String,Object> headers = new HashMap<>();

        // 对每条消息设置过期时间
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .deliveryMode(2) // 2代表持久化
                .contentEncoding("UTF-8") // 编码
                .expiration("10000") // TTL，过期时间
                .headers(headers) // 自定义属性
                .priority(5) // 优先级，默认为5，配合队列的 x-max-priority 属性使用
                .messageId(String.valueOf(UUID.randomUUID()))
                .build();

        // 此处两种方式设置消息过期时间的方式都使用了，将以较小的数值为准

        // 发送消息
        channel.basicPublish("", "TEST_TTL_QUEUE_FROM_JAVAAPI", properties, msg.getBytes());

        channel.close();
        conn.close();
    }
}

