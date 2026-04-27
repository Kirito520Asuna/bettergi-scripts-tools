//package com.cloud_guest.mp.config;
//
//
//import com.github.pagehelper.PageInterceptor;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.Properties;
//
///**
// * PageHelper 与 Spring Boot 3.2.x 兼容性修复配置
// */
//@Configuration
//@ConditionalOnClass(PageInterceptor.class)
//public class PageHelperConfig {
//
//    @Bean
//    public PageInterceptor pageInterceptor() {
//        PageInterceptor interceptor = new PageInterceptor();
//        Properties properties = new Properties();
//        // 设置合理属性避免 factoryBeanObjectType 冲突
//        properties.setProperty("rowBoundsWithCount", "true");
//        properties.setProperty("reasonable", "false");
//        properties.setProperty("supportMethodsArguments", "true");
//        interceptor.setProperties(properties);
//        return interceptor;
//    }
//}
