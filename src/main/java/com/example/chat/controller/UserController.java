package com.example.chat.controller;

import com.example.chat.entity.User;
import com.example.chat.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Resource
    UserMapper userMapper;

    @PostMapping("/login")
    @ResponseBody
    public Object login(String username, String password, HttpServletRequest req) {
        User user = userMapper.selectByName(username);
        if (user == null || !user.getPassword().equals(password)) {
            return new User();
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);
        user.setPassword("");
        return user;
    }

    @PostMapping("/register")
    @ResponseBody
    public Object register(String username, String password) {
        User user = null;
        try {
            user = new User();
            user.setUsername(username);
            user.setPassword(password);
            userMapper.insert(user);
            user.setPassword("");
        } catch (DuplicateKeyException e) {
            user = new User();
        }
        return user;
    }

    @GetMapping("/userInfo")
    @ResponseBody
    public Object getUserInfo(HttpServletRequest req) {
        // 从请求中获取到会话
        HttpSession session = req.getSession(false);
        if (session == null) {
            return new User();
        }
        // 从会话中获取到保存的用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new User();
        }

        user.setPassword("");
        return user;
    }

    @GetMapping("getalluserbyname")
    @ResponseBody
    public Object getAllUserByName(String name, HttpServletRequest req) {
        return userMapper.selectAllByName(name);
    }
}
