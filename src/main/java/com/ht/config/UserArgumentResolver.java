package com.ht.config;


import com.ht.annotation.UserContext;
import com.ht.domain.SeckillUser;
import com.ht.service.SeckillUserService;
import com.ht.service.impl.SeckillUserServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;



/**
 * 创建一个SeckillUser 类的参数解析器
 * @auth Qiu
 * @time 2018/3/22
 **/
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {


    @Autowired
    SeckillUserService seckillService;

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        //获取参数的类型
        Class<?> clazz = methodParameter.getParameterType();
        //判断参数的类型 如果相等返回true  (如果是SeckillUser类型  就继续做下一步的处理)
        return clazz==SeckillUser.class;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
     /*   //获取request和response
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = nativeWebRequest.getNativeResponse(HttpServletResponse.class);

        //获得参数传递的token 和 cookie里面的token
        String paramToken = request.getParameter(SeckillUserServiceImpl.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, SeckillUserServiceImpl.COOKIE_NAME_TOKEN);

        //判断参数传递token 或者cookie里面的token 是否为空
        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        //根据key  从redis里获取SeckillUser值的方法
        return seckillService.getSessionByToken(response, token);*/

        //上面的代码 可以用这个方法代替
        return UserContext.getUser();

    }


    /**
     * 获得cookie的方法
     * @param request
     * @param cookiName
     * @return
     */
    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[]  cookies = request.getCookies();

        if(cookies==null ||cookies.length<=0){
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
