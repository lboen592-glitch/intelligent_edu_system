package com.demo_system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // 图片保存物理路径：项目根目录下的 uploads 文件夹
    public static final String UPLOAD_PATH = System.getProperty("user.dir") + "/uploads/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 URL /images/** 映射到本地磁盘路径
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + UPLOAD_PATH);
    }
}