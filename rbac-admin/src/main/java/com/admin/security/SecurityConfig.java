package com.admin.security;

import com.admin.handler.JwtAccessDeniedHandler;
import com.admin.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Value("${knife4j.enable:false}")
    private boolean knife4jEnabled;

    /**
     * 基础放行路径（始终放行）
     */
    private static final String[] PERMIT_URLS = {
            // 认证接口
            "/api/auth/login",
            "/api/auth/refresh",
            "/favicon.ico",
            // 静态资源
            "/static/**",
            "/uploads/**"
    };

    /**
     * 接口文档路径（仅开发环境放行）
     */
    private static final String[] SWAGGER_URLS = {
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 关闭 CSRF（前后端分离不需要）
            .csrf(AbstractHttpConfigurer::disable)
            // 关闭 Session（JWT 无状态）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 授权规则
            .authorizeHttpRequests(auth -> {
                // 始终放行的路径
                auth.requestMatchers(PERMIT_URLS).permitAll();
                // 仅在 Knife4j 启用时放行文档路径
                if (knife4jEnabled) {
                    auth.requestMatchers(SWAGGER_URLS).permitAll();
                }
                // 其他请求需要认证
                auth.anyRequest().authenticated();
            })
            // 禁用默认登出
            .logout(AbstractHttpConfigurer::disable)
            // 未认证处理（返回 JSON）
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            // 添加 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
