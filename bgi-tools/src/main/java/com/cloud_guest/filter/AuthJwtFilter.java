package com.cloud_guest.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.abs.AuthFilter;
import com.cloud_guest.aop.bean.AbsBean;
import com.cloud_guest.enums.ApiCode;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.properties.auth.AuthProperties;
import com.cloud_guest.utils.AuthContextUtil;
import com.cloud_guest.utils.jwt.JwtUtil;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Date;

public class AuthJwtFilter extends OncePerRequestFilter implements AuthFilter, AbsBean{

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean setToken(String token) {
        //log().debug("setToken: {}", token);
        boolean validateToken = jwtUtil.validateToken(token);
        if (validateToken) {
            String username = jwtUtil.getUsernameFromToken(token);
            if (StrUtil.isEmpty(username)) {
                return false;
            }
            AuthContextUtil.setUsername(username);
        }
        return validateToken;
    }

    @Override
    public void preSetToken(String token, HttpServletRequest request, HttpServletResponse response) {
        AuthFilter.super.preSetToken(token, request, response);
        try {
            AuthProperties authProperties = SpringUtil.getBean(AuthProperties.class);
            AuthProperties.Ttl ttl = authProperties.getTtl();
            long l = System.currentTimeMillis() + ttl.getRefreshIntervalExpirationMs();// 减去刷新间隔
            Date date = new Date(l);
            boolean notTokenExpired = jwtUtil.isNotTokenExpired(token, date);
            if (!notTokenExpired) {
                log().debug("token即将过期，重新生成token");
                long expirationMs = ttl.getExpirationMs();
                // 先从过期token中提取用户名（即使过期也能解析出claims）
                String username = jwtUtil.getUsernameByToken(token);
                if (StrUtil.isNotBlank(username)) {
                    String generateToken = jwtUtil.generateToken(username, expirationMs);
                    String tokenName = authProperties.getTokenName();
                    response.addHeader(tokenName, generateToken);
                    log().debug("成功刷新token，用户: {}", username);
                } else {
                    log().warn("无法从过期token中提取用户名");
                }
            }
        }catch (Exception e){
            log().error("preSetToken error", e.getMessage());
        }

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestPath = request.getServletPath();
        // 检查是否为受保护路径
        boolean isProtectedPath = false;
        for (String path : fetchProtectedPaths()) {
            isProtectedPath = fetchPathMatcher().match(path, requestPath);
            if (isProtectedPath) {
                break;
            }
        }
        boolean isAuthenticated = true;
        boolean checkTokenLogin = checkTokenLogin(request, response);
        if (isProtectedPath) {
            isAuthenticated = checkTokenLogin;
        }
        if (!isAuthenticated) {
            ApiCode fail = ApiCode.UNAUTHORIZED;
            throw new GlobalException(fail.getCode(), fail.getMessage());
        }
        chain.doFilter(request, response);
    }
}