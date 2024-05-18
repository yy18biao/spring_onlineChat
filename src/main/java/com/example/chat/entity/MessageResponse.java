package com.example.chat.entity;

import lombok.Data;

import java.util.List;

@Data
public class MessageResponse {
    private String type = "message";
    private int fromId;
    private String fromName;
    private int sessionId;
    private String content;
    private List<User> users;
}
