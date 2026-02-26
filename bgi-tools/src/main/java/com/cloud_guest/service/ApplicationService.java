package com.cloud_guest.service;

import cn.hutool.json.JSONObject;
import lombok.SneakyThrows;

/**
 * @Author yan
 * @Date 2026/2/23 16:11:19
 * @Description
 */
public interface ApplicationService {
    @SneakyThrows
    boolean saveToken(String name, String value);

    boolean loadApplicationYml();

    boolean saveLoadApplicationYml(JSONObject jsonObject);

    JSONObject setCheckToken(String name, String value, JSONObject jsonObject);
}
