package com.example.chat.controller;

import com.example.chat.entity.Friend;
import com.example.chat.entity.User;
import com.example.chat.mapper.FriendMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class FriendController {
    @Resource
    private FriendMapper friendMapper;

    @GetMapping("/friendList")
    @ResponseBody
    public Object getFriendList(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            System.out.println("[getFriendList] session 不存在");
            return new ArrayList<Friend>();
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            System.out.println("[getFriendList] user 不存在");
            return new ArrayList<Friend>();
        }
        List<Friend> friendList = friendMapper.selectFriendList(user.getUserId());
        return friendList;
    }
}
