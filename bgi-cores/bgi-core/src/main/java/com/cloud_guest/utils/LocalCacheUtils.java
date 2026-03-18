package com.cloud_guest.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    //@Value("${local.cache.json-file-path:../../.././cache/local-cache.json}")
    //private String localCacheJsonFilePath;
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
                    FileUtil.readUtf8String(getLocalCacheJsonFilePath()),
                    Map.class
            );
        } catch (Exception e) {
            log.error("加载本地缓存失败:{}", e.getMessage());
        }
        if (map != null) {
            LOCAL_CACHE_MAP.putAll(map);
            log.info("加载本地缓存成功");
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
        writeLocal(getLocalCacheJsonFilePath());
        log.info("保存本地缓存成功");
    }
    public static Map<String, Object> getLocalCacheMap(){
        return LOCAL_CACHE_MAP;
    }
    public static String getLocalCacheJsonFilePath() {
        Environment env = SpringUtil.getBean(Environment.class);
        String localCacheJsonFilePath  = env.getProperty("local.cache.json-file-path", "../../.././cache/local-cache.json");
        return localCacheJsonFilePath;
    }

    public static void writeLocal(String localCacheJsonFilePath) {
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
        writeLocal(getLocalCacheJsonFilePath());
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
        writeLocal(getLocalCacheJsonFilePath());
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


    /**
     * 获取指定前缀的所有 key
     *
     * @param prefix 前缀，如 "user:"
     * @return key 集合
     */
    public static Set<String> getKeysByPrefix(String prefix) {
        if (StrUtil.isBlank(prefix)) {
            return Collections.emptySet();
        }

        return LOCAL_CACHE_MAP.keySet().stream()
                .filter(key -> key.startsWith(prefix))
                .collect(Collectors.toSet());
    }

    /**
     * 获取指定前缀的所有数据（Map 形式）
     *
     * @param prefix 前缀，如 "task:"
     * @return Map<key, value>
     */
    public static Map<String, Object> getDataMapByPrefix(String prefix) {
        if (StrUtil.isBlank(prefix)) {
            return Collections.emptyMap();
        }

        Map<String, Object> resultMap = Maps.newLinkedHashMap();
        LOCAL_CACHE_MAP.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .forEach(entry -> resultMap.put(entry.getKey(), entry.getValue()));

        return resultMap;
    }

    /**
     * 获取指定前缀的所有数据（List 形式）
     *
     * @param prefix 前缀，如 "task:"
     * @return 包含 key 和 value 的 Map 列表
     */
    public static List<Map<String, Object>> getDataListByPrefix(String prefix) {
        if (StrUtil.isBlank(prefix)) {
            return Collections.emptyList();
        }

        return LOCAL_CACHE_MAP.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .map(entry -> {
                    Map<String, Object> map = Maps.newLinkedHashMap();
                    map.put("key", entry.getKey());
                    map.put("value", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 统计指定前缀的数据数量
     *
     * @param prefix 前缀
     * @return 数量
     */
    public static Long countByPrefix(String prefix) {
        if (StrUtil.isBlank(prefix)) {
            return 0L;
        }

        return LOCAL_CACHE_MAP.keySet().stream()
                .filter(key -> key.startsWith(prefix))
                .count();
    }

    /**
     * 删除指定前缀的所有数据
     *
     * @param prefix 前缀
     * @return 删除的数量
     */
    public static Long deleteByPrefix(String prefix) {
        if (StrUtil.isBlank(prefix)) {
            return 0L;
        }

        List<String> keysToDelete = LOCAL_CACHE_MAP.keySet().stream()
                .filter(key -> key.startsWith(prefix))
                .collect(Collectors.toList());

        keysToDelete.forEach(LOCAL_CACHE_MAP::remove);

        // 保存到文件
        writeLocal(getLocalCacheJsonFilePath());

        log.info("删除前缀为 {} 的数据，删除数量：{}", prefix, keysToDelete.size());
        return (long) keysToDelete.size();
    }

    /**
     * 检查是否存在指定前缀的数据
     *
     * @param prefix 前缀
     * @return true/false
     */
    public static boolean existsByPrefix(String prefix) {
        if (StrUtil.isBlank(prefix)) {
            return false;
        }

        return LOCAL_CACHE_MAP.keySet().stream()
                .anyMatch(key -> key.startsWith(prefix));
    }
}