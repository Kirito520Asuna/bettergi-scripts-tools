package com.cloud_guest.filter;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.cloud_guest.utils.ThreadMdcUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @Author yan
 * @Date 2026/5/6 17:01:18
 * @Description
 */
@Slf4j
public class TraceFilter extends OncePerRequestFilter implements Ordered {
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //log.debug("TraceFilter 启动");
        try {
            // 从请求头获取 traceId，如果不存在则生成新的
            String traceId = request.getHeader(TRACE_ID_HEADER);
            if (StrUtil.isBlank(traceId)) {
                traceId = ThreadMdcUtil.generateTraceId();
            }
            ThreadMdcUtil.setTraceId(traceId);

            // 返回 traceId 给客户端，方便前后端串联
            response.setHeader(TRACE_ID_HEADER, traceId);

            //log.debug("TraceFilter 设置 traceId = {}", traceId);
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束必须移除，避免线程池线程复用时 MDC 残留
            ThreadMdcUtil.removeTraceId();
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}