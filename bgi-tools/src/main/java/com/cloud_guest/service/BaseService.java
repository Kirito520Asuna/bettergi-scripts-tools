package com.cloud_guest.service;


import cn.hutool.core.util.StrUtil;

/**
 * @Author yan
 * @Date 2026/3/30 17:47:12
 * @Description
 */
public interface BaseService {
    default String getSuffix() {
        return "";
    }
    default String buildId(String key) {
        return buildId(key, getSuffix());
    }

    default String buildId(String key, String suffix) {
        if (StrUtil.isNotBlank(suffix) && !key.startsWith(suffix)) {
            key = suffix + key;
        }
        return key;
    }
}
