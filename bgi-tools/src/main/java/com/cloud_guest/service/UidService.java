package com.cloud_guest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.entitys.pojo.UidInfoConfig;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/3/30 17:39:36
 * @Description
 */
public interface UidService extends IService<UidInfoConfig>, BaseService {
    default String getSuffix() {
        return KeyConstants.mapping_uid_key;
    }

    boolean removeList(List<String> ids);
    //boolean save(UidInfo uidInfo);
    List<UidInfoConfig> findUidAll();

    UidInfoConfig find(String uid);
}
