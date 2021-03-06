package com.starter.track;

import com.common.util.JsonUtil;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/track")
@Component
public class TrackSocket {
    private static ConcurrentHashMap<String, TrackSocket> webSocketMap = new ConcurrentHashMap<>();

    private Session session;
    private String uid;

    public static void sendMessage(Map<String, Object> msg) {
        String msgStr = JsonUtil.toStr(msg);
        System.out.printf("Send message: %s\n", msgStr);

        for (String uid : webSocketMap.keySet()) {
            try {
                webSocketMap.get(uid).session.getBasicRemote().sendText(msgStr);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    @OnMessage
    public void onMessage(String msg, Session session) {
        System.out.printf("Receive message: %s, %s, %s\n", uid, session.getId(), msg);
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        this.uid = session.getId();

        webSocketMap.put(uid, this);
        System.out.printf("Online: %d, %s\n", webSocketMap.size(), uid);
    }

    @OnClose
    public void onClose() {
        webSocketMap.remove(uid);
        System.out.printf("Offline 1, %s, online: %d\n", uid, webSocketMap.size());
    }

    @OnError
    public void onError(Session session, Throwable e) {
        System.err.printf("Error: %s, %s, %s\n", uid, session.getId(), e.getMessage());
    }
}
