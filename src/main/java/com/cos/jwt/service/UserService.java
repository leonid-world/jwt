package com.cos.jwt.service;


import com.cos.jwt.model.User;
import com.cos.jwt.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public int insertUser(User user) {
        return userMapper.insertUser(user);
    }

    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }
}
