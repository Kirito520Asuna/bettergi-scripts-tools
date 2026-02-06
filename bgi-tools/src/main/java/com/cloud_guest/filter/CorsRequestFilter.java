package com.cloud_guest.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.aop.bean.AbsBean;
import com.cloud_guest.properties.cors.CorsProperties;
import com.cloud_guest.utils.object.ObjectUtils;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author minimalism
 * @date 2023/7/29 2:47
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsRequestFilter extends OncePerRequestFilter implements AbsBean {
    private CorsProperties cors;

    @Override
    public void init() {
        log().debug("[Filter]-[Cors]-[init] {}", getClass().getName());
    }

    /**
     * @param request
     * @param response
     * @param chain
     * @throws
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        Map<String, Object> headerMap = Maps.newLinkedHashMap();
        if (cors == null) {
            cors = new CorsProperties();
            try {
                cors = SpringUtil.getBean(CorsProperties.class);
            } catch (Exception e) {
                log().warn("[warn]-[Filter]-[Cors] {}", e.getMessage());
            }
        }
        String allowedOrigin = cors.getAllowedOrigin();
        String[] allowedMethods = cors.AllowedMethods();
        Long maxAge = cors.getMaxAge();
        Boolean allowCredentials = cors.getAllowCredentials();
        String allowedMethodsStr = Arrays.stream(allowedMethods).collect(Collectors.joining(","));

        String origin = "Origin";
        String accessControlAllowOrigin = "Access-Control-Allow-Origin";
        String accessControlAllowMethods = "Access-Control-Allow-Methods";
        String accessControlRequestMethod = "Access-Control-Request-Method";
        String accessControlAllowCredentials = "Access-Control-Allow-Credentials";
        String accessControlMaxAge = "Access-Control-Max-Age";
        String accessControlAllowHeaders = "Access-Control-Allow-Headers";
        String accessControlRequestHeaders = "Access-Control-Request-Headers";

        String headerOrigin = request.getHeader(origin);
        String headerAllowMethods = request.getHeader(accessControlAllowMethods);
        String headerRequestMethod = request.getHeader(accessControlRequestMethod);
        String headerAllowHeaders = request.getHeader(accessControlAllowHeaders);
        String headerRequestHeaders = request.getHeader(accessControlRequestHeaders);

        if (ObjectUtils.isNotEmpty(headerOrigin) || ObjectUtils.isNotEmpty(allowedOrigin)) {
            String originHeader = ObjectUtils.defaultIfBlank(headerOrigin, allowedOrigin);
            headerMap.put(accessControlAllowOrigin, originHeader);
        }

        if (ObjectUtils.isNotEmpty(headerAllowMethods)) {
            headerMap.put(accessControlAllowMethods, headerAllowMethods);
        } else {
            headerMap.put(accessControlAllowMethods, allowedMethodsStr);
        }

        if (ObjectUtils.isNotEmpty(headerRequestMethod)) {
            headerMap.put(accessControlRequestMethod, headerRequestMethod);
        }

        if (ObjectUtils.isNotEmpty(maxAge)) {
            headerMap.put(accessControlMaxAge, String.valueOf(maxAge));
        }

        if (ObjectUtils.isNotEmpty(allowCredentials)) {
            headerMap.put(accessControlAllowCredentials, String.valueOf(allowCredentials)); // 如果支持携带凭证
        }

        if (ObjectUtils.isNotEmpty(headerAllowHeaders) || ObjectUtils.isNotEmpty(headerRequestHeaders)) {
            String allowHeaders = String.valueOf(ObjectUtils.defaultIfEmpty(headerAllowHeaders, headerRequestHeaders));
            allowHeaders = ObjectUtils.defaultIfBlank(allowHeaders, cors.getAllowedHeader());
            headerMap.put(accessControlAllowHeaders, allowHeaders); //
        } else {
            String allowHeaders = ObjectUtils.defaultIfBlank(cors.getAllowedHeader(), "*");
            headerMap.put(accessControlAllowHeaders, allowHeaders); //
        }

        if (ObjectUtils.isNotEmpty(headerMap)) {
            for (Map.Entry<String, Object> entry : headerMap.entrySet()) {
                response.setHeader(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        //log().info("headerMap: {}", headerMap);
        if (StrUtil.equalsIgnoreCase("OPTIONS", request.getMethod())) {
            //log().info("OPTIONS ....");
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            //log().info("not OPTIONS ....");
            chain.doFilter(request, response);
        }
    }

}