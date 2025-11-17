package com.demo_system.entity;

import lombok.Data;


@Data
public class User {
    private Long id;
    private String username;  // 用户名
    private String password;  // 密码
    private String fullname;  // 真实姓名

    // 构造函数（id设置为null，反正后续数据库更新自增）
    public User(String fullname, String username, String password) {
        this.id = null;
        this.fullname = fullname;
        this.username = username;
        this.password = password;
    }
}