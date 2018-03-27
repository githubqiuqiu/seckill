package com.ht.redis;

/**
 * 继承了 获取redis前缀的 实现类BasePrefix
 * @auth Qiu
 * @time 2018/3/21
 **/
public class UserKey extends BasePrefix{
    private UserKey(String prefix) {
        super(prefix);
    }

    private UserKey(int expireSeconds, String prefix) {
        super(expireSeconds,prefix);
    }


    public static UserKey getById = new UserKey("id");
    public static UserKey getByName = new UserKey("name");

}
