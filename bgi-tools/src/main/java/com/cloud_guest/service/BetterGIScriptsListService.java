package com.cloud_guest.service;

import com.cloud_guest.constants.KeyConstants;

/**
 * @Author yan
 * @Date 2026/4/17 10:32:23
 * @Description
 */
public interface BetterGIScriptsListService extends BaseService {
    @Override
    default String getSuffix() {
        return KeyConstants.bettergi_scripts_list;
    }
}
