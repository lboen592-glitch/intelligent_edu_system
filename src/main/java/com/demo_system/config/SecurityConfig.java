package com.demo_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 密码加密器（必须配置，用于密码加密和验证）
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 配置安全规则
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // 前后端分离关闭CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "login.html","/index.html").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()  // 登录接口允许匿名访问
                        .anyRequest().authenticated()  // 其他接口需要认证
                );
        return http.build();
    }
}