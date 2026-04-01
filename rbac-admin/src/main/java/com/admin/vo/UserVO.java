package com.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息 VO（登录后返回）
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private Long deptId;

    private String deptName;

    private List<SysRoleVO> roles;

    /** 权限标识列表 */
    private List<String> permissions;

    /** 菜单路由列表（用于前端动态路由） */
    private List<RouterVO> routes;
}
