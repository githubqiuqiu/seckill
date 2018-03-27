package com.ht.redis;

/**
 * 设置redis的 key 实现类
 * @auth Qiu
 * @time 2018/3/21
 **/
public abstract class BasePrefix implements KeyPrefix {

    //过期时间
    private int expireSeconds;
    //前缀
    private String prefix;


    public BasePrefix(String prefix) {//0代表永不过期
        this(0, prefix);
    }

    public BasePrefix( int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    //设置过期时间
    public int expireSeconds() {//默认0代表永不过期
        return expireSeconds;
    }

    //设置前缀
    public String getPrefix() {
        String className = getClass().getSimpleName();
        return className+":" + prefix;
    }

}
