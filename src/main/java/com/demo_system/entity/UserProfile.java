package com.demo_system.entity;

import lombok.Data;

@Data
public class UserProfile {
    private Long id;
    private Long userId;           //  关联user表的id
    private Integer gender;        //  性别（0:未知, 1:男, 2:女）
    private String profile;         // 个人简介
    private Integer totalStudyTime; // 总学习时长（分钟）
    private String avatar;          // 头像路径


    public UserProfile(Long userId, Integer gender, String profile, Integer totalStudyTime, String avatar) {
        this.id = null;
        this.userId = userId;
        this.gender = gender;
        this.profile = profile;
        this.totalStudyTime = totalStudyTime;
        this.avatar = avatar;
    }

    public UserProfile() {}
}