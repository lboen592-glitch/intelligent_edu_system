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
class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null){
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())){
            throw new RuntimeException("密码错误");
        }
        return user;
    }
}
