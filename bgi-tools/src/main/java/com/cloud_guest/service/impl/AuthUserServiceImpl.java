package com.cloud_guest.service.impl;

import com.cloud_guest.mp.service.AuthUserService;
import com.cloud_guest.utils.AuthContextUtil;

/**
 * @Author yan
 * @Date 2026/7/13 21:15:06
 * @Description
 */
public class AuthUserServiceImpl implements AuthUserService {
    @Override
    public String getUserId() {
        return AuthContextUtil.getUsernameNoThrow();
    }
}
