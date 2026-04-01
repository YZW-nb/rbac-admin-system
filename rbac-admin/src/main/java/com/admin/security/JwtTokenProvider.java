package com.admin.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 提供者
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${admin.jwt.secret}")
    private String secret;

    @Value("${admin.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${admin.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${admin.jwt.prefix}")
    private String tokenPrefix;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // 安全校验：拒绝默认密钥和过短密钥
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT 密钥未配置！请通过环境变量 JWT_SECRET 设置一个安全的密钥");
        }
        if ("please-set-jwt-secret-in-environment".equals(secret)) {
            log.error("=========================================================");
            log.error("安全警告：正在使用默认 JWT 密钥！请通过环境变量 JWT_SECRET 设置安全密钥！");
            log.error("生成方式：openssl rand -base64 64");
            log.error("=========================================================");
            if ("prod".equals(System.getenv("SPRING_PROFILES_ACTIVE"))) {
                throw new IllegalStateException("生产环境禁止使用默认 JWT 密钥！请设置环境变量 JWT_SECRET");
            }
        }
        if (secret.length() < 32) {
            log.warn("JWT 密钥长度过短（{} 字符），建议至少 64 字符以增强安全性", secret.length());
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT Token 提供者初始化完成，访问令牌有效期: {}分钟", accessTokenExpiration / 60000);
    }

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(Long userId, String username) {
        return generateToken(userId, username, accessTokenExpiration);
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(Long userId, String username) {
        return generateToken(userId, username, refreshTokenExpiration);
    }

    /**
     * 生成 Token
     */
    private String generateToken(Long userId, String username, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 从 Token 中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的 Token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token 格式错误: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("Token 签名无效: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Token 为空: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 解析 Token
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取 Token 前缀
     */
    public String getTokenPrefix() {
        return tokenPrefix;
    }

    /**
     * 获取访问令牌过期时间（秒）
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration / 1000;
    }
}
