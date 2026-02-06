package com.cloud_guest.redis.exception;

import lombok.Getter;

/**
 * @Author yan
 * @Date 2025/5/20 14:36:52
 * @Description
 */
@Getter
public class BanException extends RedisException {
    public BanException(String message) {
        super(message);
    }

    public BanException() {
        super("禁止访问");
    }
}
