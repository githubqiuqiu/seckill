package com.ht.redis;

/**
 * 继承了 获取redis前缀的 实现类BasePrefix
 * @auth Qiu
 * @time 2018/3/21
 **/
public class OrderKey  extends BasePrefix{
    public OrderKey(String prefix) {
        super(prefix);
    }
    public static OrderKey getMiaoshaOrderByUidGid = new OrderKey("UidGid_");
}
