package com.example.chat.mapper;

import com.example.chat.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper {
    int insert(User user);
    User selectByName(String username);
    List<User> selectAllByName(String name);
    @Select("select * from user where userId=#{id}")
    User selectById(int id);
    @Update("update user set photo = #{photo} where userId = #{id}")
    int updatePhotoById(Integer id, String photo);
}
