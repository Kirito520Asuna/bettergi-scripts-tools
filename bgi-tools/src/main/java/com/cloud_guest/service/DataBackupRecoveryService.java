package com.cloud_guest.service;

import java.util.Map;

/**
 * @Author yan
 * @Date 2026/3/15 22:16:29
 * @Description
 */
public interface DataBackupRecoveryService {
    Map<String, Object> backup();

    boolean recovery(Map<String, Object> map);
}
