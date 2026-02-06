package com.cloud_guest.redis.aop.redis;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author yan
 * @Date 2024/5/24 0024 9:55
 * @Description 对@CachePut 仿写增强 可设置有效时间
 * 注：若同时使用@CachePut 和@RedisCachePut  @RedisCachePut 在@CachePut 之后执行
 * 可能会出现俩种情况 Cache-name 和 Key 相同时  如：
 * @CachePut 的 value ==> Cache-name
 * @CachePut(value = "test", key = "#key+'_'+#value+#result.data.key+'_'+#result.data.value")
 * @RedisCachePut 的 cacheName ==> Cache-name
 * @RedisCachePut(cacheName = "test", key = "#re.key+'_'+#re.value"
 *             ,value = "#{result.data.key}+'_'+#{result.data.value}"
 *             ,requestAsName = "re"
 *             ,responseAsName = "result"
 *             ,condition = "#{result.data}!=null",timout = 1,timeUnit = TimeUnit.MINUTES)
 *
 * 由于 CachePut 的 Key构建是使用 "::"拼接的
 * 而RedisCachePut 的 Key构建是使用 ":"拼接的 因此同时使用时会在redis中设置俩个不同的key
 * 如 test::123 和 test:123
 * 因此不建议同时使用
 *
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisCachePut {
    /**
     * 1. cacheName 缓存名称
     * 2. condition  条件
     * 3. key  key值
     * 4. value 缓存值 value值 当value 不设置时默认设置为返回值
     *
     * 5说明
     * 5.1.response 返回值前缀 response.xx.xx requestAsName 可替换前缀名
     * 5.2.requset 请求参数前缀 request.xx.xx requestAsName 可替换前缀名
     * 指定前缀 key值 区分 请求参数和返回值
     * @return
     */
    String cacheName() default "";
    String condition() default "true";

    String key() default "";
    String value() default "";
    String requestAsName() default "request";
    String responseAsName() default "response";
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    long timeout() default 0;
    //注此功能 提供replaceKey抽象方法 自行实现
    //开启替换功能 默认不开启
    boolean openReplace() default false;
    // 替换key中 : 符号 替换key 中的 ":" 为 ":"  0 为原值 1为替换值
    String [] replaceSplicingList() default {":",":"};

    /**
     * 是否为hash类型
     * @return
     */
    boolean isHash() default false;

    // 是否随机时间
    boolean random() default false;
    // 随机时间范围 0~1 ==> [0,1)
    String randomRange() default "0~1";
}
