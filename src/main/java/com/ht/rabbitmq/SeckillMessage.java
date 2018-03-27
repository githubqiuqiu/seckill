package com.ht.rabbitmq;

import com.ht.domain.SeckillUser;

/**
 * 秒杀的消息对象
 * @auth Qiu
 * @time 2018/3/26
 **/
public class SeckillMessage {
    //秒杀的用户信息
    private SeckillUser user;
    //商品id信息
    private long goodsId;

    public SeckillUser getUser() {
        return user;
    }

    public void setUser(SeckillUser user) {
        this.user = user;
    }

    public long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(long goodsId) {
        this.goodsId = goodsId;
    }
}
