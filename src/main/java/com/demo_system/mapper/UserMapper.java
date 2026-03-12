package com.demo_system.mapper;

import com.demo_system.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
// 告诉MyBatis这是一个Mapper接口，MyBatis会通过动态代理自动生成这个接口的实现类；
// 告诉Spring：这个接口的实例可以被自动注入到Service层（比如UserServiceImpl中的userMapper）；
// 无需手动写实现类，MyBatis会根据方法名/注解SQL/XML配置，自动完成数据库操作。
public interface UserMapper {
    // @Param("username")给参数起别名“username”，方便在SQL中用 #{username} 引用
    User selectByUsername(@Param("username") String username);  // 自定义查询：根据用户名查用户
    int insertUser(@Param("username") String username,
               @Param("password") String password,
               @Param("fullname") String fullname
    );
}