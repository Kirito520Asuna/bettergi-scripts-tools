package com.cloud_guest.abs;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.properties.auth.AuthProperties;
import com.cloud_guest.utils.jwt.JwtUtil;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author yan
 * @Date 2026/2/10 12:54:47
 * @Description
 */
public interface AuthFilter {

    /**
     * 检查token是否合法
     *
     * @param request
     * @param response
     * @return
     */

    default boolean checkToken(HttpServletRequest request, HttpServletResponse response) {
        AuthProperties authProperties = SpringUtil.getBean(AuthProperties.class);
        String authHeader = request.getHeader(authProperties.getTokenName());
        authHeader = StrUtil.isBlankIfStr(authHeader) ? request.getHeader(HttpHeaders.AUTHORIZATION) : authHeader;
        if (StrUtil.isNotBlank(authHeader)) {
            String bearer = "Bearer";
            String token = "";
            if (authHeader.startsWith(bearer)) {
                token = authHeader.substring(bearer.length() - 1);
            }
            token = token.trim();

            if (StrUtil.isNotBlank(token)) {
                setToken(token);
            }
        }
        return true;
    }

    default void setToken(String token) {
        //JwtUtil jwtUtil = SpringUtil.getBean(JwtUtil.class);
        //if (jwtUtil.validateToken(token)) {
        //    String username = jwtUtil.getUsernameFromToken(token);
        //    // 简单起见，不设权限，只放用户名进去
        //    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        //            username, null, null);
        //    SecurityContextHolder.getContext().setAuthentication(auth);
        //}
    }

    default void checkTokenLogin(HttpServletRequest request, HttpServletResponse response) {
        checkToken(request, response);
    }
}
