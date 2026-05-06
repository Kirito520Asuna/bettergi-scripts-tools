package com.cloud_guest.config;

import com.cloud_guest.filter.CorsRequestFilter;
import com.cloud_guest.filter.TraceFilter;
import com.cloud_guest.properties.cors.CorsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author yan
 * @Date 2026/2/2 15:08:35
 * @Description
 */
@Configuration
public class FilterConfig {
    @Bean
    @ConditionalOnBean(CorsProperties.class)
    @ConditionalOnMissingBean(CorsRequestFilter.class)
    public CorsRequestFilter corsRequestFilter() {
        return new CorsRequestFilter();
    }
    @Bean
    public TraceFilter traceFilter() {
        return new TraceFilter();
    }
}
