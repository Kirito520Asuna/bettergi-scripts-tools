package com.cloud_guest.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.abs.service.AbstractKeyService;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.key.KeyInfo;
import com.cloud_guest.service.BaseService;
import com.cloud_guest.service.CacheService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author yan
 * @Date 2026/3/31 20:01:10
 * @Description
 */
@Service
public class KeyServiceImpl implements AbstractKeyService, BaseService {
    @Resource
    private CacheService cacheService;

    @Override
    public String getSuffix() {
        return KeyConstants.key;
    }

    @Override
    public boolean saveKeyInfo(KeyInfo keyInfo) {
        String id = buildId(keyInfo.getId());
        return cacheService.save(id, JSONUtil.toJsonStr(keyInfo), keyInfo.getValidTimeInterval(), TimeUnit.MILLISECONDS);
    }

    @Override
    public KeyInfo getKeyInfoById(String keyId) {
        String id = buildId(keyId);
        return cacheService.find(id, KeyInfo.class);
    }

    @Override
    public boolean remove(List<String> keyIds) {
        keyIds.stream().forEach(id -> buildId(id));
        return cacheService.removeList(keyIds);
    }

    @Override
    public List<KeyInfo> getAllExpiredKeyInfoList() {
        List<String> uidList = new ArrayList<>();
        String key = getSuffix().substring(0, getSuffix().lastIndexOf(":"));
        String uid_all = cacheService.findValueByKey(key);
        if (StrUtil.isNotBlank(uid_all)) {
            if (JSONUtil.isTypeJSONArray(uid_all)) {
                JSONUtil.toList(uid_all, String.class).stream().forEach(uidList::add);
            } else {
                uidList.add(uid_all);
            }
        }
        List<KeyInfo> list = uidList.stream().map(this::getKeyInfoById).toList();
        list = list.stream().filter(keyInfo -> keyInfo.getCreateTime() + keyInfo.getValidTimeInterval() < System.currentTimeMillis()).toList();
        return list;
    }
}
