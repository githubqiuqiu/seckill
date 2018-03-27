package com.ht.redis;

/**
 * @auth Qiu
 * @time 2018/3/26
 **/
public class SeckillKey extends BasePrefix{
    private SeckillKey( int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    //保存商品的库存是否为空到 redis 的key前缀
    public static SeckillKey isGoodsOver = new SeckillKey(0,"isGoodsOver_");
    //保存秒杀接口的 path 到redis的 key前缀
    public static SeckillKey getSeckillPath = new SeckillKey(60, "seckillPath_");
    //保存秒杀前的验证码判断 的值 到redis 的key 前缀
    public static SeckillKey getSeckillVerifyCode = new SeckillKey(300, "seckillVerifyCode_");
}
