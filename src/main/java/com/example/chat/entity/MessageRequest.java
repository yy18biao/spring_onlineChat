package com.example.chat.entity;

import lombok.Data;

@Data
public class MessageRequest {
    private String type;
    private int sessionId;
    private String content;
    private String filename; // 文件名
    private String fileExtension; // 文件后缀名
    private String fileSize; // 文件大小
    private byte[] fileBytes;
    private Integer totalChunks;
    private int chunkIndex = 0;
}
