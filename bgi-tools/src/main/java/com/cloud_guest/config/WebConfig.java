package com.cloud_guest.config;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/10 15:03:59
 * @Description
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        addUiResource(registry);
    }

    private static void addUiResource(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/ui/**", "/ui/","/ui")
                .addResourceLocations("classpath:/static/ui/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(
                            String resourcePath,
                            Resource location
                    ) throws IOException {
                        // 1️⃣ 让父类先查找真实资源
                        Resource resource = super.getResource(resourcePath, location);

                        if (resource != null) {
                            return resource;
                        }

                        // 2️⃣ 只对非静态文件 fallback
                        if (!resourcePath.contains(".")) {
                            return super.getResource("index.html", location);
                        }

                        return null;
                    }
                });
    }
}