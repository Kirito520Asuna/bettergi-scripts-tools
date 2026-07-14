package com.cloud_guest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author yan
 * @Date 2026/7/14 21:00:06
 * @Description
 */
/**
 * 线程池配置
 * 启用异步支持，并根据 CPU 核心数动态设置线程池参数
 */
@Configuration
@EnableAsync  // 开启 Spring 异步方法执行能力
public class ThreadPoolTaskExecutorConfig {
    @Bean
    @Primary
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // ---------- 核心参数 ----------
        int cpuCores = Runtime.getRuntime().availableProcessors(); // 获取逻辑核心数
        // 核心线程数：常驻线程数，建议设为 CPU 核心数
        executor.setCorePoolSize(cpuCores);
        // 最大线程数：允许同时运行的最大线程数，一般为核心数的 2 倍
        executor.setMaxPoolSize(cpuCores * 2);
        // 队列容量：用于存放等待执行的任务，满了之后会创建新线程（直到 maxPoolSize）
        executor.setQueueCapacity(1000);
        // 线程名前缀，方便在日志中识别
        executor.setThreadNamePrefix("async-exec-");
        // ---------- 拒绝策略 ----------
        // 当线程池和队列都已满时，由调用者所在的线程执行该任务（避免丢失任务）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 优雅停机时等待任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 最多等待 60 秒
        executor.setAwaitTerminationSeconds(60);
        // 初始化线程池
        executor.initialize();
        return executor;
    }
}
