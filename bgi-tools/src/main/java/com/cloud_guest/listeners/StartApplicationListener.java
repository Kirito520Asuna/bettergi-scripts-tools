package com.cloud_guest.listeners;

import com.cloud_guest.runner.RunnerTools;
import javax.sql.DataSource;
import jakarta.annotation.Resource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @Author yan
 * @Date 2026/7/20 0:29:06
 * @Description
 */
@Component
public class StartApplicationListener implements AbstractApplicationListener<ContextRefreshedEvent> {
    @Override
    public void init() {
        AbstractApplicationListener.super.init();
        RunnerTools.init();
    }

    @Override
    public void destroy() {
        AbstractApplicationListener.super.destroy();
        RunnerTools.destroy();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //bean 初始化完成
    }

    @Override
    public boolean supportsAsyncExecution() {
        return AbstractApplicationListener.super.supportsAsyncExecution();
    }
}
