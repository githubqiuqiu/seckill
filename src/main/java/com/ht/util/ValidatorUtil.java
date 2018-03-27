package com.ht.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 判断手机号格式是否正确的 util类
 * @auth Qiu
 * @time 2018/3/22
 **/
public class ValidatorUtil {
    private static final Pattern mobile_pattern = Pattern.compile("1\\d{10}");

    /**
     * 验证手机号格式是否正确
     * @param src 手机号
     * @return
     */
    public static boolean isMobile(String src) {
        if(StringUtils.isEmpty(src)) {
            return false;
        }
        Matcher m = mobile_pattern.matcher(src);
        return m.matches();
    }
}
