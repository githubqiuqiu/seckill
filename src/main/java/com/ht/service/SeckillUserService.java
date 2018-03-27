package com.ht.service;

import com.ht.domain.SeckillUser;
import com.ht.vo.LoginVo;
import org.apache.ibatis.annotations.Param;

import javax.servlet.http.HttpServletResponse;

/**
 * @auth Qiu
 * @time 2018/3/22
 **/
public interface SeckillUserService {

    //要设置的cookie的名字
    public static final String COOKIE_NAME_TOKEN = "token";

    /**
     * 根据id 查询用户信息
     * @param id 用户id
     * @return
     */
    public SeckillUser getById(long id);

    /**
     * 登录的判断
     * @param response
     * @param loginVo
     * @return
     */
    public boolean login(HttpServletResponse response, LoginVo loginVo);


    /**
     *  根据cookie 从redis中获取用户的session的值
     * @param response
     * @param token 要从redis里面获取值的 cookie里或者传进来的参数 token
     * @return
     */
    public SeckillUser getSessionByToken(HttpServletResponse response, String token);

    /**
     * 修改密码
     * @param token
     * @param id
     * @param formPass
     * @return
     */
    public boolean updatePassword(String token, long id, String formPass);
}
