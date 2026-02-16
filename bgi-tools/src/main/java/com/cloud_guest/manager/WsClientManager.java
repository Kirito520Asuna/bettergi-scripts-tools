package com.cloud_guest.manager;

import cn.hutool.core.util.StrUtil;
import com.cloud_guest.domain.dto.WsProxyDto;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author yan
 * @Date 2025/12/31 21:36:35
 * @Description
 */
@Component
public class WsClientManager {
    private final WsClient client = new WsClient();
    @Value("${ws.url:ws://localhost:8080/ws}")
    private String WS_URL;
    @Value("${ws.access-token-name:access_token}")
    private String accessTokenName;

    /**
     * 根据提供的URL建立连接，如果URL为空则使用默认的WS_URL
     * 该方法使用了synchronized关键字确保线程安全
     *
     * @param url 连接的目标URL，如果为空则使用默认的WS_URL
     * @throws Exception 连接过程中可能出现的异常
     */
    public synchronized void connectIfNeeded(String url) throws Exception {
        // 检查URL是否为空，如果是空则使用默认的WS_URL
        if (StrUtil.isBlankIfStr(url)) {
            url = WS_URL;
        }
        // 建立WebSocket连接
        client.connect(url);
    }

    public String buildUrl(String url, String token) {
        if (StrUtil.isNotBlank(token)) {
            if (url.lastIndexOf("/") <= 0) {
                url += "/";
            }
            url = url + "?" + accessTokenName + "=" + token;
        }
        return url;
    }

    @SneakyThrows
    public void send(WsProxyDto wsProxyDto) {
        String bodyJson = wsProxyDto.getBodyJson();
        String url = wsProxyDto.getUrl();
        String token = wsProxyDto.getToken();
        url = buildUrl(url, token);
        send(url, bodyJson);
    }

    @SneakyThrows
    public void send(String url, String msg) {
        connectIfNeeded(url);
        client.send(msg);
        client.onClose();
    }
}



