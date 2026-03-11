package com.demo_system.service;

import com.demo_system.entity.User;
import com.demo_system.entity.UserProfile;

public interface UserProfileService {

    /**
     * 根据用户ID获取个人信息（如果不存在会自动创建）
     */
    UserProfile getProfileByUserId(Long userId);

    /**
     * 根据用户ID获取用户基本信息
     */
    User getUserById(Long id);

    /**
     * 更新用户个人信息
     */
    void updateProfile(Long userId, Integer gender, String profile);

    /**
     * 更新用户基本信息
     */
    void updateUser(User user);

}