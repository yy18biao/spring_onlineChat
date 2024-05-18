package com.example.chat.mapper;

import com.example.chat.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    String getLastMessageBySessionId(int sessionId);
    List<Message> getMessagesBySessionId(int sessionId);
    void add(Message message);
}
