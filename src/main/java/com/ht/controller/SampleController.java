package com.ht.controller;

import com.ht.domain.User;
import com.ht.rabbitmq.MQSender;
import com.ht.redis.RedisService;
import com.ht.redis.UserKey;
import com.ht.result.CodeMsg;
import com.ht.result.Result;
import com.ht.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @auth Qiu
 * @time 2018/3/20
 **/
@Controller
@RequestMapping("/sample")
public class SampleController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MQSender mqSender;

    /**
     * 测试发送 Header模式的信息
     * @return
     */
    @RequestMapping("/mq/header")
    @ResponseBody
    public Result<String> header() {
        mqSender.sendHeader("hello,imooc");
        return Result.success("Hello，world");
    }

    /**
     * 测试发送 Fanout模式的消息
     * @return
     */
    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> fanout() {
        mqSender.sendFanout("hello,imooc");
        return Result.success("Hello，world");
    }

    /**
     * 测试rabbitmq的发送 Direct模式的消息
     * @return
     */
	@RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq() {
	    //测试rabbitmq 发送消息
        mqSender.send("hello,rabbitmq");
        return Result.success("Hello,rabbitmq");
   }

    /**
     * 测试rabbitmq的发送 Topic模式的消息
     * @return
     */
    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> topic() {
       mqSender.sendTopic("hello,imooc");
        return Result.success("Hello，world");
    }

    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {
        return "Hello World!";
    }


    @RequestMapping("/success")
    @ResponseBody
    public Result<String> success(){
      return Result.success("test success");
    }

    @ResponseBody
    @RequestMapping("/error")
    public Result<String> error(){
        return  Result.error(CodeMsg.SERVER_ERROR);
    }

    /**
     * 测试 thymeleaf 模板
     * @param model
     * @return
     */
    @RequestMapping("/thymeleaf")
    public String thymeleaf(Model model){
        model.addAttribute("name","springboot");
        return "hello";
    }

    /**
     * 测试mybatis的查询
     * @return
     */
    @RequestMapping("/selectUserById")
    @ResponseBody
    public Result<User> selectUserById(){
        return   Result.success(userService.getById(1));
    }

    /**
     * 测试mybatis的事务
     */
    @RequestMapping("/insertUser")
    public void  insertUser(){
        User user=new User();
        userService.insertUser(user);
    }


    /**
     * 测试redis的 set()方法
     * @return
     */
    @RequestMapping("/redisSet")
    @ResponseBody
    public Result<User> redisSet() {
        User user  = new User();
        user.setId(1);
        user.setUname("1111");
        redisService.set(UserKey.getById, ""+1, user);//UserKey:id1
        return Result.success(user);
    }

    /**
     * 测试redis 的get() 方法
     * @return
     */
    @RequestMapping("/redisGet")
    @ResponseBody
    public Result<User> redisGet() {
        User  user  = redisService.get(UserKey.getById, ""+1, User.class);
        return Result.success(user);
    }


}
