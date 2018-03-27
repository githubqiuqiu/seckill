package com.ht.annotation;

import com.ht.domain.SeckillUser;

/**
 * 保存用户信息到当前线程 (不存在线程安全的问题) 每个线程单独保存一份
 * @auth Qiu
 * @time 2018/3/26
 **/
public class UserContext {
    private static ThreadLocal<SeckillUser> userHolder = new ThreadLocal<SeckillUser>();

    public static void setUser(SeckillUser user) {
        userHolder.set(user);
    }

    public static SeckillUser getUser() {
        return userHolder.get();
    }
}
