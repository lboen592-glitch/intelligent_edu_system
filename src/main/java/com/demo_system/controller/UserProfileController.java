package com.demo_system.controller;

import com.demo_system.entity.Response;
import com.demo_system.entity.User;
import com.demo_system.entity.UserProfile;
import com.demo_system.service.UserProfileService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * 获取个人信息
     * GET /api/profile
     */
    @GetMapping
    public Response getProfile(@RequestHeader("X-User-Id") Long userId) {
        System.out.println("用户id " + userId + " 请求个人信息");
        try {
            User user = userProfileService.getUserById(userId);
            if (user == null) {
                return Response.fail("用户不存在");
            }

            UserProfile userProfile = userProfileService.getProfileByUserId(userId);
            if (userProfile == null) {
                userProfile = new UserProfile();
                userProfile.setGender(0);
                userProfile.setProfile("");
                userProfile.setTotalStudyTime(0);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("fullname", user.getFullname());
            data.put("gender", userProfile.getGender());
            data.put("profile", userProfile.getProfile());
            data.put("totalStudyTime", userProfile.getTotalStudyTime());
            data.put("avatar", userProfile.getAvatar());

            return Response.ok("获取个人信息成功", data);
        } catch (Exception e) {
            return Response.fail("获取个人信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新个人信息
     * PUT /api/profile
     */
    @PutMapping
    public Response updateProfile(@RequestHeader("X-User-Id") Long userId,
                                  @RequestBody ProfileUpdateReq req) {
        System.out.println("用户id " + userId + " 更新个人信息: " + req);
        try {
            // 更新 user 表
            if (req.getFullname() != null || req.getUsername() != null) {
                User user = userProfileService.getUserById(userId);
                if (user == null) {
                    return Response.fail("用户不存在");
                }
                if (req.getFullname() != null) user.setFullname(req.getFullname());
                if (req.getUsername() != null) user.setUsername(req.getUsername());
                userProfileService.updateUser(user);
            }
            // 更新 profile 表
            Integer gender = req.getGender();
            String profile = req.getProfile();
            userProfileService.updateProfile(userId, gender, profile);

            return Response.ok("更新个人信息成功", null);
        } catch (RuntimeException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     * PUT /api/profile/update-password
     */
    @PutMapping("/update-password")
    public Response updatePassword(@RequestHeader("X-User-Id") Long userId,
                                   @RequestBody PasswordUpdateReq req) {
        System.out.println("用户id " + userId + " 修改密码");
        try {
            if (req.getOldPassword() == null || req.getNewPassword() == null) {
                return Response.fail("参数不完整");
            }
            User user = userProfileService.getUserById(userId);
            if (user == null) {
                return Response.fail("用户不存在");
            }
            if (!user.getPassword().equals(req.getOldPassword())) {
                return Response.fail("旧密码错误");
            }
            user.setPassword(req.getNewPassword());
            userProfileService.updateUser(user);
            return Response.ok("密码修改成功", null);
        } catch (RuntimeException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("服务器内部错误: " + e.getMessage());
        }
    }


    @Data
    public static class ProfileUpdateReq {
        private String username;
        private String fullname;
        private Integer gender;
        private String profile;
    }
    @Data
    public static class PasswordUpdateReq {
        private String oldPassword;
        private String newPassword;
    }
}
