package com.demo_system.service.impl;

import com.demo_system.entity.User;
import com.demo_system.mapper.UserMapper;
import com.demo_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service  //表示服务层，同时也帮助自动注入
@RequiredArgsConstructor //同理自动注入Mapper中的SQL方法
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    @Override //重写注解
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        //开始校验
        if (user == null) throw new RuntimeException("用户不存在");
        if (!password.equals(user.getPassword()))throw new RuntimeException("密码错误");
        return user;
    }

    @Override
    public User register(String fullname, String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user != null) throw new RuntimeException("用户已存在");
        //用户不存在可以注册
        int result = userMapper.insertUser(username,password,fullname);
        if (result != 1) throw new RuntimeException("注册失败");
        user = new User(fullname,username,password);
        return user;
    }
}
