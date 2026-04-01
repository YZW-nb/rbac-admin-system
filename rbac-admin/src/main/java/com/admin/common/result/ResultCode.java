package com.admin.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),

    // 认证相关 401
    UNAUTHORIZED(401, "未认证，请先登录"),
    TOKEN_EXPIRED(401, "Token 已过期"),
    TOKEN_INVALID(401, "Token 无效"),

    // 权限相关 403
    FORBIDDEN(403, "没有操作权限"),

    // 参数相关 400
    PARAM_ERROR(400, "参数错误"),
    PARAM_IS_NULL(400, "参数不能为空"),

    // 资源相关 404
    NOT_FOUND(404, "资源不存在"),

    // 业务相关
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_EXISTS(1004, "用户名已存在"),
    OLD_PASSWORD_ERROR(1005, "旧密码错误"),

    ROLE_EXISTS(2001, "角色编码已存在"),
    ROLE_NOT_FOUND(2003, "角色不存在"),
    CANNOT_DELETE_ADMIN_ROLE(2004, "不能删除超级管理员角色"),
    ROLE_HAS_USERS(2002, "角色已分配用户，不能删除"),

    MENU_HAS_CHILDREN(3001, "存在子菜单，不能删除"),
    MENU_HAS_ROLE(3002, "菜单已分配角色，不能删除"),

    DEPT_HAS_CHILDREN(4001, "存在子部门，不能删除"),
    DEPT_HAS_USERS(4002, "部门下存在用户，不能删除");

    private final int code;
    private final String message;
}
