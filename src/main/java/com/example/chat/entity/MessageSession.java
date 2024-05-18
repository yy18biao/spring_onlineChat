package com.example.chat.entity;

import lombok.Data;

import java.util.List;

@Data
public class MessageSession {
    private int sessionId;
    private List<Friend> friends;
    private String lastMessage;
}
