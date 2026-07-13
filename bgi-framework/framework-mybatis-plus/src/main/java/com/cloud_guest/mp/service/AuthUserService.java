package com.cloud_guest.mp.service;

/**
 * @Author yan
 * @Date 2026/7/13 21:12:08
 * @Description
 */
public interface AuthUserService {
    default String getUserId() {
        return null;
    }
}
