package com.cloud_guest.redis.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * @Author yan
 * @Date 2026/2/6 13:40:13
 * @Description
 */
@Configuration
@ConditionalOnProperty(
        prefix = "spring.redis",
        name = "mode",
        havingValue = "none",
        matchIfMissing = true // 没有配置就加载
)
@EnableAutoConfiguration(exclude = {
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class,
        RedissonAutoConfigurationV2.class,
})
@Slf4j
public class DisabledRedisConfig {
    // 空类，或放一些 none 模式下的 mock bean（可选）
    @PostConstruct
    public void init() {
        log.debug("Redis is disabled");
    }
}
