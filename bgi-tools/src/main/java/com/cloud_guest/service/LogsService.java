package com.cloud_guest.service;

import com.cloud_guest.domain.LogKey;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/5/8 0:59:15
 * @Description
 */
public interface LogsService {
    List<String> getFileNames();

    LogKey createLogKey();

    LogKey update(LogKey logKey);

    LogKey getLogKey(String token);

    List<LogKey> getAllExpiredLogKeyList();

    boolean remove(LogKey logKey);

    boolean saveOrUpdate(LogKey logKey);
}
