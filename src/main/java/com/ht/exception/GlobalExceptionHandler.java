package com.ht.exception;

import com.ht.result.CodeMsg;
import com.ht.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 全局异常处理器
 * @auth Qiu
 * @time 2018/3/22
 **/
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    //这里面的value 可以指定拦截的异常类  这里拦截的是所有的异常Exception
    @ExceptionHandler(value=Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e){
        //打印异常
        e.printStackTrace();

        //判断异常是否是自定义的全局异常
        if(e instanceof GlobalException) {
            GlobalException ex = (GlobalException)e;
            return Result.error(ex.getCm());
        }
        //判断异常是否是绑定参数异常
        else if(e instanceof BindException) {
            BindException ex = (BindException)e;
            //获取错误信息 返回一个数组
            List<ObjectError> errors = ex.getAllErrors();
            //得到第一个错误
            ObjectError error = errors.get(0);
            //获取第一个错误的错误信息
            String msg = error.getDefaultMessage();
            //把错误信息返回给Result对象显示
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
        }else {
            //其他异常
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
