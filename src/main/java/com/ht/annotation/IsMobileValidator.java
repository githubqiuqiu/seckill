package com.ht.annotation;

import com.ht.util.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 参数校验器: 校验手机号格式是否正确
 * @auth Qiu
 * @time 2018/3/22
 **/
//第一个参数是 注解的类名 第二个是注解的类型
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {
    private boolean required = false;

    //初始化方法  可以拿到注解
    public void initialize(IsMobile constraintAnnotation) {
        //判断注解里的这个值是否是必须值
        required = constraintAnnotation.required();
    }

    public boolean isValid(String value, ConstraintValidatorContext context) {
        //判断这个值是否是必须的
        if(required) {
            //是必须的 判断手机号格式是否正确
            return ValidatorUtil.isMobile(value);
        }else {
            //不是必须的 判断值是否为空
            if(StringUtils.isEmpty(value)) {
                return true;
            }else {
                //不为空 则判断手机号格式是否正确
                return ValidatorUtil.isMobile(value);
            }
        }
    }
}
