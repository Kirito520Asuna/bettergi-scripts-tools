package com.cloud_guest.interceptors;

import cn.hutool.core.util.StrUtil;
import com.cloud_guest.utils.AuthContextUtil;
import com.cloud_guest.utils.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * @Author yan
 * @Date 2026/5/7 18:29:02
 * @Description
 */

@Slf4j
@Component
public class LogWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String query = request.getURI().getQuery();
        if (StrUtil.isBlank(query)) {
            log.warn("[WS-HANDSHAKE] 缺少查询参数");
            return false;
        }

        String token = extractToken(query);
        if (StrUtil.isBlank(token)|| StrUtil.equals(token, "null")|| StrUtil.equals(token, "undefined")) {
            log.warn("[WS-HANDSHAKE] 缺少Token参数");
            return false;
        }

        try {
            String username = JwtUtil.getSubjectByParseJWT(token);
            if (StrUtil.isNotBlank(username)) {
                AuthContextUtil.setUsername(username);
                attributes.put("username", username);
                log.debug("[WS-HANDSHAKE] 用户认证成功: {}", username);
                return true;
            }
        } catch (Exception e) {
            log.error("[WS-HANDSHAKE] Token解析失败", e);
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        AuthContextUtil.clear();
    }

    private String extractToken(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                return param.substring(6);
            }
        }
        return null;
    }
}