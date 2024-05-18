package com.example.chat.controller;

import com.example.chat.entity.Message;
import com.example.chat.mapper.MessageMapper;
import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
public class MessageController {
    @Resource
    private MessageMapper messageMapper;

    @GetMapping("/message")
    public Object getMessage(int sessionId) {
        List<Message> messages = messageMapper.getMessagesBySessionId(sessionId);
        Collections.reverse(messages);
        return messages;
    }

}
