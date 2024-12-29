package com.antgroup.ewallet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow requests from localhost:3000 (for development with React or other frontend)
        registry.addMapping("/**").allowedOrigins("http://localhost:3000");
    }
}
