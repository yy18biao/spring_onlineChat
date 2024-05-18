package com.example.chat.controller;

import com.example.chat.entity.Friend;
import com.example.chat.entity.MessageSession;
import com.example.chat.entity.MessageSessionUser;
import com.example.chat.entity.User;
import com.example.chat.mapper.MessageMapper;
import com.example.chat.mapper.MessageSessionMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class MessageSessionController {
    @Resource
    private MessageSessionMapper messageSessionMapper;
    @Resource
    private MessageMapper messageMapper;

    @GetMapping("/sessionList")
    @ResponseBody
    public Object getMessageSessionList(HttpServletRequest req) {
        List<MessageSession> messageSessionList = new ArrayList<>();        HttpSession session = req.getSession(false);
        if (session == null) {
            return messageSessionList;
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return messageSessionList;
        }
        List<Integer> sessionIdList = messageSessionMapper.getSessionIdsByUserId(user.getUserId());
        for (int sessionId : sessionIdList) {
            MessageSession messageSession = new MessageSession();
            messageSession.setSessionId(sessionId);
            List<Friend> friends = messageSessionMapper.getFriendsBySessionId(sessionId, user.getUserId());
            messageSession.setFriends(friends);
            String lastMessage = messageMapper.getLastMessageBySessionId(sessionId);
            if (lastMessage == null) {
                messageSession.setLastMessage("");
            } else {
                messageSession.setLastMessage(lastMessage);
            }
            messageSessionList.add(messageSession);
        }
        return messageSessionList;
    }

    @PostMapping("/session")
    @ResponseBody
    @Transactional
    public Object addMessageSession(int toUserId, @SessionAttribute("user") User user) {
        HashMap<String, Integer> resp = new HashMap<>();

        MessageSession messageSession = new MessageSession();
        messageSessionMapper.addMessageSession(messageSession);

        MessageSessionUser item1 = new MessageSessionUser();
        item1.setSessionId(messageSession.getSessionId());
        item1.setUserId(user.getUserId());
        messageSessionMapper.addMessageSessionUser(item1);

        MessageSessionUser item2 = new MessageSessionUser();
        item2.setSessionId(messageSession.getSessionId());
        item2.setUserId(toUserId);
        messageSessionMapper.addMessageSessionUser(item2);


        resp.put("sessionId", messageSession.getSessionId());
        return resp;
    }

}
