package com.cloud_guest.manager;

import javax.websocket.*;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author yan
 * @Date 2025/12/31 21:38:02
 * @Description
 */

@ClientEndpoint
public class WsClient {

    private Session session;
    private static final AtomicBoolean CONNECTED = new AtomicBoolean(false);

    public void connect(String wsUrl) throws Exception {
        if (CONNECTED.get()) {
            return;
        }
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, new URI(wsUrl));
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        CONNECTED.set(true);
        //System.out.println("[WS] connected");
    }

    @OnMessage
    public void onMessage(String msg) {
        //System.out.println("[WS] recv: " + msg);
    }

    @OnClose
    public void onClose() {
        CONNECTED.set(false);
        //System.out.println("[WS] closed");
    }

    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    public void send(String text) throws Exception {
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(text);
        }
    }
}
