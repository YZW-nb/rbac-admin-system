package com.admin.service.impl;

import com.admin.service.CacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    // 缓存键前缀
    private static final String DICT_DATA_PREFIX = "cache:dict:data:";
    private static final String DICT_TYPE_PREFIX = "cache:dict:type:";
    private static final String USER_PREFIX = "cache:user:";
    private static final String USER_PERMS_PREFIX = "cache:user:perms:";
    private static final String USER_ROUTES_PREFIX = "cache:user:routes:";
    private static final String CONFIG_PREFIX = "cache:config:";

    // 缓存过期时间
    private static final long DICT_CACHE_EXPIRE_HOURS = 24;
    private static final long USER_CACHE_EXPIRE_HOURS = 2;
    private static final long CONFIG_CACHE_EXPIRE_HOURS = 12;

    // ==================== 字典缓存 ====================

    @Override
    public Object getDictData(String dictType, String dictKey) {
        String key = DICT_DATA_PREFIX + dictType + ":" + dictKey;
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void setDictData(String dictType, String dictKey, Object value) {
        String key = DICT_DATA_PREFIX + dictType + ":" + dictKey;
        redisTemplate.opsForValue().set(key, value, DICT_CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<?> getDictType(String dictType) {
        String key = DICT_TYPE_PREFIX + dictType;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            return objectMapper.convertValue(value, new TypeReference<List<?>>() {});
        }
        return null;
    }

    @Override
    public void setDictType(String dictType, List<?> data) {
        String key = DICT_TYPE_PREFIX + dictType;
        redisTemplate.opsForValue().set(key, data, DICT_CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    @Override
    public void clearDictCache(String dictType) {
        stringRedisTemplate.delete(DICT_TYPE_PREFIX + dictType);
        deleteByPattern(DICT_DATA_PREFIX + dictType + ":*");
        log.debug("清除字典缓存: {}", dictType);
    }

    @Override
    public void clearAllDictCache() {
        deleteByPattern(DICT_DATA_PREFIX + "*");
        deleteByPattern(DICT_TYPE_PREFIX + "*");
        log.info("清除所有字典缓存");
    }

    // ==================== 用户缓存 ====================

    @Override
    public Object getUser(Long userId) {
        String key = USER_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void setUser(Long userId, Object user) {
        String key = USER_PREFIX + userId;
        redisTemplate.opsForValue().set(key, user, USER_CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    @Override
    public void clearUser(Long userId) {
        String key = USER_PREFIX + userId;
        redisTemplate.delete(key);
        clearUserPermissions(userId);
        clearUserRoutes(userId);
        log.debug("清除用户缓存: {}", userId);
    }

    @Override
    public void clearUserPermissions(Long userId) {
        String key = USER_PERMS_PREFIX + userId;
        redisTemplate.delete(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getUserPermissions(Long userId) {
        String key = USER_PERMS_PREFIX + userId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            return objectMapper.convertValue(value, new TypeReference<Set<String>>() {});
        }
        return null;
    }

    @Override
    public void setUserPermissions(Long userId, Set<String> permissions) {
        String key = USER_PERMS_PREFIX + userId;
        redisTemplate.opsForValue().set(key, permissions, USER_CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    // ==================== 路由缓存 ====================

    @Override
    public Object getUserRoutes(Long userId) {
        String key = USER_ROUTES_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void setUserRoutes(Long userId, Object routes) {
        String key = USER_ROUTES_PREFIX + userId;
        redisTemplate.opsForValue().set(key, routes, USER_CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    @Override
    public void clearUserRoutes(Long userId) {
        String key = USER_ROUTES_PREFIX + userId;
        redisTemplate.delete(key);
    }

    @Override
    public void clearAllUserRoutes() {
        deleteByPattern(USER_ROUTES_PREFIX + "*");
        log.info("清除所有用户路由缓存");
    }

    // ==================== 配置缓存 ====================

    @Override
    public String getConfig(String configKey) {
        String key = CONFIG_PREFIX + configKey;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    @Override
    public void setConfig(String configKey, String value) {
        String key = CONFIG_PREFIX + configKey;
        redisTemplate.opsForValue().set(key, value, CONFIG_CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    @Override
    public void clearConfig(String configKey) {
        String key = CONFIG_PREFIX + configKey;
        redisTemplate.delete(key);
    }

    // ==================== 通用缓存操作 ====================

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @Override
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void deleteByPattern(String pattern) {
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    @Override
    public void clearAll() {
        // 注意：生产环境慎用，应该只清除应用相关的缓存
        deleteByPattern("cache:*");
        log.warn("清除所有缓存");
    }
}
