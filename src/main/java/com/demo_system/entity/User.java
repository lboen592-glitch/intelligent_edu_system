package com.demo_system.entity;

import lombok.Data;


@Data
public class User {
    private Long id;
    private String username;  // 用户名（学号）
    private String password;  // 密码（加密存储）
    private String nickname;  // 昵称（可选）
}