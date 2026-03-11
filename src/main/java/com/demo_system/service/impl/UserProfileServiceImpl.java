package com.demo_system.service.impl;

import com.demo_system.entity.User;
import com.demo_system.entity.UserProfile;
import com.demo_system.mapper.UserProfileMapper;
import com.demo_system.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final UserProfileMapper userProfileMapper;

    @Override
    public UserProfile getProfileByUserId(Long userId) {
        UserProfile userProfile = userProfileMapper.selectByUserId(userId);
        if (userProfile == null) {
            // 自动创建默认用户档案
            System.out.println("用户档案不存在，自动创建默认档案，userId: " + userId);
            UserProfile defaultProfile = new UserProfile(userId, 0, "这个人很懒，什么都没有写～", 0, "");
            try {
                int result = userProfileMapper.insertUserProfile(userId, 0, "这个人很懒，什么都没有写～", 0, "");
                if (result == 1) {
                    userProfile = userProfileMapper.selectByUserId(userId);
                    System.out.println("自动创建用户档案成功");
                }
            } catch (Exception e) {
                System.out.println("自动创建用户档案失败: " + e.getMessage());
                // 即使创建失败，也返回一个默认对象，避免前端报错
                userProfile = defaultProfile;
            }
        }
        return userProfile;
    }

    @Override
    public User getUserById(Long id) {
        return userProfileMapper.selectUserById(id);
    }

    @Override
    public void updateProfile(Long userId, Integer gender, String profile) {
        UserProfile existingProfile = userProfileMapper.selectByUserId(userId);
        if (existingProfile == null) {
            // 如果档案不存在，创建新的档案（使用默认的学习时长0）
            int result = userProfileMapper.insertUserProfile(userId, gender, profile, 0, "");
            if (result != 1) {
                throw new RuntimeException("创建用户档案失败");
            }
        } else {
            // 如果档案存在，更新档案（保持原有的学习时长）
            int result = userProfileMapper.updateUserProfile(userId, gender, profile, existingProfile.getTotalStudyTime(), existingProfile.getAvatar());
            if (result != 1) {
                throw new RuntimeException("更新用户档案失败");
            }
        }
    }

    @Override
    public void updateUser(User user) {
        int result = userProfileMapper.updateUser(user);
        if (result != 1) {
            throw new RuntimeException("更新用户基本信息失败");
        }
    }
}