package com.ht.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * 自定义的验证手机号格式的注解
 * @auth Qiu
 * @time 2018/3/22
 **/
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
//指向验证方式类
@Constraint(validatedBy = {IsMobileValidator.class })
public @interface IsMobile {

    //设置这个值是否必须  true为必须 false为不必须
    boolean required() default true;

    //校验不通过的信息
    String message() default "手机号码格式错误";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
