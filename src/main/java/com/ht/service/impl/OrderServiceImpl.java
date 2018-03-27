package com.ht.service.impl;

import com.ht.domain.OrderInfo;
import com.ht.domain.SeckillOrder;
import com.ht.domain.SeckillUser;
import com.ht.mapper.OrderMappper;
import com.ht.redis.OrderKey;
import com.ht.redis.RedisService;
import com.ht.service.OrderService;
import com.ht.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @auth Qiu
 * @time 2018/3/23
 **/
@Service
@Transactional
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderMappper orderMappper;

    @Autowired
    RedisService redisService;

    @Override
    public SeckillOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId) {
        //return orderDao.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
        //把查询订单的方法 不从数据库中查 改成从redis中查
        return redisService.get(OrderKey.getMiaoshaOrderByUidGid, ""+userId+"_"+goodsId, SeckillOrder.class);
    }

    /**
     * 创建订单
     * @param user
     * @param goods
     * @return
     */
    @Override
    @Transactional
    public OrderInfo createOrder(SeckillUser user, GoodsVo goods) {
        //插入订单详情表
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getSeckillPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        //这里成功的话  会返回成功的条数 1
        orderMappper.insert(orderInfo);

        //插入秒杀订单表
        SeckillOrder miaoshaOrder = new SeckillOrder();
        miaoshaOrder.setGoodsId(goods.getId());
        miaoshaOrder.setOrderId(orderInfo.getId());
        miaoshaOrder.setUserId(user.getId());
        orderMappper.insertMiaoshaOrder(miaoshaOrder);

        //生成订单后  把秒杀的数据设置到redis中
        redisService.set(OrderKey.getMiaoshaOrderByUidGid, ""+user.getId()+"_"+goods.getId(), miaoshaOrder);

        return orderInfo;
    }

    @Override
    public OrderInfo getOrderById(long orderId) {
        return orderMappper.getOrderById(orderId);
    }


    @Override
    public void deleteOrders() {
        orderMappper.deleteOrders();
        orderMappper.deleteMiaoshaOrders();
    }
}
