package com.cloud_guest.properties.bean;



import com.cloud_guest.aop.bean.AbsBean;

import javax.annotation.PostConstruct;

/**
 * @Author yan
 * @Date 2025/3/10 19:52:45
 * @Description
 */
public interface BeanProperties extends AbsBean {
    @Override
    @PostConstruct
    default void init() {
        log().debug("[init]-[Properties]-[Config]::[{}]",getAClassName());
    }
}
