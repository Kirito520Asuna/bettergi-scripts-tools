//package com.cloud_guest.config;
//
//import com.cloud_guest.runner.DatabaseInitRunner;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.ResourceLoader;
//
//import javax.sql.DataSource;
//
///**
// * 数据库初始化自动配置
// * 可通过 spring.datasource.init.enabled=false 关闭
// */
//@Configuration
//public class DatabaseInitConfig {
//
//    @Bean
//    @ConditionalOnProperty(prefix = "spring.datasource.init", name = "enabled", havingValue = "true", matchIfMissing = true)
//    public DatabaseInitRunner databaseInitRunner(DataSource dataSource, ResourceLoader resourceLoader) {
//        return new DatabaseInitRunner(dataSource, resourceLoader);
//    }
//}