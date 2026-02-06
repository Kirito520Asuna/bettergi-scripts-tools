package com.cloud_guest.redis.aop.ban;


import com.cloud_guest.redis.ban.BanType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Ban {
    BanType type() default BanType.IP_INTERFACE; // 封禁类型
    long windowSeconds() default 60;   // 请求时间窗口（秒）
    long maxRequests() default 10;     // 最大请求次数
    long banDuration() default 3600;   // 自动封禁时长
    TimeUnit timeUnit() default TimeUnit.SECONDS; // 时间单位
}