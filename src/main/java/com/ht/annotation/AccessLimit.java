package com.ht.annotation;


import static java.lang.annotation.ElementType.METHOD;
import static  java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 自定义的接口限流防刷注解
 * @auth Qiu
 * @time 2018/3/26
 **/
@Retention(RUNTIME)
@Target(METHOD)
public @interface AccessLimit {
    //多少秒
    int seconds();
    //最大访问次数
    int maxCount();
    //是否需要登录
    boolean needLogin() default true;
}

