package com.ht.rabbitmq;

import com.ht.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ht.redis.RedisService;

/**
 * 消息队列的发送者
 * @auth Qiu
 * @time 2018/3/25
 **/
@Service
public class MQSender {
    private static Logger log = LoggerFactory.getLogger(MQSender.class);

    //操作消息队列的帮助类
    @Autowired
    AmqpTemplate amqpTemplate ;

    /**
     * 发送一个 Direct模式的消息
     * @param message
     */
    public void send(Object message) {
    String msg = RedisService.beanToString(message);
    log.info("send message:"+msg);
    //发送消息  转换AND发送
    amqpTemplate.convertAndSend(MQConfig.QUEUE, msg);
	}


    /**
     * 发送一个 Topic模式的消息
     * @param message
     */
	public void sendTopic(Object message) {
    String msg = RedisService.beanToString(message);
    log.info("send topic message:"+msg);
    //发送消息
    amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key1", msg+"1");
    amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key2", msg+"2");
	}

    /**
     * 发送一个Fanout模式 的消息
     * @param message
     */
	public void sendFanout(Object message) {
    String msg = RedisService.beanToString(message);
    log.info("send fanout message:"+msg);
    amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE, "", msg);
	}

    /**
     * 发送一个Header模式 的消息
     * @param message
     */
	public void sendHeader(Object message) {
		String msg = RedisService.beanToString(message);
		log.info("send fanout message:"+msg);
		MessageProperties properties = new MessageProperties();
		properties.setHeader("header1", "value1");
		properties.setHeader("header2", "value2");
		Message obj = new Message(msg.getBytes(), properties);
		amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE, "", obj);
	}

    /**
     * 秒杀商品时 发送的消息
     * @param seckillMessage
     */
    public void sendSeckillMessage(SeckillMessage seckillMessage) {
        String msg = RedisService.beanToString(seckillMessage);
        log.info("send message:"+msg);
        amqpTemplate.convertAndSend(MQConfig.SECKILL_QUEUE, msg);
    }

}
