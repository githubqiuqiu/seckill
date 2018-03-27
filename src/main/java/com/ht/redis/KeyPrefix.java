package com.ht.redis;

/**
 * 设置redis的 key 接口
 * @auth Qiu
 * @time 2018/3/21
 **/
public interface KeyPrefix {

    //设置过期时间
    public int expireSeconds();

    //设置前缀
    public String getPrefix();

}
