package com.cloud_guest.utils;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @Author yan
 * @Date 2026/2/6 15:50:40
 * @Description
 */
public class LocalCacheUtils {
    // 本地缓存映射
    private static final Map<String, Object> LOCAL_CACHE_MAP = Maps.newConcurrentMap();

    /**
     * 判断缓存中是否包含指定键
     *
     * @param key 键
     * @return 如果包含返回 true，否则返回 false
     */
    public static boolean has(String key) {
        return LOCAL_CACHE_MAP.containsKey(key);
    }

    /**
     * 向缓存中添加键值对
     *
     * @param key   键
     * @param value 值
     */
    public static void put(String key, Object value) {
        LOCAL_CACHE_MAP.put(key, value);
    }

    /**
     * 从缓存中获取值
     *
     * @param key 键
     * @return 对应的值，如果不存在返回 null
     */
    public static Object get(String key) {
        return LOCAL_CACHE_MAP.get(key);
    }

    /**
     * 从缓存中移除指定键
     *
     * @param key 键
     */
    public static void remove(String key) {
        LOCAL_CACHE_MAP.remove(key);
    }
}