package com.cloud_guest.service;

import com.cloud_guest.domain.Cache;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/6 18:10:25
 * @Description
 */

public interface FileJsonService {
    String save(String filename, byte[] bytes);
    Cache<String> find(String id);

    boolean del(List<String> ids);
}
