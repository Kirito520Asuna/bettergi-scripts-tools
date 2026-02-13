package com.cloud_guest.redis.config;

import com.cloud_guest.redis.abs.ban.BanManager;
import com.cloud_guest.redis.abs.config.AbsRedissonConfig;
import com.cloud_guest.redis.ban.BanConfiguration;
import com.cloud_guest.redis.ban.SimpleBanManager;
import com.cloud_guest.redis.service.RedisService;
import com.cloud_guest.redis.service.impl.SimpleRedisService;

import javax.annotation.Resource;

import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * @Author yan
 * @Date 2024/5/23 0023 10:02
 * @Description
 */
@Configuration
//@ConditionalOnMissingBean(DisabledRedisConfig.class)
@ConditionalOnBean(RedisConfiguration.class)
@EnableAspectJAutoProxy
@EnableCaching // 开启Spring Redis Cache，使用注解驱动缓存机制
//@ImportAutoConfiguration({  // 手动导入官方配置（或你的自定义）
//        // 加其他需要的，如 RedisReactiveAutoConfiguration 如果要响应式
//        org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
//        org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class,
//        org.redisson.spring.starter.RedissonAutoConfigurationV2.class,
//        org.redisson.spring.starter.RedissonAutoConfiguration.class,
//})
public class RedissonConfig implements AbsRedissonConfig {

    @Resource
    @Lazy
    private RedisConfiguration configuration;
    @Resource
    @Lazy
    private Environment env;
    @Bean
    @ConditionalOnBean(RedisConfiguration.class)
    public RedisAutoConfiguration redisAutoConfiguration() {
        return new RedisAutoConfiguration();
    }

    @Bean
    @ConditionalOnBean(RedisConfiguration.class)
    public RedisRepositoriesAutoConfiguration redisRepositoriesAutoConfiguration() {
        return new RedisRepositoriesAutoConfiguration();
    }

    @Bean
    @ConditionalOnBean(RedisConfiguration.class)
    public RedissonAutoConfigurationV2 redissonAutoConfigurationV2() {
        return new RedissonAutoConfigurationV2();
    }

    @Bean
    @ConditionalOnBean(RedisConfiguration.class)
    public RedissonAutoConfiguration redissonAutoConfiguration() {
        return new RedissonAutoConfiguration();
    }
    @Bean
    @Lazy
    public RedissonClient redissonClient() {
        return initRedissonClient();
    }

    public RedissonClient initRedissonClient() {
        return AbsRedissonConfig.super.getRedissonClient(configuration);
    }

    @Bean
    @Lazy
    @Primary
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        return initRedisTemplate(connectionFactory);
    }


    @Bean
    @Lazy
    @ConditionalOnExpression("${ip.enable:false}")
    @ConditionalOnMissingBean(BanConfiguration.class)
    public BanConfiguration banConfiguration() {
        return new BanConfiguration();
    }

    @Bean
    @Lazy
    @ConditionalOnBean({RedissonClient.class, BanConfiguration.class})
    @ConditionalOnMissingBean(BanManager.class)
    public BanManager banManager() {
        return new SimpleBanManager();
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean(RedisService.class)
    public RedisService redisService() {
        log.debug("redisService init");
        return new SimpleRedisService();
    }
}
