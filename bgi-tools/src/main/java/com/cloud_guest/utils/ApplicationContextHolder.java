package com.cloud_guest.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.BgiToolsApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author yan
 * @Date 2026/2/27 21:49:23
 * @Description
 */
// 1. 先创建一个工具类保存上下文和启动参数
@Component
@Slf4j
public class ApplicationContextHolder {

    private static ConfigurableApplicationContext context;
    private static String[] args;

    public static void setContext(ConfigurableApplicationContext ctx, String[] startupArgs) {
        context = ctx;
        args = startupArgs;
    }

    public static void restart() {
        Thread restartThread = new Thread(() -> {
            try {
                String active = SpringUtil.getBean(Environment.class).getProperty("spring.profiles.active", String.class);
                Thread.sleep(1000);  // 可选：缓冲时间

                // 1. 关闭旧上下文（触发优雅关闭）
                if (context != null && context.isRunning()) {
                    context.close();
                }

                // 2. 显式创建 SpringApplication 并强制设为 SERVLET 类型
                SpringApplication application = new SpringApplication(BgiToolsApplication.class);  // YourApplication 换成你的 @SpringBootApplication 类
                application.setWebApplicationType(WebApplicationType.SERVLET);  // ← 关键这一行！

                // 可选：如果有其他配置（如 profiles、banner 等），在这里设置
                application.setAdditionalProfiles(active);
                // application.setBannerMode(Banner.Mode.OFF);

                // 3. 重新 run
                ConfigurableApplicationContext newContext = application.run(args);

                // 更新 holder
                setContext(newContext, args);

                log.info("应用已成功重启");

            } catch (Exception e) {
                log.error("重启失败: " + e.getMessage());
                e.printStackTrace();
                // 可选：System.exit(1); 或其他处理
            }
        });

        restartThread.setDaemon(false);
        restartThread.start();
    }
}