package com.cloud_guest.service;

import lombok.SneakyThrows;

/**
 * @Author yan
 * @Date 2026/2/23 16:11:19
 * @Description
 */
public interface ApplicationService {
    @SneakyThrows
    boolean saveToken(String name, String value);
}
