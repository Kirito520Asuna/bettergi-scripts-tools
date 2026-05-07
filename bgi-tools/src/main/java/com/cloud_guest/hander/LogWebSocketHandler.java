package com.cloud_guest.hander;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.cloud_guest.utils.ApplicationUtil;

/**
 * @Author yan
 * @Date 2026/5/7 18:27:25
 * @Description
 */

@Slf4j
@Component
public class LogWebSocketHandler extends TextWebSocketHandler {

    private static final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> SESSION_POOL = new ConcurrentHashMap<>();

    @Value("${logging.file.path:./logs}")
    private String logPath;

    private String LOG_PATH;

    @PostConstruct
    public void init() {
        LOG_PATH = logPath;
        log.info("[WS-LOG] 日志目录初始化: {}", LOG_PATH);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String applicationId = (String) session.getAttributes().get("applicationId");
        String tokenRefreshed = (String) session.getAttributes().get("tokenRefreshed");
        String newToken = (String) session.getAttributes().get("token");

        if (StrUtil.isBlank(applicationId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("未认证"));
            return;
        }

        String currentApplicationId = ApplicationUtil.getApplicationId();
        if (!StrUtil.equals(currentApplicationId, applicationId)) {
            sendMessage(session, JSONUtil.toJsonStr(Map.of(
                    "type", "no-connected",
                    "message", "实例未命中"
            )));
            return;
        }

        String sessionKey = applicationId;
        SESSION_POOL.computeIfAbsent(sessionKey, k -> new CopyOnWriteArraySet<>()).add(session);

        String filename = (String) session.getAttributes().get("filename");
        String linesParam = (String) session.getAttributes().get("lines");

        log.info("[WS-LOG] 客户端连接成功 | App: {} | File: {} | Lines: {} | 当前连接数: {}",
                applicationId, filename, linesParam, SESSION_POOL.get(sessionKey).size());

        if (StrUtil.equals(tokenRefreshed, "true") && StrUtil.isNotBlank(newToken)) {
            sendMessage(session, JSONUtil.toJsonStr(Map.of(
                    "type", "token_refreshed",
                    "newToken", newToken,
                    "message", "Token已续期"
            )));
            log.info("[WS-LOG] 已发送新Token给客户端: {}", newToken);
        }

        sendMessage(session, JSONUtil.toJsonStr(Map.of(
                "type", "connected",
                "message", "日志推送已建立"
        )));

        if (StrUtil.isNotBlank(filename)) {
            sendFileContent(session, sessionKey, filename, linesParam);
        }
    }

    private void sendFileContent(WebSocketSession session, String sessionKey, String filename, String linesParam) {
        try {
            File logFile = new File(LOG_PATH, filename);

            if (!logFile.exists() || !logFile.isFile()) {
                sendMessage(session, JSONUtil.toJsonStr(Map.of(
                        "type", "error",
                        "message", "文件不存在: " + filename
                )));
                return;
            }

            int maxLines = parseMaxLines(linesParam);

            List<String> allLines = FileUtil.readLines(logFile, StandardCharsets.UTF_8);
            int totalLines = allLines.size();

            List<String> linesToSend;
            if (maxLines == -1 || maxLines >= totalLines) {
                linesToSend = allLines;
            } else {
                linesToSend = allLines.subList(totalLines - maxLines, totalLines);
            }

            String content = String.join("\n", linesToSend) + "\n";

            sendMessage(session, JSONUtil.toJsonStr(Map.of(
                    "type", "file_content",
                    "filename", filename,
                    "totalLines", totalLines,
                    "sentLines", linesToSend.size(),
                    "data", content
            )));

            log.info("[WS-LOG] 文件内容发送成功 | Session: {} | File: {} | Total: {} | Sent: {}",
                    sessionKey, filename, totalLines, linesToSend.size());

        } catch (Exception e) {
            log.error("[WS-LOG] 读取文件失败 | Session: {} | File: {}", sessionKey, filename, e);
            sendMessage(session, JSONUtil.toJsonStr(Map.of(
                    "type", "error",
                    "message", "读取文件失败: " + e.getMessage()
            )));
        }
    }

    private int parseMaxLines(String linesParam) {
        if ("all".equalsIgnoreCase(linesParam)) {
            return -1;
        }
        if (StrUtil.isNotBlank(linesParam)) {
            try {
                int lines = Integer.parseInt(linesParam);
                return lines > 0 ? lines : 200;
            } catch (NumberFormatException e) {
                return 200;
            }
        }
        return 200;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String applicationId = (String) session.getAttributes().get("applicationId");
        String sessionKey = applicationId;

        log.debug("[WS-LOG] 收到消息 | App: {} | Message: {}", applicationId, message.getPayload());

        String payload = message.getPayload();
        if (StrUtil.isNotBlank(payload)) {
            try {
                Map<String, Object> msgMap = JSONUtil.toBean(payload, Map.class);
                String action = (String) msgMap.get("action");

                if ("load_file".equals(action)) {
                    String appId = (String) msgMap.get("applicationId");
                    String filename = (String) msgMap.get("filename");
                    String lines = (String) msgMap.get("lines");

                    if (StrUtil.isNotBlank(appId) && StrUtil.isNotBlank(filename)) {
                        sendFileContent(session, appId, filename, lines);
                    }
                }
            } catch (Exception e) {
                log.warn("[WS-LOG] 解析消息失败", e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String applicationId = (String) session.getAttributes().get("applicationId");
        String sessionKey = applicationId;

        if (sessionKey != null) {
            CopyOnWriteArraySet<WebSocketSession> sessions = SESSION_POOL.get(sessionKey);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    SESSION_POOL.remove(sessionKey);
                }
                log.info("[WS-LOG] 客户端断开 | App: {} | 剩余连接数: {}",
                        applicationId, SESSION_POOL.getOrDefault(sessionKey, new CopyOnWriteArraySet<>()).size());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String applicationId = (String) session.getAttributes().get("applicationId");
        log.error("[WS-LOG] 传输错误 | App: {}", applicationId, exception);
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    public static void broadcastLog(String applicationId, String logMessage) {
        String currentAppId = ApplicationUtil.getApplicationId();
        String sessionKey = StrUtil.isNotBlank(applicationId) ? applicationId : currentAppId;

        CopyOnWriteArraySet<WebSocketSession> sessions = SESSION_POOL.get(sessionKey);

        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        String message = JSONUtil.toJsonStr(Map.of(
                "type", "log",
                "data", logMessage
        ));

        sessions.forEach(session -> {
            if (session.isOpen()) {
                sendMessage(session, message);
            }
        });
    }

    private static void sendMessage(WebSocketSession session, String message) {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            log.error("[WS-LOG] 发送消息失败", e);
        }
    }
}
