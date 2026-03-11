package com.demo_system.mapper;

import com.demo_system.entity.User;
import com.demo_system.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserProfileMapper {

    // 根据用户ID查询用户档案信息
    UserProfile selectByUserId(@Param("userId") Long userId);
    // 根据用户ID查询用户基本信息
    User selectUserById(@Param("id") Long id);
    // 插入用户档案信息
    int insertUserProfile(@Param("userId") Long userId,
                          @Param("gender") Integer gender,
                          @Param("profile") String profile,
                          @Param("totalStudyTime") Integer totalStudyTime,
                          @Param("avatar") String avatar
    );
    // 更新用户档案信息
    int updateUserProfile(@Param("userId") Long userId,
                          @Param("gender") Integer gender,
                          @Param("profile") String profile,
                          @Param("totalStudyTime") Integer totalStudyTime,
                          @Param("avatar") String avatar
    );
    // 更新用户基本信息
    int updateUser(@Param("user") User user);

}