package com.cloud_guest.redis.config;

import cn.hutool.core.collection.CollUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2024/5/23 0023 10:41
 * @Description
 */
@Configuration
@ConditionalOnMissingBean(DisabledRedisConfig.class)  // 反向：当 none 时不加载
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Slf4j

public class RedisConfiguration {
    @Value("${spring.data.redis.host:-127.0.0.1}")
    private String redisHost;

    @Value("${spring.data.redis.port:-6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:#{null}}")
    private String redisPassword;

    @Value("${spring.data.redis.timeout:#{null}}")
    private Integer redisTimeout;

    @Value("${spring.data.redis.mode:single}")
    private String redisMode; // 可配置为 "single" 或 "cluster"
    @Value("${spring.data.redis.cluster.nodes:redis://127.0.0.1:6379}")
    private String clusterNodeStr;
    @Value("${spring.data.redis.cluster.nodes:redis://127.0.0.1:6379}")
    private List<String> clusterNodes = CollUtil.newArrayList();
    //@Resource(shareable = false)
    //private RedisProperties redisProperties;

    public enum RedisMode {
        none, single, sentinel, cluster;
    }

    public RedisMode getRedisModeEnum() {
        RedisMode mode = RedisMode.single;
        try {
            mode = RedisMode.valueOf(redisMode);
        } catch (IllegalArgumentException e) {
            log.error("redis mode is illegal");
        }
        return mode;
    }

    public List<String> getAddresses() {
        String template = "redis://%s:%s";
        String format = String.format(template, redisHost, redisPort);

        List<String> addresses = new ArrayList<>();
        RedisMode mode = RedisMode.single;
        try {
            mode = RedisMode.valueOf(redisMode);
        } catch (IllegalArgumentException e) {
            log.error("redis mode is illegal");
        }

        switch (mode) {
            case cluster:
            case sentinel:
                List<String> collect = Arrays.stream(clusterNodeStr.replace(" ", "")
                        .replace("，", ",")
                        .split(",")).distinct().collect(Collectors.toList());
                addresses.addAll(collect);
                break;
            case single:// 单机模式 传递默认default
            default:
                addresses.add(format);
                break;
        }
        return addresses;
    }

}
