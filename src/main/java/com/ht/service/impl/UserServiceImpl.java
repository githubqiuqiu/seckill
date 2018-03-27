package com.ht.service.impl;

import com.ht.domain.User;
import com.ht.mapper.UserMapper;
import com.ht.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @auth Qiu
 * @time 2018/3/21
 **/
@Service
@Transactional
public class UserServiceImpl implements UserService{
    @Autowired
    private UserMapper userMapper;

    @Override
    public User getById(Integer id) {
        return userMapper.getById(id);
    }

    /**
     * 测试事务
     * @param user
     */
    @Override
    public void insertUser(User user) {

        User user1=new User();
        user1.setId(3);
        user1.setUname("张三1");

        userMapper.insertUser(user1);

        User user2=new User();
        user2.setUname(Integer.valueOf("xxx")+"");
        userMapper.insertUser(user2);

    }
}
