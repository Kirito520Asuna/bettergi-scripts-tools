package com.cloud_guest.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.redis.service.RedisService;
import com.cloud_guest.service.DataBackupRecoveryService;
import com.cloud_guest.utils.LocalCacheUtils;
import com.cloud_guest.utils.ModeUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/3/15 22:16:42
 * @Description
 */
@Slf4j
@Service
public class DataBackupRecoveryServiceImpl implements DataBackupRecoveryService {
    @Override
    public Map<String, Object> backup() {
        Map<String, Object> map = Maps.newLinkedHashMap();
        if (ModeUtil.isLocal()) {
            Map<String, Object> cacheMap = LocalCacheUtils.getLocalCacheMap();
            map.putAll(cacheMap);
        } else if (ModeUtil.isRedis()) {
            RedisService bean = SpringUtil.getBean(RedisService.class);
            Collection<String> keys = bean.keys(KeyConstants.redis_file_json_key + "*");
            log.debug("{}", keys);
            keys.forEach(key -> {
                Object o = bean.get(key);

                if (key.startsWith(KeyConstants.redis_file_json_key)) {
                    key = key.replace(KeyConstants.redis_file_json_key, "");
                }

                map.put(key, o);
            });
        }
        List<String> exBackupList = KeyConstants.ex_backup_list;
        // 先收集需要移除的 key
        List<String> keysToRemove = map.keySet().stream()
                .filter(key -> exBackupList.stream()
                        .anyMatch(exKey -> key.contains(exKey)))
                .collect(Collectors.toList());
        // 然后移除
        keysToRemove.forEach(map::remove);
        return map;
    }

    @Override
    public boolean recovery(Map<String, Object> map) {
        Map<String, Object> hashMap = Maps.newLinkedHashMap();
        hashMap.putAll(map);
        if (ModeUtil.isLocal()) {
            hashMap.forEach((k, v) -> LocalCacheUtils.put(k, v));
        } else if (ModeUtil.isRedis()) {
            RedisService bean = SpringUtil.getBean(RedisService.class);
            hashMap.forEach((k, v) -> {
                if (!k.startsWith(KeyConstants.redis_file_json_key)) {
                    k = KeyConstants.redis_file_json_key + k;
                }
                bean.save(k, v);
            });
        }
        return true;
    }
}
