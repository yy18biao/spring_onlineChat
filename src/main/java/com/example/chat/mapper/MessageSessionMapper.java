package com.example.chat.mapper;

import com.example.chat.entity.Friend;
import com.example.chat.entity.MessageSession;
import com.example.chat.entity.MessageSessionUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageSessionMapper {
    List<Integer> getSessionIdsByUserId(int userId);
    List<Friend> getFriendsBySessionId(int sessionId, int selfUserId);
    int addMessageSession(MessageSession messageSession);
    void addMessageSessionUser(MessageSessionUser messageSessionUser);
}
