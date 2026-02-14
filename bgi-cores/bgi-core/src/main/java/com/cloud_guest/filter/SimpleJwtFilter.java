package com.cloud_guest.filter;
import com.cloud_guest.abs.AuthFilter;
import com.cloud_guest.utils.jwt.JwtUtil;
import org.springframework.web.filter.OncePerRequestFilter;
/**
 * @Author yan
 * @Date 2026/2/10 12:52:38
 * @Description
 */


import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SimpleJwtFilter extends OncePerRequestFilter implements AuthFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        checkTokenLogin(request, response);

        //String authHeader = request.getHeader("Authorization");
        //if (authHeader != null && authHeader.startsWith("Bearer ")) {
        //    String token = authHeader.substring(7);
        //    if (jwtUtil.validateToken(token)) {
        //        String username = jwtUtil.getUsernameFromToken(token);
        //        // 简单起见，不设权限，只放用户名进去
        //        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        //                username, null, null);
        //        SecurityContextHolder.getContext().setAuthentication(auth);
        //    }
        //}
        chain.doFilter(request, response);
    }
}