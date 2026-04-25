package com.cloud_guest.filter;

import com.cloud_guest.abs.ApiSignFilter;
import com.cloud_guest.domain.http.CachedBodyHttpServletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @Author yan
 * @Date 2026/4/1 0:16:30
 * @Description
 */
public class SignApiFilter extends OncePerRequestFilter implements ApiSignFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        CachedBodyHttpServletRequest cachedBodyHttpServletRequest = new CachedBodyHttpServletRequest(request);
        checkApi(request,cachedBodyHttpServletRequest);
        chain.doFilter(cachedBodyHttpServletRequest, response);
    }
}
