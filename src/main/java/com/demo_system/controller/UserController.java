package com.demo_system.controller;
import com.demo_system.entity.User;
import com.demo_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController    //rest接口控制器，返回的对象可以自动转化为json然后给前段
@RequiredArgsConstructor       //lombok自动生成构造器，自动注入service
@RequestMapping("/api")
public class UserController {

    //spring帮忙自动注入
    private final UserService userService;
    @PostMapping("/login")
    public Map<String,Object> login(@RequestParam String username,//可以不写接收参数名，同名自动绑定
                                    @RequestParam String password)
    {
        // @RequestParam 表示从前端的「表单格式参数」中取值（对应前端URLSearchParams构造的参数）
        // username=zhangsan&password=123
        Map<String,Object> result = new HashMap<>();
        try
        {
            //调用业务逻辑层
            User user = userService.login(username,password);

            result.put("code",200);
            result.put("message","登录成功");
            Map<String,Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("fullname", user.getFullname());
            result.put("data", data);
        }
        catch (RuntimeException e)
        {
            result.put("code",401);
            result.put("message",e.getMessage());
        }
        return result;
    }
    @PostMapping("/register")
    public Map<String,Object> register(@RequestBody User user)
    {
        System.out.println("接收到的用户数据：" + user.getUsername() + "," + user.getFullname());
        Map<String,Object> result = new HashMap<>();
        String fullname = user.getFullname();
        String username = user.getUsername();
        String password = user.getPassword();
        try
        {
            user = userService.register(fullname,username,password);
            result.put("success",true);
            result.put("message","注册成功");
            result.put("data",user.getUsername());
        }
        catch (RuntimeException e){
            result.put("success",false);
            result.put("message",e.getMessage());
        }
        return result;
    }
}
