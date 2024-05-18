package com.example.chat.component;

import com.example.chat.entity.*;
import com.example.chat.mapper.FriendMapper;
import com.example.chat.mapper.MessageMapper;
import com.example.chat.mapper.MessageSessionMapper;
import com.example.chat.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class WebSocketComponent extends TextWebSocketHandler {
    @Autowired
    private OnlineUserComponent onlineUserComponent;

    @Autowired
    private MessageSessionMapper messageSessionMapper;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("[WebSocketAPI] 连接成功!");
        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            return;
        }
        // 把这个键值对给存起来
        onlineUserComponent.online(user.getUserId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("[WebSocketAPI] 收到消息!" + message.toString());
        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            System.out.println("[WebSocketAPI] user == null! 未登录用户, 无法进行消息转发");
            return;
        }

        // 反序列化
        MessageRequest req = objectMapper.readValue(message.getPayload(), MessageRequest.class);
        if (req.getType().equals("message")) {
            // 就进行消息转发
            transferMessage(user, req);
        } else if (req.getType().equals("searchFriends")) {
            MessageResponse resp = new MessageResponse();
            resp.setType("friends");
            resp.setUsers(userMapper.selectAllByName(req.getContent()));
            String respJson = objectMapper.writeValueAsString(resp);
            WebSocketSession webSocketSession = onlineUserComponent.getSession(user.getUserId());
            webSocketSession.sendMessage(new TextMessage(respJson));
        } else if (req.getType().equals("invite_start")) {
            // 排除自己
            if(user.getUsername().equals(req.getContent())) {
                MessageResponse resp = new MessageResponse();
                resp.setType("invite_error_myself");
                String respJson = objectMapper.writeValueAsString(resp);
                WebSocketSession webSocketSession = onlineUserComponent.getSession(user.getUserId());
                webSocketSession.sendMessage(new TextMessage(respJson));
                return;
            }
            // 排除已有好友
            List<Friend> friends = friendMapper.selectFriendList(user.getUserId());
            for (Friend friend1 : friends) {
                if(friend1.getFriendName().equals(req.getContent())) {
                    MessageResponse resp = new MessageResponse();
                    resp.setType("invite_error");
                    String respJson = objectMapper.writeValueAsString(resp);
                    WebSocketSession webSocketSession = onlineUserComponent.getSession(user.getUserId());
                    webSocketSession.sendMessage(new TextMessage(respJson));
                    return;
                }
            }
            User friend = userMapper.selectByName(req.getContent());
            friendMapper.insertFriend(user.getUserId(), friend.getUserId(), 1);
            friendMapper.insertFriend(friend.getUserId(), user.getUserId(), 0);
        }else if(req.getType().equals("invite_success")){
            User friend = userMapper.selectByName(req.getContent());
            friendMapper.updateFriend(user.getUserId(), friend.getUserId(), 1);
        }else if(req.getType().equals("invite_fail")){
            User friend = userMapper.selectByName(req.getContent());
            friendMapper.deleteFriend(user.getUserId(), friend.getUserId());
            friendMapper.deleteFriend(friend.getUserId(), user.getUserId());
        } else if (req.getType().equals("select_invite")) {
            List<Friend> friends = friendMapper.selectinviteList(user.getUserId());
            List<User> users = new ArrayList<>();
            for (Friend friend : friends) {
                User user1 = userMapper.selectById(friend.getFriendId());
                users.add(user1);
            }
            MessageResponse resp = new MessageResponse();
            resp.setType("select_success");
            resp.setUsers(users);
            String respJson = objectMapper.writeValueAsString(resp);
            WebSocketSession webSocketSession = onlineUserComponent.getSession(user.getUserId());
            webSocketSession.sendMessage(new TextMessage(respJson));
        }
    }

    // 消息转发业务处理
    private void transferMessage(User fromUser, MessageRequest req) throws IOException {
        // 一个待转发的响应对象
        MessageResponse resp = new MessageResponse();
        resp.setType("message");
        resp.setFromId(fromUser.getUserId());
        resp.setFromName(fromUser.getUsername());
        resp.setSessionId(req.getSessionId());
        resp.setContent(req.getContent());
        // 序列化
        String respJson = objectMapper.writeValueAsString(resp);
        List<Friend> friends = messageSessionMapper.getFriendsBySessionId(req.getSessionId(), fromUser.getUserId());
        Friend myself = new Friend();
        myself.setFriendId(fromUser.getUserId());
        myself.setFriendName(fromUser.getUsername());
        friends.add(myself);

        // 会话中的所有用户都发一份 包括自己
        for (Friend friend : friends) {
            WebSocketSession webSocketSession = onlineUserComponent.getSession(friend.getFriendId());
            if (webSocketSession == null) {
                continue;
            }
            webSocketSession.sendMessage(new TextMessage(respJson));
        }

        // 将新消息存入数据库
        Message message = new Message();
        message.setFromId(fromUser.getUserId());
        message.setSessionId(req.getSessionId());
        message.setContent(req.getContent());
        messageMapper.add(message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("[WebSocketAPI] 连接异常!" + exception.toString());

        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            return;
        }
        onlineUserComponent.offline(user.getUserId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("[WebSocketAPI] 连接关闭!" + status.toString());

        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            return;
        }
        onlineUserComponent.offline(user.getUserId(), session);
    }
}
