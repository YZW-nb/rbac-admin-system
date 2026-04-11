package com.admin.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * 统一管理应用中的各种缓存
 */
public interface CacheService {

    // ==================== 字典缓存 ====================

    /**
     * 获取字典数据
     */
    Object getDictData(String dictType, String dictKey);

    /**
     * 设置字典数据
     */
    void setDictData(String dictType, String dictKey, Object value);

    /**
     * 获取字典类型下的所有数据
     */
    List<?> getDictType(String dictType);

    /**
     * 设置字典类型下的所有数据
     */
    void setDictType(String dictType, List<?> data);

    /**
     * 清除字典缓存
     */
    void clearDictCache(String dictType);

    /**
     * 清除所有字典缓存
     */
    void clearAllDictCache();

    // ==================== 用户缓存 ====================

    /**
     * 获取用户信息
     */
    Object getUser(Long userId);

    /**
     * 设置用户信息
     */
    void setUser(Long userId, Object user);

    /**
     * 清除用户缓存
     */
    void clearUser(Long userId);

    /**
     * 清除用户权限缓存
     */
    void clearUserPermissions(Long userId);

    /**
     * 获取用户权限列表
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 设置用户权限列表
     */
    void setUserPermissions(Long userId, Set<String> permissions);

    // ==================== 路由缓存 ====================

    /**
     * 获取用户路由
     */
    Object getUserRoutes(Long userId);

    /**
     * 设置用户路由
     */
    void setUserRoutes(Long userId, Object routes);

    /**
     * 清除用户路由缓存
     */
    void clearUserRoutes(Long userId);

    /**
     * 清除所有用户路由缓存
     */
    void clearAllUserRoutes();

    // ==================== 配置缓存 ====================

    /**
     * 获取配置值
     */
    String getConfig(String configKey);

    /**
     * 设置配置值
     */
    void setConfig(String configKey, String value);

    /**
     * 清除配置缓存
     */
    void clearConfig(String configKey);

    // ==================== 通用缓存操作 ====================

    /**
     * 设置缓存（带过期时间）
     */
    void set(String key, Object value, long timeout, TimeUnit unit);

    /**
     * 设置缓存（永不过期）
     */
    void set(String key, Object value);

    /**
     * 获取缓存
     */
    Object get(String key);

    /**
     * 删除缓存
     */
    void delete(String key);

    /**
     * 判断缓存是否存在
     */
    boolean exists(String key);

    /**
     * 批量删除缓存（支持通配符）
     */
    void deleteByPattern(String pattern);

    /**
     * 清空所有缓存
     */
    void clearAll();
}
