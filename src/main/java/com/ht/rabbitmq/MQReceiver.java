package com.ht.rabbitmq;

import com.ht.domain.SeckillOrder;
import com.ht.domain.SeckillUser;
import com.ht.redis.RedisService;
import com.ht.service.GoodsService;
import com.ht.service.OrderService;
import com.ht.service.SeckillService;
import com.ht.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息队列的接受者
 * @auth Qiu
 * @time 2018/3/25
 **/
@Service
public class MQReceiver {


    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SeckillService seckillService;

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    /**
     * 接收一个 Direct模式的消息  queues=MQConfig.QUEUE 队列名
     * @param message
     */
    @RabbitListener(queues=MQConfig.QUEUE)
    public void receive(String message) {
        log.info("receive message:"+message);
    }

    /**
     * 接收一个的消息 queues=MQConfig.TOPIC_QUEUE1 队列名
     * 测试Topic 和Fanout模式的接收
     * @param message
     */
    @RabbitListener(queues=MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message) {
        log.info(" topic  queue1 message:"+message);
    }

    /**
     * 接收一个消息 queues=MQConfig.TOPIC_QUEUE2 队列名
     * 测试Topic 和Fanout模式的接收
     * @param message
     */
    @RabbitListener(queues=MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message) {
        log.info(" topic  queue2 message:"+message);
    }

    /**
     * 接收一个Header模式 的消息
     * 注意 用字节数组来接收
     * @param message
     */
    @RabbitListener(queues=MQConfig.HEADER_QUEUE)
    public void receiveHeaderQueue(byte[] message) {
        log.info(" header  queue message:"+new String(message));
    }


    /**
     * 接收一个秒杀的消息
     * @param message
     */
    @RabbitListener(queues=MQConfig.SECKILL_QUEUE)
    public void receiveSeckillMessage(String message) {
        log.info("receive message:"+message);

        SeckillMessage mm  = RedisService.stringToBean(message, SeckillMessage.class);
        //得到队列里面的 秒杀用户的信息
        SeckillUser user = mm.getUser();
        //得到队列里面的 秒杀的订单消息
        long goodsId = mm.getGoodsId();

        //获取商品的信息
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        //判断库存是否足够
        if(stock <= 0) {
            return;
        }
        //判断是否已经秒杀过了
        SeckillOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        //如果已经秒杀到了该商品
        if(order != null) {
            return;
        }

        //减库存 下订单 生成秒杀订单
        seckillService.miaosha(user, goods);
    }

}
