package com.cloud_guest.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author yan
 * @Date 2026/2/13 17:20:18
 * @Description
 */
@Configuration
@ConditionalOnClass(RestartEndpoint.class)
public class RestartEndpointConfig {
    @Bean
    @ConditionalOnMissingBean
    public RestartEndpoint restartEndpoint() {
        return new RestartEndpoint();
    }
}
