package com.cloud_guest.abs;

import com.cloud_guest.abs.order.FilterOrderConstants;


import jakarta.servlet.*;
import org.springframework.core.Ordered;

import java.io.IOException;


/**
 * @Author yan
 * @Date 2026/2/10 12:54:47
 * @Description
 */
public interface ApiSignFilter extends Ordered, Filter,  AbsApiSign {
    @Override
    default int getOrder() {
        return FilterOrderConstants.ApiOrder;
    }

    @Override
    default void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    default void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    default void destroy() {
        Filter.super.destroy();
    }
}
