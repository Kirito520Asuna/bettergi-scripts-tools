package com.cloud_guest.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/2/6 15:50:40
 * @Description
 */
@Slf4j
@Component
public class LocalCacheUtils {
    // 本地缓存映射
    private static final Map<String, Object> LOCAL_CACHE_MAP = Maps.newConcurrentMap();
    @Value("${local.cache.json-file-path:../../.././cache/local-cache.json}")
    private String localCacheJsonFilePath;
    @Resource
    private Environment env;

    @PostConstruct
    public void init() {
        String redisMode = env.getProperty("spring.redis.mode", "none");
        if (!"none".equals(redisMode)) {
            log.info("redis模式，不加载本地缓存");
            return;
        }
        //加载缓存
        Map<String, Object> map = null;
        try {
            map = JSONUtil.toBean(
                    FileUtil.readUtf8String(localCacheJsonFilePath),
                    Map.class
            );
        } catch (Exception e) {
            log.error("加载本地缓存失败:{}", e.getMessage());
        }
        if (map != null) {
            LOCAL_CACHE_MAP.putAll(map);
        }
    }

    @PreDestroy
    public void destroy() {
        String redisMode = env.getProperty("spring.redis.mode", "none");
        if (!"none".equals(redisMode)) {
            log.info("redis模式，不保存本地缓存");
            return;
        }
        //保存缓存
        String localCacheJson = JSONUtil.toJsonStr(LOCAL_CACHE_MAP);
        FileUtil.writeUtf8String(localCacheJson, localCacheJsonFilePath);
    }

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

    /**
     * 从缓存中移除指定键的列表
     *
     * @param keys
     */
    public static List<Map<String, Object>> getList(List<String> keys) {
        return keys.stream().map(key -> {
            Map<String, Object> hashMap = Maps.newLinkedHashMap();
            hashMap.put(key, LOCAL_CACHE_MAP.get(key));
            return hashMap;
        }).collect(Collectors.toList());
    }

    /**
     * 从缓存中移除指定键的列表
     *
     * @param keys
     */
    public static void removeList(List<String> keys) {
        keys.forEach(LocalCacheUtils::remove);
    }
}