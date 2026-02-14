package com.cloud_guest.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.domain.Cache;
import com.cloud_guest.redis.service.RedisService;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.utils.LocalCacheUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/2/6 16:01:48
 * @Description
 */
@Service
public class CacheServiceImpl implements CacheService {
    @Value("${spring.redis.mode:none}")
    private String redisMode;
    public static final String REDIS_FILE_JSON_KEY = "redis:file:json:";

    @Override
    public boolean delList(List<String> ids) {
        if ("none".equals(redisMode)) {
            LocalCacheUtils.removeList(ids);
        } else {
            RedisService bean = SpringUtil.getBean(RedisService.class);
            ids = ids.stream()
                    .map(id -> REDIS_FILE_JSON_KEY + id)
                    .collect(Collectors.toList());
            bean.delList(ids);
        }
        return true;
    }

    @Override
    public boolean save(String id, String json) {
        Cache<String> cache = new Cache<>();
        cache.setType("json");
        cache.setData(json);
        if ("none".equals(redisMode)) {
            LocalCacheUtils.put(id, JSONUtil.toJsonStr(cache));
        } else {
            RedisService bean = SpringUtil.getBean(RedisService.class);
            bean.save(REDIS_FILE_JSON_KEY + id, JSONUtil.toJsonStr(cache));
        }
        return true;
    }

    @Override
    public Cache<String> find(String id) {
        String o;
        if ("none".equals(redisMode)) {
            o = (String) LocalCacheUtils.get(id);
        } else {
            RedisService bean = SpringUtil.getBean(RedisService.class);
            o = (String) bean.get(REDIS_FILE_JSON_KEY + id);
        }
        Cache<String> cache = JSONUtil.toBean(o, Cache.class);
        return cache;
    }
}
