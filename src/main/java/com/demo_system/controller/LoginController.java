package com.demo_system.controller;
import com.demo_system.entity.User;
import com.demo_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class LoginController {
    private final UserService userService;

    @PostMapping("/login")
    public Map<String,Object> login(
            @RequestParam String username,
            @RequestParam String password
    ){
        Map<String,Object> result = new HashMap<>();
        try{
            User user = userService.login(username,password);
            // 登陆成功：返回用户信息
            result.put("code",200);
            result.put("message","登录成功");
            result.put("data",user.getUsername());
        } catch (RuntimeException e){
            result.put("code",401);
            result.put("message",e.getMessage());
        }
        return result;
    }
}
