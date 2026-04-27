package com.cloud_guest.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.abs.service.AbstractKeyService;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.key.KeyInfo;
import com.cloud_guest.pojo.DbKV;
import com.cloud_guest.service.BaseService;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.service.DbKVService;
import com.cloud_guest.utils.IdUtils;
import com.cloud_guest.utils.object.ObjectUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @Author yan
 * @Date 2026/3/31 20:01:10
 * @Description
 */
@Service
public class KeyServiceImpl implements AbstractKeyService, BaseService {

    @Resource
    private DbKVService dbKVService;

    @Override
    public String getSuffix() {
        return KeyConstants.key;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveKeyInfo(KeyInfo keyInfo) {
        DbKV dbKV = new DbKV();
        dbKV.setKey(IdUtils.getNextIdStr());
        dbKV.setType(getSuffix());
        dbKV.setValue(JSONUtil.toJsonStr(keyInfo));
        dbKVService.save(dbKV);
        return dbKV.getId();
    }

    @Override
    public KeyInfo getKeyInfoById(String keyId) {
        Long id = Long.valueOf(keyId);
        return Optional.ofNullable(dbKVService.getById(id))
                .map(dbKV -> JSONUtil.toBean(dbKV.getValue(), KeyInfo.class))
                .orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(List<String> keyIds) {
        if (CollUtil.isEmpty(keyIds)) {
            return false;
        }

        List<Long> ids = keyIds.stream()
                .filter(ObjectUtils::isNotEmpty)
                .map(Long::valueOf)
                .toList();

        return !ids.isEmpty() && dbKVService.removeByIds(ids);
    }

    @Override
    public List<KeyInfo> getAllExpiredKeyInfoList() {
        // 使用 dbKVService 查询所有 key 记录
        List<DbKV> dbKVList = dbKVService.lambdaQuery()
                .eq(DbKV::getType, getSuffix())
                .list();

        // 转换为 KeyInfo 并过滤过期记录
        return dbKVList.stream()
                .map(dbKV -> JSONUtil.toBean(dbKV.getValue(), KeyInfo.class))
                .filter(keyInfo -> keyInfo.getCreateTime() + keyInfo.getValidTimeInterval() < System.currentTimeMillis())
                .toList();
    }
}
