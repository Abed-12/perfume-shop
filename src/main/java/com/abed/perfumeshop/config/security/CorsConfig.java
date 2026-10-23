package com.abed.perfumeshop.config.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @PostConstruct
    public void validate() {
        if (allowedOrigins == null || allowedOrigins.isEmpty() || allowedOrigins.stream().allMatch(String::isBlank)) {
            throw new IllegalStateException("CORS_ALLOWED_ORIGINS must be set and cannot be empty. Please define it in your environment variables.");
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(allowedOrigins.stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.endsWith("/") ? s.substring(0, s.length() - 1) : s)
                .toList());
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
