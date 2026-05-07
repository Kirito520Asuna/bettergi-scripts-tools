package com.cloud_guest.config;

import com.cloud_guest.handler.LogWebSocketHandler;
import com.cloud_guest.interceptors.LogWebSocketHandshakeInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * @Author yan
 * @Date 2026/5/7 18:26:32
 * @Description
 */
@Configuration
@EnableWebSocket
//@ConditionalOnWebApplication
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private LogWebSocketHandshakeInterceptor handshakeInterceptor;
    @Resource
    private LogWebSocketHandler logWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(logWebSocketHandler, "/ws/logs")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }

    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        return container;
    }

    //@Bean
    //public WebSocketConfigurer webSocketConfigurer() {
    //    return registry -> registry
    //            .addHandler(logWebSocketHandler, "/ws/logs")
    //            .addInterceptors(handshakeInterceptor)
    //            .setAllowedOrigins("*");
    //}


}