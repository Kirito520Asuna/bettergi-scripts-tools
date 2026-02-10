package com.cloud_guest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * @Author yan
 * @Date 2026/2/10 15:03:59
 * @Description
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(
                            String resourcePath,
                            Resource location
                    ) throws IOException {

                        Resource requested = location.createRelative(resourcePath);
                        // 静态资源存在 → 正常返回
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        // 否则 → 返回 index.html（Vue 接管路由）
                        return location.createRelative("index.html");
                    }
                });
    }
}