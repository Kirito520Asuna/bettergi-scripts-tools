package com.cloud_guest.redis.aop.redis;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author yan
 * @Date 2024/5/29 0029 16:26
 * @Description
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLock {
    String key();
    String requestAsName() default "request";
    // 获取锁等待时间
    long waitTime() default 10;
    //
    long leaseTime() default 10;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    String exceptionMessage() default "获取分布式锁失败！";
}
