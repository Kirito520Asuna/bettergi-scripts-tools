package com.cloud_guest.redis.aop.redis;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author yan
 * @Date 2024/8/14 0014 11:39:36
 * @Description
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisNoRepeatSubmit {
    /*
     * 缓存名称 *  cache name
     */
    String cacheName() default "";

    /**
     * 条件
     * @return
     */
    String condition() default "true";

    /**
     * 缓存key
     *
     * @return
     */
    String key() default "";
    String value() default "";
    String requestAsName() default "request";
    String requestPrefixHeaderAsName() default "X-Request-Prefix-";
    String userIdAsName() default "currentUserId";
    String addressAsName() default "address";
    long timeout() default 10;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    String exceptionMessage() default "请勿重复操作！";
}
