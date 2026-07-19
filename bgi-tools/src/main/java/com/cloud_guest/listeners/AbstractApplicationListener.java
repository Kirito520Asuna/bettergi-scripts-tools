package com.cloud_guest.listeners;

import com.cloud_guest.aop.bean.AbsBean;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
/**
 * @Author yan
 * @Date 2026/7/20 0:23:03
 * @Description
 */
public interface AbstractApplicationListener<E extends ApplicationEvent> extends ApplicationListener<E>, AbsBean {
    @PostConstruct
    default void init() {
        AbsBean.super.init();
    }
    @PreDestroy
    default void destroy() {
        AbsBean.super.destroy();
    }
    @Override
    default void onApplicationEvent(E event) {

    }

    @Override
    default boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
