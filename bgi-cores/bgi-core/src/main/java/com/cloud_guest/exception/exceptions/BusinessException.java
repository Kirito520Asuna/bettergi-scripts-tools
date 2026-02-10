package com.cloud_guest.exception.exceptions;


import com.cloud_guest.enums.ApiCode;

/**
 * @Author yan
 * @Date 2024/7/30 0030 10:57:25
 * @Description
 */
public class BusinessException extends GlobalCustomException{
    public BusinessException(Integer code) {
        super(code);
    }

    public BusinessException() {
        super(ApiCode.FAIL);
    }

    public BusinessException(ApiCode apiCode) {
        super(apiCode);
    }

    public BusinessException(int code, String message) {
        super(code, message);
    }

    public BusinessException(ApiCode apiCode, String message) {
        super(apiCode, message);
    }

    public BusinessException(String message) {
        super(message);
    }
}
