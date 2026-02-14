package com.cloud_guest.utils;

import cn.hutool.core.util.ObjectUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @Author yan
 * @Date 2026/2/14 11:42:28
 * @Description
 */
public class AuthContextUtil {
    // 获取SecurityContext
    public static SecurityContext getContext() {
        return SecurityContextHolder.getContext();
    }

    // 获取当前登录用户的Authentication对象
    public static Authentication getAuthentication() {
        return getContext().getAuthentication();
    }

    /**
     * 获取当前认证用户的用户名ID，不会抛出异常
     * 如果用户是匿名用户，则返回null
     *
     * @return 当前用户的ID，如果是匿名用户则返回null
     */
    public static String getUsernameIdNoThrow() {
        // 获取当前认证用户的用户名
        String userId = getAuthentication().getName();
        // 检查是否为匿名用户，如果是则设置为null
        if (ObjectUtil.equal("anonymousUser", userId)) {
            userId = null;
        }
        return userId;
    }

    /**
     * 设置用户名并进行身份验证
     *
     * @param username 要设置的用户名
     * @return 设置成功返回true
     */
    public static boolean setUsername(String username) {
        // 创建一个UsernamePasswordAuthenticationToken对象，使用提供的用户名
        // 密码为null，权限列表也为null
        try {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username, null, null);
            // 将认证信息设置到安全上下文中
            getContext().setAuthentication(auth);
            // 返回true表示设置成功
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
