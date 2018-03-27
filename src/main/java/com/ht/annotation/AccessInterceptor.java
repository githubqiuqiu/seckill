package com.ht.annotation;

import java.io.OutputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ht.domain.SeckillUser;
import com.ht.redis.RedisService;
import com.ht.service.SeckillUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSON;
import com.ht.domain.SeckillUser;
import com.ht.redis.AccessKey;
import com.ht.redis.RedisService;
import com.ht.result.CodeMsg;
import com.ht.result.Result;
import com.ht.service.SeckillUserService;

/**
 * 自定义的接口限流防刷注解 的拦截器
 * @auth Qiu
 * @time 2018/3/26
 **/

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    RedisService redisService;

    //方法执行前拦截
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if(handler instanceof HandlerMethod) {

            //判断浏览器里的cookie 和redis里面保持的cookie是否相等
            SeckillUser user = getUser(request, response);

            //保存用户到当前线程
            UserContext.setUser(user);

            //获得 @AccessLimit 注解
            HandlerMethod hm = (HandlerMethod)handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);

            //如果没有限制 返回true 表示不做任何限制 判断是否有@AccessLimit 注解
            if(accessLimit == null) {
                return true;
            }

            //如果有限制  有注解
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            //获取页面的url地址
            String key = request.getRequestURI();
            //判断是否需要登录
            if(needLogin) {
                //判断用户是否为空
                if(user == null) {
                    render(response, CodeMsg.SESSION_ERROR);
                    return false;
                }
                //当有 @AccessLimit注解的时候 获取需要注解的URL地址 拼接上用户的id 得到一个key
                key += "_" + user.getId();
            }else {
                //do nothing
            }
            // 设置该URL地址的key 保存在redis中的存活时间
            AccessKey ak = AccessKey.withExpire(seconds);

            //从redis根据key 获取redis的值
            Integer count = redisService.get(ak, key, Integer.class);
            //判断redis中获取的值 和注解传入的 最大访问次数的值 做对比
            if(count  == null) {
                //第一次访问 给redis里设置这个key 的存活时间 以及初始化value 的值为1
                redisService.set(ak, key, 1);
            }else if(count < maxCount) {
                //如果不是第一次访问  但是访问次数又比最大次数少 就根据key 把这个value增加1
                redisService.incr(ak, key);
            }else {
                //如果超过了访问次数  抛出异常信息
                render(response, CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }



    //用户没登入  抛出错误异常  设置错误信息
    private void render(HttpServletResponse response, CodeMsg cm)throws Exception {
        //设置输出的编码方式
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str  = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    /**
     * 判断浏览器里的 cookie 和 redis里面保存的cookie 是否相等
     * @param request
     * @param response
     * @return
     */
    private SeckillUser getUser(HttpServletRequest request, HttpServletResponse response) {
        String paramToken = request.getParameter(seckillUserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, seckillUserService.COOKIE_NAME_TOKEN);
        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return seckillUserService.getSessionByToken(response, token);
    }

    //从浏览器上获取cookie的值
    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[]  cookies = request.getCookies();
        if(cookies == null || cookies.length <= 0){
            return null;
        }
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(cookiName)) {
                return cookie.getValue();
            }
        }
        return null;
    }


}
