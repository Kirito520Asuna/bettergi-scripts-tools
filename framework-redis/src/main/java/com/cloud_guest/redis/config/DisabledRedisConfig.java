package com.cloud_guest.redis.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
        matchIfMissing = true
)
public class DisabledRedisConfig {
    // 空类，或放一些 none 模式下的 mock bean（可选）
}
