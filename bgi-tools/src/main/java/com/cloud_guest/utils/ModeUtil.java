package com.cloud_guest.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.aop.bean.AbsBean;
import com.cloud_guest.redis.config.RedisConfiguration;
import lombok.Data;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @Author yan
 * @Date 2026/2/28 20:47:05
 * @Description
 */
@Component
@Data
public class ModeUtil implements AbsBean {

    private RedisConfiguration.RedisMode mode;

    @Override
    public void init() {
        AbsBean.super.init();
        String value = SpringUtil.getBean(Environment.class).getProperty("spring.redis.mode");
        try {
            mode = RedisConfiguration.RedisMode.valueOf(value);
        } catch (Exception e) {
            mode = RedisConfiguration.RedisMode.none;
        }
    }

    public static ModeUtil getModeUtil() {
        return SpringUtil.getBean(ModeUtil.class);
    }

    public static boolean isRedis() {
        ModeUtil modeUtil = getModeUtil();
        RedisConfiguration.RedisMode mode = modeUtil.getMode();
        return mode == RedisConfiguration.RedisMode.single || mode == RedisConfiguration.RedisMode.cluster || mode == RedisConfiguration.RedisMode.sentinel;
    }

    public static boolean isLocal() {
        ModeUtil modeUtil = getModeUtil();
        RedisConfiguration.RedisMode mode = modeUtil.getMode();
        return mode == RedisConfiguration.RedisMode.none;
    }
}
