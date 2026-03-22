package com.cloud_guest.utils;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.properties.check.TokenProperties;
import com.cloud_guest.utils.object.ObjectUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.Charset;

/**
 * @Author yan
 * @Date 2026/3/22 22:16:45
 * @Description
 */
public class TokenUtil {
    public static boolean checkToken() {
        TokenProperties tokenProperties = SpringUtil.getBean(TokenProperties.class);
        String value = tokenProperties.getValue();
        String name = tokenProperties.getName();
        HttpServletRequest request = ServletUtil.getRequest();
        String token = JakartaServletUtil.getHeader(request, name, Charset.forName("UTF-8"));
        return ObjectUtils.equals(value, token);
    }

}
