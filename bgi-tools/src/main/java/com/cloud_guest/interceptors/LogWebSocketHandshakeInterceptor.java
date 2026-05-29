package com.cloud_guest.interceptors;

import cn.hutool.core.util.StrUtil;
import com.cloud_guest.entitys.domain.LogKey;
import com.cloud_guest.service.LogsService;
import com.cloud_guest.utils.IdUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @Author yan
 * @Date 2026/5/7 18:29:02
 * @Description
 */


@Slf4j
@Component
public class LogWebSocketHandshakeInterceptor implements HandshakeInterceptor {
    @Resource
    private LogsService logsService;
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String query = request.getURI().getQuery();
        if (StrUtil.isBlank(query)) {
            log.warn("[WS-HANDSHAKE] 缺少查询参数");
            return false;
        }

        String token = extractParameter(query, "token");
        if (StrUtil.isBlank(token) || StrUtil.equals(token, "null") || StrUtil.equals(token, "undefined")) {
            log.warn("[WS-HANDSHAKE] 缺少Token参数");
            return false;
        }

        LogKey logKey = logsService.getLogKey(token);
        if (logKey == null) {
            log.warn("[WS-HANDSHAKE] Token无效");
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (logKey.getExpireTime().isBefore(now)) {
            log.warn("[WS-HANDSHAKE] Token已过期");
            return false;
        }

        LocalDateTime dateTime = now.plusMinutes(30);
        if (logKey.getExpireTime().isBefore(dateTime)) {
            String newToken = IdUtils.getNextIdStr();
            logKey.setToken(newToken);
            logKey.setExpireTime(dateTime);
            logsService.update(logKey);

            attributes.put("token", newToken);
            attributes.put("tokenRefreshed", "true");
            log.info("[WS-HANDSHAKE] Token已续期，新Token: {}", newToken);
        }

        String applicationId = extractParameter(query, "applicationId");
        if (StrUtil.isNotBlank(applicationId)) {
            attributes.put("applicationId", applicationId);
            log.debug("[WS-HANDSHAKE] 指定应用实例: {}", applicationId);
        } else {
            log.warn("[WS-HANDSHAKE] 缺少applicationId参数");
            return false;
        }

        String lastTimestamp = extractParameter(query, "lastTimestamp");
        if (StrUtil.isNotBlank(lastTimestamp)) {
            attributes.put("lastTimestamp", lastTimestamp);
        }
        log.debug("[WS-HANDSHAKE] 握手成功 | App: {}", applicationId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private String extractParameter(String query, String paramName) {
        if (query.contains(paramName)){
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith(paramName + "=")) {
                    return param.substring(paramName.length() + 1);
                }
            }
        }
        return null;
    }
}
