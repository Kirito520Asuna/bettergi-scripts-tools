package com.cloud_guest.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.Cache;
import com.cloud_guest.redis.service.RedisService;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.utils.LocalCacheUtils;
import com.cloud_guest.utils.object.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
    //public static final String  KeyConstants.redis_file_json_key = "redis:file:json:";

    @Override
    public boolean removeList(List<String> ids) {
        List<String> parentKeys = ids.stream().collect(Collectors.toList());
        if ("none".equals(redisMode)) {
            LocalCacheUtils.removeList(ids);
        } else {
            RedisService bean = SpringUtil.getBean(RedisService.class);
            ids = ids.stream()
                    .map(id -> KeyConstants.redis_file_json_key + id)
                    .collect(Collectors.toList());
            bean.delList(ids);
        }

        parentKeys.stream().forEach(id -> {
            if (!id.contains("ALL")) {
                String parentKey = id.substring(0, id.lastIndexOf(":"));
                removeId(parentKey, id);
            }
        });

        return true;
    }

    @Override
    public boolean save(String id, String json) {
        String parentKey = "";
        Cache<String> cache = new Cache<>();
        cache.setType("json");
        cache.setData(json);
        if ("none".equals(redisMode)) {
            parentKey = id.substring(0, id.lastIndexOf(":"));
            LocalCacheUtils.put(id, JSONUtil.toJsonStr(cache));
        } else {
            RedisService bean = SpringUtil.getBean(RedisService.class);
            String key = KeyConstants.redis_file_json_key + id;
            bean.save(key, JSONUtil.toJsonStr(cache));
            parentKey = key.substring(0, key.lastIndexOf(":"));
        }
        if (!id.contains("ALL")) {
            saveId(parentKey, id);
        }
        return true;
    }

    @Override
    public boolean removeId(String key, String id) {
        Set<String> hashSet = new LinkedHashSet<>();
        String ids;
        if ("none".equals(redisMode)) {
            ids = (String) LocalCacheUtils.get(key);
        } else {
            RedisService bean = SpringUtil.getBean(RedisService.class);
            ids = (String) bean.get(key);
        }

        if (StrUtil.isNotBlank(ids)) {
            if (JSONUtil.isTypeJSONArray(ids)) {
                // 是数组
                JSONUtil.toList(ids, String.class).forEach(hashSet::add);
            } else {
                // 不是数组
                hashSet.add(ids);
            }
        }

        hashSet.remove(id);

        if ("none".equals(redisMode)) {
            LocalCacheUtils.put(key, JSONUtil.toJsonStr(hashSet));
        } else {
            RedisService bean = SpringUtil.getBean(RedisService.class);
            bean.save(key, JSONUtil.toJsonStr(hashSet.stream().collect(Collectors.toList())));
        }
        return true;
    }

    @Override
    public boolean saveId(String key, String id) {
        Set<String> hashSet = new LinkedHashSet<>();
        String ids;
        if ("none".equals(redisMode)) {
            ids = (String) LocalCacheUtils.get(key);
        } else {
            RedisService bean = SpringUtil.getBean(RedisService.class);
            ids = (String) bean.get(key);
        }

        if (StrUtil.isNotBlank(ids)) {
            if (JSONUtil.isTypeJSONArray(ids)) {
                // 是数组
                JSONUtil.toList(ids, String.class).forEach(hashSet::add);
            } else {
                // 不是数组
                hashSet.add(ids);
            }
        }

        hashSet.add(id);

        if ("none".equals(redisMode)) {
            LocalCacheUtils.put(key, JSONUtil.toJsonStr(hashSet));
        } else {
            RedisService bean = SpringUtil.getBean(RedisService.class);
            bean.save(key, JSONUtil.toJsonStr(hashSet.stream().collect(Collectors.toList())));
        }
        return true;
    }

    @Override
    public Cache<String> find(String id) {
        String o;
        if ("none".equals(redisMode)) {
            o = (String) LocalCacheUtils.get(id);
        } else {
            String key = KeyConstants.redis_file_json_key + id;
            RedisService bean = SpringUtil.getBean(RedisService.class);
            o = (String) bean.get(key);
        }
        Cache<String> cache = JSONUtil.toBean(o, Cache.class);
        return cache;
    }
    @Override
    public String findById(String id) {
        Cache<String> cache = find(id);
        return cache.getData();
    }
    @Override
    public <T> T find(String id, Class<T> clazz) {
        Cache<String> cache = find(id);
        String data = cache.getData();
        if (StrUtil.isNotBlank(data)) {
            T t = JSONUtil.toBean(data, clazz);
            return t;
        }
        return null;
    }
    @Override
    public <T> List<T> findAll(String key, Class<T> clazz) {
        List<T> list;
        Set<String> hashSetIds = new LinkedHashSet<>();
        String ids;
        if ("none".equals(redisMode)) {
            ids = (String) LocalCacheUtils.get(key);
        } else {
            RedisService bean = SpringUtil.getBean(RedisService.class);
            ids = (String) bean.get(key);
        }
        if (StrUtil.isNotBlank(ids)) {
            if (JSONUtil.isTypeJSONArray(ids)) {
                // 是数组
                JSONUtil.toList(ids, String.class).forEach(hashSetIds::add);
            } else {
                // 不是数组
                hashSetIds.add(ids);
            }
        }
        list = hashSetIds.stream().map(id -> find(id, clazz)).filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        return list;
    }
}
