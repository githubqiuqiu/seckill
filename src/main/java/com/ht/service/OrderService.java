package com.ht.service;

import com.ht.domain.OrderInfo;
import com.ht.domain.SeckillOrder;
import com.ht.domain.SeckillUser;
import com.ht.vo.GoodsVo;
import org.apache.ibatis.annotations.Param;

/**
 * @auth Qiu
 * @time 2018/3/23
 **/
public interface OrderService {
    public SeckillOrder getMiaoshaOrderByUserIdGoodsId(@Param("userId")long userId, @Param("goodsId")long goodsId);

    public OrderInfo createOrder(SeckillUser user, GoodsVo goods);

    public OrderInfo getOrderById(@Param("orderId")long orderId);

    public void deleteOrders();
}
