package com.cloud_guest.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.WsProxyAccess;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.service.WsProxyService;
import com.cloud_guest.utils.object.ObjectUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/3/22 16:19:45
 * @Description
 */
@Service
public class WsProxyServiceImpl implements WsProxyService {
    @Resource
    private CacheService cacheService;

    @Override
    public boolean save(String id, String json) {
        //id = buildId(id);
        id = buildId(id);
        return cacheService.save(id, json);
    }

    @Override
    public boolean delList(List<String> ids) {
        ids = ids.stream().map(id -> buildId(id)).toList();
        return cacheService.removeList(ids);
    }

    @Override
    public List<String> findUidAll() {
        List<String> uidList = new ArrayList<>();
        String key = KeyConstants.ws_proxy_access_key.substring(0, KeyConstants.ws_proxy_access_key.lastIndexOf(":"));
        String uid_all = cacheService.findValueByKey(key);
        if (StrUtil.isNotBlank(uid_all)) {
            if (JSONUtil.isTypeJSONArray(uid_all)) {
                JSONUtil.toList(uid_all, String.class).stream().forEach(uidList::add);
            } else {
                uidList.add(uid_all);
            }
        }

        return uidList;
    }
    @Override
    public WsProxyAccess find(String id) {
        if (StrUtil.isBlank(id)) {
            return null;
        }else if (!id.startsWith(getSuffix())) {
            id = buildId(id);
        }
        WsProxyAccess wsProxyAccess = cacheService.find(id, WsProxyAccess.class);
        return wsProxyAccess;
    }
    @Override
    public List<WsProxyAccess> findAll(){
        List<String> uidAll = findUidAll();
        List<WsProxyAccess> list = uidAll.stream().map(this::find).filter(ObjectUtils::isNotEmpty).toList();
        return list;
    }
}
