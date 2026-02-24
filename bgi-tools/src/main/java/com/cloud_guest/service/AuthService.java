package com.cloud_guest.service;

/**
 * @Author yan
 * @Date 2026/2/24 20:37:45
 * @Description
 */
public interface AuthService {
    String login(String username, String password);

    boolean saveUser(String username, String password);
}
