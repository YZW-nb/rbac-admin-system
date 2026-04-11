package com.admin.security;

import com.admin.common.constant.CommonConstant;
import com.admin.entity.SysUser;
import com.admin.service.TokenBlacklistService;
import com.admin.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationContext applicationContext;

    /** 延迟注入，打破循环依赖 */
    private UserService userService;
    private TokenBlacklistService tokenBlacklistService;

    /** Token 滑动窗口续期时间（分钟），与 JwtTokenProvider 过期时间保持一致 */
    private int getTokenExpireMinutes() {
        return (int) (jwtTokenProvider.getAccessTokenExpirationSeconds() / 60);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 从请求头获取 Token
        String token = extractToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 检查 Token 是否在黑名单中
            if (this.tokenBlacklistService == null) {
                this.tokenBlacklistService = applicationContext.getBean(TokenBlacklistService.class);
            }

            if (tokenBlacklistService.isBlacklisted(token)) {
                log.debug("Token 在黑名单中，拒绝访问");
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(token);

            // 检查 Redis 中 Token 是否有效（支持主动失效）
            String redisKey = CommonConstant.REDIS_TOKEN_PREFIX + userId;
            Object cachedToken = redisTemplate.opsForValue().get(redisKey);

            if (cachedToken != null && token.equals(cachedToken.toString())) {
                // 延迟获取 UserService，打破循环依赖
                if (this.userService == null) {
                    this.userService = applicationContext.getBean(UserService.class);
                }
                // 查询用户信息
                SysUser user = userService.getById(userId);
                if (user != null && user.getStatus() == CommonConstant.STATUS_NORMAL) {
                    // 获取用户权限
                    List<String> permissions = userService.getUserPermissions(userId);
                    List<SimpleGrantedAuthority> authorities = permissions.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 刷新 Token 过期时间（滑动窗口）
                    redisTemplate.expire(redisKey, getTokenExpireMinutes(), TimeUnit.MINUTES);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
