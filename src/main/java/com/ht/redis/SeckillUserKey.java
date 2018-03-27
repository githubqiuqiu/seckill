package com.ht.redis;

/**
 * @auth Qiu
 * @time 2018/3/22
 **/
public class SeckillUserKey extends  BasePrefix{
    //设置过期时间
    public static final int TOKEN_EXPIRE_SECONDS = 3600*24 * 2;
    private SeckillUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    //用户浏览器上的token信息前缀 有过期时间
    public static SeckillUserKey token = new SeckillUserKey(TOKEN_EXPIRE_SECONDS, "user_");
    //用户redis里面保存的用户信息的前缀 永久保存
    public static SeckillUserKey getById = new SeckillUserKey(0, "id_");
}
