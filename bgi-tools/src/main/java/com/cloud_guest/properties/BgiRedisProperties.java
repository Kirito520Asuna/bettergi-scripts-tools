package com.cloud_guest.properties;

import com.cloud_guest.redis.config.RedisConfiguration;
import lombok.Data;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author yan
 * @Date 2026/2/26 22:04:29
 * @Description
 */
@Component
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class BgiRedisProperties  {
    private RedisConfiguration.RedisMode mode = RedisConfiguration.RedisMode.none;
    private String url;
    private String host = "localhost";
    private int port = 6379;
    private int database = 0;

    private RedisProperties.Sentinel sentinel;
    private RedisProperties.Cluster cluster;

    private String username;
    private String password;
}
