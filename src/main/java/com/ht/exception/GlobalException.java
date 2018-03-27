package com.ht.exception;

import com.ht.result.CodeMsg;

/**
 * 全局异常类
 * @auth Qiu
 * @time 2018/3/22
 **/
public class GlobalException extends  RuntimeException{
    private static final long serialVersionUID = 1L;

    //返回的错误信息
    private CodeMsg cm;

    public GlobalException(CodeMsg cm) {
        super(cm.toString());
        this.cm = cm;
    }

    public CodeMsg getCm() {
        return cm;
    }
}
