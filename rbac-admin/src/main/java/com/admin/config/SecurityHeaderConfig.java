package com.admin.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 安全响应头配置
 * 添加常见安全头，防止点击劫持、MIME 嗅探等攻击
 */
@Configuration
public class SecurityHeaderConfig {

    @Bean
    public FilterRegistrationBean<SecurityHeaderFilter> securityHeaderFilter() {
        FilterRegistrationBean<SecurityHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SecurityHeaderFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.setName("securityHeaderFilter");
        return registration;
    }
}
