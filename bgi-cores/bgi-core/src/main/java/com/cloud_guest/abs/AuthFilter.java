package com.cloud_guest.abs;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.properties.auth.AuthProperties;
import com.cloud_guest.utils.jwt.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author yan
 * @Date 2026/2/10 12:54:47
 * @Description
 */
public interface AuthFilter extends Filter, AbsAuth {
}
