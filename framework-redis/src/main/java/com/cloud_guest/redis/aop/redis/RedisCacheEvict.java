package com.cloud_guest.redis.aop.redis;

import java.lang.annotation.*;

/**
 * @Author yan
 * @Date 2024/5/24 0024 9:58
 * @Description
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME) //@CacheEvict注解 存在缺陷 必须使用缓存管理器
public @interface RedisCacheEvict {
    /**
     * 缓存名称
     * @return
     */
    String cacheName() default "";

    /**
     * 条件
     * @return
     */
    String condition() default "true";

    /**
     * 缓存key
     * @return
     */
    String key() default "";
    String requestAsName() default "request";
    String responseAsName() default "response";

    /**
     * 是否是hash
     * @return
     */
    boolean isHash() default false;
}
