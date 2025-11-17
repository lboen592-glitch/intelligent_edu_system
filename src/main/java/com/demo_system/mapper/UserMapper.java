package com.demo_system.mapper;

import com.demo_system.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User selectByUsername(@Param("username") String username);  // 自定义查询：根据用户名查用户
    int insertUser(@Param("username") String username,
               @Param("password") String password,
               @Param("fullname") String fullname
    );
}