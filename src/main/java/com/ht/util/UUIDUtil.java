package com.ht.util;

import java.util.UUID;

/**
 * 生成通用唯一识别码 的util
 * @auth Qiu
 * @time 2018/3/22
 **/
public class UUIDUtil {
    public static String uuid() {
        //两个参数分别是 第一个是把 原生的uuid的-参数 替换成第二个参数 空 相当于生成的uuid去掉了 -
        return UUID.randomUUID().toString().replace("-", "");
    }
}
