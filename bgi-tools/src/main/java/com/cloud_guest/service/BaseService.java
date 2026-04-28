package com.cloud_guest.service;



/**
 * @Author yan
 * @Date 2026/3/30 17:47:12
 * @Description
 */
public interface BaseService {
    default String getSuffix() {
        return "";
    }
}
