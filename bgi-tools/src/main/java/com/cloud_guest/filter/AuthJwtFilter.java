package com.cloud_guest.filter;

import cn.hutool.core.util.StrUtil;
import com.cloud_guest.abs.AuthFilter;
import com.cloud_guest.aop.bean.AbsBean;
import com.cloud_guest.enums.ApiCode;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.utils.AuthContextUtil;
import com.cloud_guest.utils.jwt.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthJwtFilter extends OncePerRequestFilter implements AuthFilter, AbsBean {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean setToken(String token) {
        log().debug("setToken: {}", token);
        boolean validateToken = jwtUtil.validateToken(token);
        if (validateToken) {
            String username = jwtUtil.getUsernameFromToken(token);
            if (StrUtil.isEmpty(username)){
                return false;
            }
            AuthContextUtil.setUsername(username);
        }
        return validateToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestPath = request.getServletPath();
        // 检查是否为受保护路径
        boolean isProtectedPath = false;
        for (String path : fetchProtectedPaths()) {
            if (fetchPathMatcher().match(path, requestPath)) {
                isProtectedPath = true;
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