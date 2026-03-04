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

    /**
     * 加载application.yml (加载n ms内的缓冲数据 为空时直接加载忽悠时间判断)
     * @param loadTime(加载n ms内的缓冲数据 为空时直接加载忽悠时间判断)
     * @return
     */
    boolean loadApplicationYml(Long loadTime);

    boolean saveLoadApplicationYml(JSONObject jsonObject);

    JSONObject setCheckToken(String name, String value, JSONObject jsonObject);
}
