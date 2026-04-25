package com.cloud_guest.service;

import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.WsProxyAccess;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/3/22 16:19:31
 * @Description
 */
public interface WsProxyService extends BaseService{
    @Override
    default String getSuffix() {
        return KeyConstants.ws_proxy_access_key;
    }

    boolean save(String id, String json);

    boolean delList(List<String> ids);

    List<String> findUidAll();

    WsProxyAccess find(String id);

    List<WsProxyAccess> findAll();
}
