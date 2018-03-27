package com.ht.vo;

import com.ht.annotation.IsMobile;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @auth Qiu
 * @time 2018/3/22
 **/
public class LoginVo {
    @NotNull
    //自定义的一个注解标签
    @IsMobile
    private String mobile;

    @NotNull
    @Length(min=30)
    private String password;

    public String getMobile() {
        return mobile;
    }
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    @Override
    public String toString() {
        return "LoginVo [mobile=" + mobile + ", password=" + password + "]";
    }
}
