package com.admin.common.controller;

import com.admin.entity.SysUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 控制器基类
 */
public abstract class BaseController {

    /**
     * 获取当前登录用户 ID
     */
    protected Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SysUser user) {
            return user.getId();
        }
        return null;
    }

    /**
     * 获取当前登录用户名
     */
    protected String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SysUser user) {
            return user.getUsername();
        }
        return "unknown";
    }
}
