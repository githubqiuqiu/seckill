package com.ht.mapper;

import com.ht.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @auth Qiu
 * @time 2018/3/21
 **/
@Repository
@Mapper
public interface UserMapper {

    @Select("select * from user where id = #{id}")
    public User getById(@Param("id") Integer id);

    @Insert("insert into user(uname) values(#{uname})")
    public Integer insertUser(User user);


}
