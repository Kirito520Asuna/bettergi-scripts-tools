package com.cloud_guest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.entitys.domain.WsProxyAccess;
import com.cloud_guest.entitys.pojo.WsProxyAccessConfig;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/3/22 16:19:31
 * @Description
 */
public interface WsProxyService  extends IService<WsProxyAccessConfig>, BaseService {
    default String getSuffix() {
        return KeyConstants.ws_proxy_access_key;
    }


    List<String> findUidAll();

    WsProxyAccess find(String id);

    List<WsProxyAccess> findAll();
}
