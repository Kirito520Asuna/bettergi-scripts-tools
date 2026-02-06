package com.cloud_guest.redis.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

;

/**
 * @Author yan
 * @Date 2024/7/30 0030 10:57:25
 * @Description
 */
@NoArgsConstructor
@Getter
public class RedisException extends RuntimeException {
    private Integer code = Integer.valueOf("500");
    public RedisException(String message) {
        super(message);
    }

    public RedisException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
