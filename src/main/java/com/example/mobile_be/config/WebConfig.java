package com.example.mobile_be.config;

import java.io.File;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = new File("/uploads/").getAbsolutePath();
        registry
                .addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}