package com.cloud_guest.redis.aop.redis;

import java.lang.annotation.*;

/**
 * @Author yan
 * @Date 2024/5/24 0024 9:54
 * @Description
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME) //@Cacheable注解存在缺陷 必须使用缓存管理器
public @interface RedisCacheable {
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

    /**
     * 请求参数前缀名称
     * @return
     */
    String requestAsName() default "request";

    /**
     * 返回值类型
     * @return
     */
    Class classType() default Object.class;

    /**
     * 是否抛出异常
     * @return
     */
    boolean throwException() default true;
    String exceptionMessage() default "非法操作！";

    /**
     * 是否为hash类型
     * @return
     */
    boolean isHash() default false;
}
