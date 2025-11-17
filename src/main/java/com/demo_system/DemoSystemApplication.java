package com.demo_system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@MapperScan("com.demo_system.mapper")  // 关键：指定Mapper接口所在的包
public class DemoSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoSystemApplication.class, args);
    }
}