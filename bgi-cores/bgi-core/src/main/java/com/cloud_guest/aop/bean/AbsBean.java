package com.cloud_guest.aop.bean;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


/**
 * @Author yan
 * @Date 2025/5/18 15:39:46
 * @Description
 */
public interface AbsBean {
    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    class LogBean {
        private Logger logger;
        private Class<?> aClass;
    }

    @JsonIgnore
    default LogBean getLogBean() {
        LogBean logBean = new LogBean()
                .setAClass(this.getClass())
                .setLogger(LoggerFactory.getLogger(this.getClass()));
        return logBean;
    }

    default <T> T getBean(Class<T> clazz) {
        T bean = SpringUtil.getBean(clazz);
        return bean;
    }

    @JsonIgnore
    default Logger getLogger() {
        return getLogBean().getLogger();
    }

    @JsonIgnore
    default Logger log() {
        return getLogger();
    }

    @JsonIgnore
    default Class<?> getAClass() {
        return getLogBean().getAClass();
    }

    @JsonIgnore
    default String getAClassName() {
        return StrUtil.subBefore(getAClass().getName(), "$", false);
    }

    /**
     * 初始化
     */
    @PostConstruct
    default void init() {
        log().info("[init]::[{}]: ", getAClassName());
    }

    /**
     * 销毁
     */
    @PreDestroy
    default void destroy() {
        log().info("[destroy]::[{}]", getAClassName());
    }

}
