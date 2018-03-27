package com.ht.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * MD5加密工具类
 * @auth Qiu
 * @time 2018/3/22
 **/
public class MD5Util {


    /**
     * MD5加密的方法
     * @param src 要加密的密码
     * @return
     */
    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
    }

    /**
     * 加密的盐值
     */
    private static final String salt = "1a2b3c4d";

    /**
     * 第一次加密的方法
     * 第一次加密  把用户在表单上提交的明文密码 先通过第一次加密 传到服务器端(第一次还不保存在数据库中)
     * @param inputPass
     * @return
     */
    public static String inputPassToFormPass(String inputPass) {
        //取盐值里面的位数+用户输入的明文密码  组成新的密码 然后加密
        String str = ""+salt.charAt(0)+salt.charAt(2) + inputPass +salt.charAt(5) + salt.charAt(4);
        //System.out.println(str);
        return md5(str);
    }

    /**
     * 第二次加密的方法
     * 把第一次加密后的密码 再做第二次加密  这次加密后的密码就保存在数据库中
     * @param formPass 要加密的密码
     * @param salt 加密的盐值
     * @return
     */
    public static String formPassToDBPass(String formPass, String salt) {
        String str = ""+salt.charAt(0)+salt.charAt(2) + formPass +salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    /**
     * 明文密码 加密两次后 转换成的数据库密码
     * @param inputPass
     * @param saltDB
     * @return
     */
    public static String inputPassToDbPass(String inputPass, String saltDB) {
        String formPass = inputPassToFormPass(inputPass);
        String dbPass = formPassToDBPass(formPass, saltDB);
        return dbPass;
    }

    public static void main(String[] args) {
        System.out.println("第一次明文加密后的密码:"+inputPassToFormPass("123456"));//d3b1294a61a07da9b49b6e22b2cbd7f9
		System.out.println("第二次加密后的密码:"+formPassToDBPass(inputPassToFormPass("123456"), "1a2b3c4d"));
		System.out.println("明文密码加密两次后的数据库密码:"+inputPassToDbPass("123456", "1a2b3c4d"));//b7797cce01b4b131b433b6acf4add449
    }
}
