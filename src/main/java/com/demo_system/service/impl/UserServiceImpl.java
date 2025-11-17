package com.demo_system.service.impl;

import com.demo_system.entity.User;
import com.demo_system.mapper.UserMapper;
import com.demo_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.demo_system.config.SecurityConfig;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    @Override
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null){
            throw new RuntimeException("用户不存在");
        }
        if (!password.equals(user.getPassword())){
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    @Override
    public User register(String fullname, String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user != null){
            throw new RuntimeException("用户已存在");
        }
        int result = userMapper.insertUser(username,password,fullname);
        if (result != 1){
            throw new RuntimeException("注册失败");
        }
        user = new User(fullname,username,password);
        return user;
    }
}
