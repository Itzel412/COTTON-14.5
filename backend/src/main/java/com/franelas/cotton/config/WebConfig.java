package com.franelas.cotton.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // (1. Permite CORS para todas tus URLs /api/...)
                .allowedOrigins("http://localhost:5173") // (2. La URL de tu frontend Vue)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // (3. Métodos permitidos)
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}