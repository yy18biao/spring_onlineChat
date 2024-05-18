package com.example.chat.component;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;


import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineUserComponent {
    private ConcurrentHashMap<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 用户上线, 给这个哈希表中插入键值对
    public void online(int userId, WebSocketSession session) {
        if (sessions.get(userId) != null) {
            return;
        }
        sessions.put(userId, session);
    }

    // 用户下线, 针对这个哈希表进行删除元素
    public void offline(int userId, WebSocketSession session) {
        WebSocketSession existSession = sessions.get(userId);
        if (existSession == session) {
            sessions.remove((userId));
        }
    }

    // 根据 userId 获取到 WebSocketSession
    public WebSocketSession getSession(int userId) {
        return sessions.get(userId);
    }

}
