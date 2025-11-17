package com.demo_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");  // 允许所有来源（生产环境需指定具体域名）
        config.addAllowedHeader("*");  // 允许所有请求头
        config.addAllowedMethod("*");  // 允许所有请求方法
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}