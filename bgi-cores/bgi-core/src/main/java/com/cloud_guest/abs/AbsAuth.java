package com.cloud_guest.abs;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.properties.auth.AuthProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author yan
 * @Date 2026/2/13 11:13:33
 * @Description
 */
public interface AbsAuth {
    /**
     * 获取需要保护的路径列表
     * @return 返回一个包含需要保护路径的字符串数组
     */
    default String[] fetchProtectedPaths() {
        // 定义需要保护的路径，使用逗号分隔，然后分割成数组
        String[] protectedPaths = "/jwt/**".split(",");
        return protectedPaths;
    }
    /**
     * 获取一个AntPathMatcher实例的默认方法
     * AntPathMatcher是Spring框架中用于匹配URL路径的工具类
     * 支持通配符和正则表达式进行路径匹配
     *
     * @return 返回一个新的AntPathMatcher实例
     */
    default AntPathMatcher fetchPathMatcher() {
        // 创建并返回一个新的AntPathMatcher对象
        return new AntPathMatcher();
    }
    /**
     * 检查token是否合法
     *
     * @param request
     * @param response
     * @return
     */

    default boolean checkToken(HttpServletRequest request, HttpServletResponse response) {
        AuthProperties authProperties = SpringUtil.getBean(AuthProperties.class);
        if (!authProperties.isEnabled()) {
            return true;
        }
        String authHeader = request.getHeader(authProperties.getTokenName());
        authHeader = StrUtil.isBlank(authHeader) ? request.getHeader(HttpHeaders.AUTHORIZATION) : authHeader;
        if (StrUtil.isNotBlank(authHeader)) {
            String bearer = "Bearer";
            String token = "";
            if (authHeader.startsWith(bearer)) {
                token = authHeader.substring(bearer.length());
            }else {
                token = authHeader;
            }
            token = token.trim();

            if (StrUtil.isNotBlank(token)) {
                return setToken(token);
            }
        }
        return false;
    }

    default boolean setToken(String token) {
        //JwtUtil jwtUtil = SpringUtil.getBean(JwtUtil.class);
        //if (jwtUtil.validateToken(token)) {
        //    String username = jwtUtil.getUsernameFromToken(token);
        //    // 简单起见，不设权限，只放用户名进去
        //    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        //            username, null, null);
        //    SecurityContextHolder.getContext().setAuthentication(auth);
        //}
        return true;
    }

    default boolean checkTokenLogin(HttpServletRequest request, HttpServletResponse response) {
        return checkToken(request, response);
    }
}
