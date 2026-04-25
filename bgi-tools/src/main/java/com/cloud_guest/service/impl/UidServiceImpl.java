package com.cloud_guest.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.domain.UidInfo;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.service.UidService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/3/30 17:39:54
 * @Description
 */
@Service
public class UidServiceImpl implements UidService {
    @Resource
    private CacheService cacheService;

    @Override
    public boolean removeList(List<String> ids) {
        ids=ids.stream().map(id -> buildId(id)).toList();
        return cacheService.removeList(ids);
    }

    @Override
    public boolean save(UidInfo uidInfo) {
        String id = uidInfo.getUid();
        id = buildId(id);
        return cacheService.save(id, JSONUtil.toJsonStr(uidInfo));
    }

    @Override
    public List<UidInfo> findUidAll() {
        List<String> uidList = new ArrayList<>();
        String key = getSuffix().substring(0, getSuffix().lastIndexOf(":"));
        String uid_all = cacheService.findValueByKey(key);
        if (StrUtil.isNotBlank(uid_all)) {
            if (JSONUtil.isTypeJSONArray(uid_all)) {
                JSONUtil.toList(uid_all, String.class).stream().forEach(uidList::add);
            } else  {
                uidList.add(uid_all);
            }
        }
        List<UidInfo> list = uidList.stream().map(this::find).toList();
        return list;
    }

    @Override
    public UidInfo find(String id) {
        id = buildId(id);
        UidInfo uidInfo = cacheService.find(id, UidInfo.class);
        return uidInfo;
    }
}
