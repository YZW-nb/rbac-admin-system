package com.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * 跨域配置
 * 通过 admin.cors.allowed-origins 配置允许的来源
 * 生产环境必须配置具体域名，禁止使用 *
 */
@Slf4j
@Configuration
public class CorsConfig {

    @Value("${admin.cors.allowed-origins:}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            // 未配置时默认允许所有来源（仅限开发环境）
            log.warn("CORS 未配置白名单，默认允许所有来源！生产环境请通过 admin.cors.allowed-origins 配置具体域名");
            config.addAllowedOriginPattern("*");
        } else if ("*".equals(allowedOrigins.trim())) {
            // 显式配置为 * 时也允许（开发环境）
            config.addAllowedOriginPattern("*");
        } else {
            // 配置了具体域名白名单
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            origins.forEach(origin -> config.addAllowedOrigin(origin.trim()));
            log.info("CORS 已配置白名单: {}", origins);
        }

        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有方法
        config.addAllowedMethod("*");
        // 允许携带凭证
        config.setAllowCredentials(true);
        // 预检请求缓存时间
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
