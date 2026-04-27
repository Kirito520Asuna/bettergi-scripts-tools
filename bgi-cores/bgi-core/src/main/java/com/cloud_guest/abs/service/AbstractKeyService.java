package com.cloud_guest.abs.service;

import com.cloud_guest.domain.key.KeyInfo;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/3/31 19:37:46
 * @Description
 */
public interface AbstractKeyService {
    /**
     * 保存密钥信息
     *
     * @param keyInfo
     * @return
     */
    default Long saveKeyInfo(KeyInfo keyInfo) {
        throw new UnsupportedOperationException();
    }

    /**
     * 根据密钥ID获取密钥信息
     *
     * @param keyId
     * @return
     */
    default KeyInfo getKeyInfoById(String keyId) {
        throw new UnsupportedOperationException();
    }

    /**
     * 根据密钥ID删除密钥信息
     *
     * @param keyIds
     * @return
     */
    default boolean remove(List<String> keyIds) {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @return
     */
    default List<KeyInfo> getAllExpiredKeyInfoList() {
        throw new UnsupportedOperationException();
    }
}
