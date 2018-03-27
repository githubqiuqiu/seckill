package com.ht.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitmq的配置文件信息
 * @auth Qiu
 * @time 2018/3/25
 **/
@Configuration
public class MQConfig {
    public static final String SECKILL_QUEUE = "seckill.queue";

    public static final String QUEUE = "queue";
    public static final String TOPIC_QUEUE1 = "topic.queue1";
    public static final String TOPIC_QUEUE2 = "topic.queue2";
    public static final String HEADER_QUEUE = "header.queue";
    public static final String TOPIC_EXCHANGE = "topicExchage";
    public static final String FANOUT_EXCHANGE = "fanoutxchage";
    public static final String HEADERS_EXCHANGE = "headersExchage";

    /**
     * 完全根据key进行投递的叫做Direct交换机，例如，绑定时设置了routing key为”abc”，那么客户端提交的消息，只有设置了key为”abc”的才会投递到队列。
     * 对key进行模式匹配后进行投递的叫做Topic交换机，符号”#”匹配一个或多个词，符号”*”匹配正好一个词。例如”abc.#”匹配”abc.def.ghi”，”abc.*”只匹配”abc.def”。
     * 还有一种不需要key的，叫做Fanout交换机，它采取广播模式，一个消息进来时，投递到与该交换机绑定的所有队列。
     *
     Direct：direct 类型的行为是"先匹配, 再投送". 即在绑定时设定一个 routing_key, 消息的routing_key 匹配时, 才会被交换器投送到绑定的队列中去.
     Topic：按规则转发消息（最灵活） *表示一个词   #表示零个或多个词
     Headers：设置header attribute参数类型的交换机
     Fanout：转发消息到所有绑定队列
     */


    /**
     * Direct模式(最简单的一种模式) 交换机Exchange
     * */
    @Bean
    public Queue queue() {
        return new Queue(QUEUE, true);
    }

    /**
     * 使用Direct模式  发送SECKILL_QUEUE  这个队列
     * @return
     */
    @Bean
    public Queue seckillQueue() {
        return new Queue(SECKILL_QUEUE, true);
    }


    /**
     * Topic模式 交换机Exchange
     * 先把消息放到交换机Exchange 中  在把Exchange中的消息 放到MQ队列里
     * */
    @Bean
    public Queue topicQueue1() {
        return new Queue(TOPIC_QUEUE1, true);
    }
    @Bean
    public Queue topicQueue2() {
        return new Queue(TOPIC_QUEUE2, true);
    }

    /**
     * Topic 模式的交换机
     * @return
     */
    @Bean
    public TopicExchange topicExchage(){
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    /**
     * 绑定Topic模式 的交换机Exchage 和 queue队列
     * @return
     */
    @Bean
    public Binding topicBinding1() {
        //把topicQueue1 绑定到 topicExchage 交换机上 并且带上一个名字
        return BindingBuilder.bind(topicQueue1()).to(topicExchage()).with("topic.key1");
    }
    @Bean
    public Binding topicBinding2() {
        return BindingBuilder.bind(topicQueue2()).to(topicExchage()).with("topic.#");
    }

    /**
     * Fanout模式 交换机Exchange
     * 创建Fanout模式 的交换机
     * */

    @Bean
    public FanoutExchange fanoutExchage(){
        return new FanoutExchange(FANOUT_EXCHANGE);
    }

    /**
     * 绑定Fanout模式 的交换机 和 queue 队列
     * @return
     */
    @Bean
    public Binding FanoutBinding1() {
        return BindingBuilder.bind(topicQueue1()).to(fanoutExchage());
    }
    @Bean
    public Binding FanoutBinding2() {
        return BindingBuilder.bind(topicQueue2()).to(fanoutExchage());
    }

    /**
     * Header模式 交换机Exchange
     * 创建Header模式 交换机
     * */
    @Bean
    public HeadersExchange headersExchage(){
        return new HeadersExchange(HEADERS_EXCHANGE);
    }
    //创建队列
    @Bean
    public Queue headerQueue1() {
        return new Queue(HEADER_QUEUE, true);
    }
    //绑定Header模式 交换机 和queue队列
    @Bean
    public Binding headerBinding() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("header1", "value1");
        map.put("header2", "value2");
        //whereAll 满足所有的key 和value 才会运行这个队列
        return BindingBuilder.bind(headerQueue1()).to(headersExchage()).whereAll(map).match();
    }


}
