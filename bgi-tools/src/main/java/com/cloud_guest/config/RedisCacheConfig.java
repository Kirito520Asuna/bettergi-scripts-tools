package com.cloud_guest.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Author yan
 * @Date 2026/2/6 12:55:28
 * @Description
 */
@Configuration
@ConditionalOnProperty(
        prefix = "run.db",
        name = "mode",
        havingValue = "redis",           // 只在 mode=redis 时生效
        matchIfMissing = false           // 属性不存在或值不对 → 不生效
)
@Import(RedisAutoConfiguration.class)   // 导入 Spring Boot 的 Redis 自动配置
public class RedisCacheConfig {
    //@Bean
    //public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory factory) {
    //    RedisTemplate<String, Object> template = new RedisTemplate<>();
    //    template.setConnectionFactory(factory);
    //    template.setKeySerializer(new StringRedisSerializer());
    //    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    //    template.afterPropertiesSet();
    //    return template;
    //}
}
