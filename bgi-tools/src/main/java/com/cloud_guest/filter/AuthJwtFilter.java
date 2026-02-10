package com.cloud_guest.filter;

import com.cloud_guest.abs.AuthFilter;
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

public class AuthJwtFilter extends OncePerRequestFilter implements AuthFilter {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public void setToken(String token) {
        if (jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            // 简单起见，不设权限，只放用户名进去
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username, null, null);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        checkTokenLogin(request, response);
        chain.doFilter(request, response);
    }
}