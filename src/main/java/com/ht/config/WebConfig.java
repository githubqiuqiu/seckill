package com.ht.config;

import com.ht.annotation.AccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * @auth Qiu
 * @time 2018/3/22
 **/
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter{

    @Autowired
    private UserArgumentResolver userArgumentResolver;

    @Autowired
    AccessInterceptor accessInterceptor;


    /**
     * 参数解析  框架会回调这个方法 给controller 的参数赋值
     * @param argumentResolvers 参数解析器
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
           //添加一个参数解析器
            argumentResolvers.add(userArgumentResolver);
    }


    /**
     * 注入拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessInterceptor);
    }

}
