package com.example.chat.mapper;

import com.example.chat.entity.Friend;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FriendMapper {
    List<Friend> selectFriendList(int userId);
    @Insert("insert into friend values (#{userId}, #{friendId}, #{state})")
    int insertFriend(int userId, int friendId, int state);
    @Update("update friend set state=#{state} where userId = #{userId} and friendId=#{friendId}")
    int updateFriend(int userId, int friendId, int state);
    @Delete("delete from friend where userId = #{userId} and friendId = #{friendId}")
    int deleteFriend(int userId, int friendId);
    @Select("select * from friend where userId = #{userId} and state=0")
    List<Friend> selectinviteList(int userId);
}
