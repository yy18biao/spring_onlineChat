package com.example.chat.entity;

import lombok.Data;

@Data
public class User {
    private int userId;
    private String username;
    private String password;
    private String photo;
}
