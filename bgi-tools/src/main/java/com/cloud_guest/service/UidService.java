package com.cloud_guest.service;

import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.UidInfo;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/3/30 17:39:36
 * @Description
 */
public interface UidService extends BaseService {
    @Override
    default String getSuffix() {
        return KeyConstants.mapping_uid_key;
    }

    boolean removeList(List<String> ids);
    boolean save(UidInfo uidInfo);
    List<UidInfo> findUidAll();

    UidInfo find(String id);
}
