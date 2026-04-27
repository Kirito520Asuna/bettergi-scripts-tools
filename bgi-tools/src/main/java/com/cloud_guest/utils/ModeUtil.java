package com.cloud_guest.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.aop.bean.AbsBean;
import com.cloud_guest.redis.config.RedisConfiguration;
import com.cloud_guest.utils.object.ObjectUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @Author yan
 * @Date 2026/2/28 20:47:05
 * @Description
 */
@Slf4j
@Component
@Data
public class ModeUtil implements AbsBean {

    private RedisConfiguration.RedisMode mode = RedisConfiguration.RedisMode.none;
    private static Boolean IS_SQLITE_DB = null;
    @Override
    public void init() {
        AbsBean.super.init();
        //String value = RedisConfiguration.RedisMode.none.name();
        String value = SpringUtil.getBean(Environment.class).getProperty("spring.redis.mode");

        try {
            mode = RedisConfiguration.RedisMode.valueOf(value);
        } catch (Exception e) {
            mode = RedisConfiguration.RedisMode.none;
        }
    }

    public static ModeUtil getModeUtil() {
        //ModeUtil bean = new ModeUtil();
        ModeUtil bean = SpringUtil.getBean(ModeUtil.class);
        return bean;
    }

    public static void setSqlite(Boolean sqlite) {
        if (IS_SQLITE_DB == null) {
            IS_SQLITE_DB = sqlite;
        }
    }

    public static Boolean isSqlite() {
        return ObjectUtils.defaultIfEmpty(IS_SQLITE_DB, false);
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
