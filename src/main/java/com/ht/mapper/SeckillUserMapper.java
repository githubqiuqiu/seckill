package com.ht.mapper;

import com.ht.domain.SeckillUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * @auth Qiu
 * @time 2018/3/22
 **/
@Repository
@Mapper
public interface SeckillUserMapper {
    @Select("select * from seckill_user where id = #{id}")
    public SeckillUser getById(@Param("id")long id);

    @Update("update seckill_user set password = #{password} where id = #{id}")
    public void update(SeckillUser toBeUpdate);
}
