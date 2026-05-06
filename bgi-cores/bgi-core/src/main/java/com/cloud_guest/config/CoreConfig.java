package com.cloud_guest.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @Author yan
 * @Date 2026/5/6 17:08:22
 * @Description
 */
@Configuration
public class CoreConfig {

    @Bean(name = "mdcTaskExecutor")
    public ThreadPoolTaskExecutor mdcTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(5);
        // 最大线程数
        executor.setMaxPoolSize(10);
        // 队列容量
        executor.setQueueCapacity(200);
        // 线程名前缀，方便日志排查
        executor.setThreadNamePrefix("async-mdc-");

        // ★ 装饰者：父线程的 MDC 上下文自动复制到子线程
        executor.setTaskDecorator(runnable -> {
            // 获取父线程的 MDC 上下文快照
            Map<String, String> parentContext = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    // 子线程执行前，设置父线程的 MDC
                    if (parentContext != null) {
                        MDC.setContextMap(parentContext);
                    }
                    runnable.run();
                } finally {
                    // 子线程执行完毕，清空 MDC（避免线程池复用污染）
                    MDC.clear();
                }
            };
        });

        executor.initialize();
        return executor;
    }
}
