package com.demo_system.controller;
import com.demo_system.entity.User;
import com.demo_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    @Autowired
    private final UserService userService;

    @PostMapping("/login")
    public Map<String,Object> login(
            @RequestParam String username,
            @RequestParam String password
    ){
        Map<String,Object> result = new HashMap<>();
        try{
            User user = userService.login(username,password);
            // 登陆成功，返回用户信息，让控制台输出出来
            result.put("code",200);
            result.put("message","登录成功");
            result.put("data",user.getUsername());
        } catch (RuntimeException e){
            result.put("code",401);
            result.put("message",e.getMessage());
        }
        return result;
    }
    @PostMapping("/register")
    public Map<String,Object> register(
            @RequestBody User user
    ){
        System.out.println("接收到的用户数据：" + user.getUsername() + "," + user.getFullname());
        Map<String,Object> result = new HashMap<>();
        String fullname = user.getFullname();
        String username = user.getUsername();
        String password = user.getPassword();
        try{
            user = userService.register(fullname,username,password);
            // 注册成功：返回用户信息
            result.put("success",true);
            result.put("message","注册成功");
            result.put("data",user.getUsername());
        } catch (RuntimeException e){
            result.put("success",false);
            result.put("message",e.getMessage());
        }
        return result;
    }
}
