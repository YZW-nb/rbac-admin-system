package com.admin.service.impl;

import com.admin.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务实现
 * 基于 Redis 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_TOKENS_PREFIX = "user:tokens:";
    private static final long TOKEN_BLACKLIST_EXPIRE_DAYS = 7; // Token 黑名单过期时间

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void addToBlacklist(String token, String reason) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        stringRedisTemplate.opsForValue().set(key, reason, TOKEN_BLACKLIST_EXPIRE_DAYS, TimeUnit.DAYS);
        log.info("Token 加入黑名单: {}, 原因: {}", token.substring(0, Math.min(20, token.length())) + "...", reason);
    }

    @Override
    public boolean isBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    @Override
    public void blacklistUserTokens(Long userId) {
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        Set<String> tokens = stringRedisTemplate.opsForSet().members(userTokensKey);

        if (tokens != null && !tokens.isEmpty()) {
            for (String token : tokens) {
                addToBlacklist(token, "用户被踢出");
            }
            // 清除用户 Token 记录
            stringRedisTemplate.delete(userTokensKey);
            log.info("用户 {} 的所有 Token 已加入黑名单", userId);
        }
    }

    @Override
    public void clearUserBlacklist(Long userId) {
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        stringRedisTemplate.delete(userTokensKey);
        log.info("用户 {} 的 Token 黑名单已清除", userId);
    }

    /**
     * 记录用户登录的 Token（用于实现单点登录或强制下线）
     */
    public void recordUserToken(Long userId, String token) {
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        stringRedisTemplate.opsForSet().add(userTokensKey, token);
        // 设置过期时间（比 Token 过期时间稍长）
        stringRedisTemplate.expire(userTokensKey, 8, TimeUnit.DAYS);
    }
}
