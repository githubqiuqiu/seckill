package com.ht.redis;

/**
 * 接口访问次数限制 key
 * @auth Qiu
 * @time 2018/3/26
 **/
public class AccessKey extends BasePrefix{

    private AccessKey( int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static AccessKey withExpire(int expireSeconds) {
        return new AccessKey(expireSeconds, "access");
    }
}
