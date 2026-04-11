package com.admin.service;

import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务接口
 * 用于处理 Token 注销、踢人等场景
 */
public interface TokenBlacklistService {

    /**
     * 将 Token 加入黑名单
     * @param token JWT Token
     * @param reason 原因
     */
    void addToBlacklist(String token, String reason);

    /**
     * 检查 Token 是否在黑名单中
     * @param token JWT Token
     * @return true 在黑名单中，false 不在
     */
    boolean isBlacklisted(String token);

    /**
     * 将用户的所有 Token 加入黑名单（用于踢人）
     * @param userId 用户 ID
     */
    void blacklistUserTokens(Long userId);

    /**
     * 清除用户的所有 Token 黑名单
     * @param userId 用户 ID
     */
    void clearUserBlacklist(Long userId);
}
