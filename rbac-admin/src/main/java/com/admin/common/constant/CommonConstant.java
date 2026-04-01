package com.admin.common.constant;

/**
 * 公共常量
 */
public interface CommonConstant {

    /** 成功状态 */
    int STATUS_SUCCESS = 0;

    /** 失败状态 */
    int STATUS_FAIL = 1;

    /** 正常状态 */
    int STATUS_NORMAL = 1;

    /** 停用状态 */
    int STATUS_DISABLE = 0;

    /** 是否为菜单（是） */
    int YES = 1;

    /** 是否为菜单（否） */
    int NO = 0;

    /** 菜单类型 - 目录 */
    int MENU_TYPE_DIR = 1;

    /** 菜单类型 - 菜单 */
    int MENU_TYPE_MENU = 2;

    /** 菜单类型 - 按钮 */
    int MENU_TYPE_BUTTON = 3;

    /** 数据范围 - 全部数据 */
    int DATA_SCOPE_ALL = 1;

    /** 数据范围 - 自定义 */
    int DATA_SCOPE_CUSTOM = 2;

    /** 数据范围 - 本部门 */
    int DATA_SCOPE_DEPT = 3;

    /** 数据范围 - 本部门及以下 */
    int DATA_SCOPE_DEPT_AND_CHILD = 4;

    /** 数据范围 - 仅本人 */
    int DATA_SCOPE_SELF = 5;

    /** 超级管理员角色编码 */
    String ADMIN_ROLE_CODE = "admin";

    /** Redis Key 前缀 */
    String REDIS_PREFIX = "admin:";

    /** Redis - Token 前缀 */
    String REDIS_TOKEN_PREFIX = REDIS_PREFIX + "login:token:";

    /** Redis - 用户路由前缀 */
    String REDIS_USER_ROUTES_PREFIX = REDIS_PREFIX + "user:routes:";

    /** Redis - 字典缓存前缀 */
    String REDIS_DICT_PREFIX = REDIS_PREFIX + "dict:";

    /** Redis - 在线用户前缀 */
    String REDIS_ONLINE_PREFIX = REDIS_PREFIX + "online:";

    /** Redis - 登录锁定前缀 */
    String REDIS_LOGIN_LOCK_PREFIX = REDIS_PREFIX + "login:lock:";
}
