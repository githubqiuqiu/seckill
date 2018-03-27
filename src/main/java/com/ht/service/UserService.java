package com.ht.service;

import com.ht.domain.User;
import org.apache.ibatis.annotations.Param;

/**
 * @auth Qiu
 * @time 2018/3/21
 **/
public interface UserService {
    public User getById(@Param("id") Integer id);
    public void insertUser(User user);
}
