package com.ht.service.impl;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import com.ht.domain.SeckillUser;
import com.ht.exception.GlobalException;
import com.ht.mapper.SeckillUserMapper;
import com.ht.redis.RedisService;
import com.ht.redis.SeckillUserKey;
import com.ht.result.CodeMsg;
import com.ht.service.SeckillUserService;
import com.ht.util.MD5Util;
import com.ht.util.UUIDUtil;
import com.ht.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @auth Qiu
 * @time 2018/3/22
 **/
@Service
@Transactional
public class SeckillUserServiceImpl implements SeckillUserService {
    @Autowired
    SeckillUserMapper seckillUserMapper;

    @Autowired
    RedisService redisService;

    //要设置的cookie的名字
    public static final String COOKIE_NAME_TOKEN = "token";

    /**
     * 实现分布式session  session 并没有存到容器中来 而是存在了缓存中
     * @param response httpservletresponse
     * @param token 生成的uuid
     * @param user 要设置到redis的 object值
     */
    private void addCookie(HttpServletResponse response, String token, SeckillUser user) {

        /**
         * 第一个参数是 要设置到redis中的key 的前缀
         * 第二个参数是 生成的uuid  (因为这个set方法是自己定义的 所以redis的key为第一个参数拼接第二个参数)
         * 第三个参数是 要保存到redis中的value
         */
        //把用户信息设置到redis中 (注意 这里面设置了redis的过期时间 )
        try {
            //这里可能会有redis的连接异常
            redisService.set(SeckillUserKey.token, token, user);
        }catch (Exception e){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        //把token信息设置到cookie中
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        //设置cookie的过期时间(和redis的过期时间一致)
        cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    //根据cookie 从redis中获取用户的session的值
    public SeckillUser getSessionByToken(HttpServletResponse response, String token) {
        if(StringUtils.isEmpty(token)) {//先要判断一下cookie里的值是否为空
            return null;
        }
        //如果cookie里的值不为空  从redis中根据 前缀+token 组成的key 获取value的值  获取的值是一个泛型
        SeckillUser user = redisService.get(SeckillUserKey.token, token, SeckillUser.class);

        //如果cookie没过期 但是redis里面的值失效了
        // 延长有效期
        if(user != null) {
            //重新设置一次cookie
            addCookie(response, token, user);
        }
        return user;
    }




    /**
     * 登录判断
     * @param response
     * @param loginVo
     * @return
     */
    @Override
    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        //如果loginVo 为空 抛出服务异常
        if(loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        //获取表单的两个属性
        String mobile = loginVo.getMobile();
        //这个密码在表单传进来的时候 已经加密过第一次了
        String formPass = loginVo.getPassword();

        //判断手机号是否存在 若手机号存在 则用户信息不会为空
        SeckillUser user = getById(Long.parseLong(mobile));
        if(user == null) {
            //为空抛出手机号不存在的异常
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }

        //验证密码  获取数据库的密码
        String dbPass = user.getPassword();
        //获取数据库的盐值
        String saltDB = user.getSalt();
        //把表单传进来的第一次加密的密码 再加密一次
        String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
        //把第二次加密后的密码 和数据库的密码对比
        if(!calcPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }

        //如果登录成功 给用户生成一个类似sessionId的东西 然后把这个token放到浏览器的cookie中
        //生成一个uuid
        String token= UUIDUtil.uuid();
        //把用户信息写到redis中 把session存到第三方缓存中
        addCookie(response,token,user);

        return true;
    }

    /**
     * 当修改了用户的信息的时候  要把关于用户的缓存信息全部更新
     * @param token
     * @param id
     * @param formPass
     * @return
     */
    @Override
    public boolean updatePassword(String token, long id, String formPass) {
        //取user
        SeckillUser user = getById(id);
        if(user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //更新数据库
        SeckillUser toBeUpdate = new SeckillUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
        seckillUserMapper.update(toBeUpdate);
        //处理缓存 更新了用户信息后  把缓存的用户信息删除 下一次用户查询用户信息的时候  会进入getById()方法 判断缓存中没数据  然后从数据库中查询数据 然后重新设置到redis缓存中
        redisService.delete(SeckillUserKey.getById, ""+id);
        user.setPassword(toBeUpdate.getPassword());

        //更新之前用户登录的 token 里面的user信息  因为user信息进行了修改
        redisService.set(SeckillUserKey.token, token, user);
        return true;
    }

    /**
     * 把用户的信息缓存到redis  根据id获取redis里的用户信息 永久保存 但是修改了用户信息的时候 和用户有关的缓存也要修改
     * @param id 用户id
     * @return
     */
    public SeckillUser getById(long id) {
        //取缓存 先判断缓存中有没有用户信息  用户信息在redis里永久保存
        SeckillUser user = redisService.get(SeckillUserKey.getById, ""+id, SeckillUser.class);
        if(user != null) {
            return user;
        }
        //取数据库  没有的话就取数据库  然后保存到缓存里
        user = seckillUserMapper.getById(id);
        if(user != null) {
            redisService.set(SeckillUserKey.getById, ""+id, user);
        }
        return user;
    }
}
