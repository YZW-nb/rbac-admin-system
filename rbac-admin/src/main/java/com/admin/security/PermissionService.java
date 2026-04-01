package com.admin.security;

import com.admin.common.constant.CommonConstant;
import com.admin.entity.SysUser;
import com.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 权限校验服务（供 @PreAuthorize 使用）
 * 在 Controller 中使用：@PreAuthorize("@ss.hasPerm('sys:user:list')")
 */
@Component("ss")
@RequiredArgsConstructor
public class PermissionService {

    private final UserService userService;

    /**
     * 校验是否拥有某权限
     */
    public boolean hasPerm(String permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        // 超级管理员拥有所有权限
        if (auth.getPrincipal() instanceof SysUser user) {
            List<String> roleCodes = userService.getUserRoleCodes(user.getId());
            if (roleCodes.contains("admin")) {
                return true;
            }
        }
        // 校验权限标识
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(permission));
    }

    /**
     * 校验是否拥有某角色
     */
    public boolean hasRole(String roleCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + roleCode));
    }
}
