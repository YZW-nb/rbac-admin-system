package com.admin.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 安全响应头过滤器
 */
public class SecurityHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 防止点击劫持
        httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
        // 防止 MIME 嗅探
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        // XSS 防护
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        // 控制 Referrer 信息泄露
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        // 禁止浏览器缓存敏感 API 响应
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        httpResponse.setHeader("Pragma", "no-cache");

        chain.doFilter(request, response);
    }
}
