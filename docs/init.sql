-- =====================================================
-- RBAC 权限管理系统 - PostgreSQL 建表脚本
-- 执行顺序：先建表 → 再插入初始数据
-- =====================================================

-- 创建数据库（如需）
-- CREATE DATABASE rbac_admin ENCODING 'UTF8';

-- =====================================================
-- 1. 部门表
-- =====================================================
DROP TABLE IF EXISTS sys_dept CASCADE;
CREATE TABLE sys_dept (
    id          BIGSERIAL       PRIMARY KEY,
    parent_id   BIGINT          DEFAULT 0        NOT NULL,
    dept_name   VARCHAR(50)     DEFAULT ''       NOT NULL,
    sort        INT             DEFAULT 0        NOT NULL,
    leader      VARCHAR(50)     DEFAULT ''       NULL,
    phone       VARCHAR(20)     DEFAULT ''       NULL,
    email       VARCHAR(100)    DEFAULT ''       NULL,
    status      SMALLINT        DEFAULT 1        NOT NULL,
    del_flag    SMALLINT        DEFAULT 0        NOT NULL,
    create_by   VARCHAR(50)     DEFAULT ''       NULL,
    create_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NULL,
    update_by   VARCHAR(50)     DEFAULT ''       NULL,
    update_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NULL
);

COMMENT ON TABLE  sys_dept              IS '部门表';
COMMENT ON COLUMN sys_dept.id           IS '部门ID';
COMMENT ON COLUMN sys_dept.parent_id    IS '父部门ID（0为顶级）';
COMMENT ON COLUMN sys_dept.dept_name    IS '部门名称';
COMMENT ON COLUMN sys_dept.sort         IS '显示排序';
COMMENT ON COLUMN sys_dept.leader       IS '负责人';
COMMENT ON COLUMN sys_dept.phone        IS '联系电话';
COMMENT ON COLUMN sys_dept.email        IS '邮箱';
COMMENT ON COLUMN sys_dept.status       IS '状态（1正常 0停用）';
COMMENT ON COLUMN sys_dept.del_flag     IS '删除标志（0存在 1删除）';

-- =====================================================
-- 2. 用户表
-- =====================================================
DROP TABLE IF EXISTS sys_user CASCADE;
CREATE TABLE sys_user (
    id          BIGSERIAL       PRIMARY KEY,
    username    VARCHAR(50)     DEFAULT ''       NOT NULL,
    password    VARCHAR(200)    DEFAULT ''       NOT NULL,
    nickname    VARCHAR(50)     DEFAULT ''       NULL,
    email       VARCHAR(100)    DEFAULT ''       NULL,
    phone       VARCHAR(20)     DEFAULT ''       NULL,
    avatar      VARCHAR(500)    DEFAULT ''       NULL,
    dept_id     BIGINT          DEFAULT NULL     NULL,
    status      SMALLINT        DEFAULT 1        NOT NULL,
    login_ip    VARCHAR(50)     DEFAULT ''       NULL,
    login_time  TIMESTAMP       DEFAULT NULL     NULL,
    del_flag    SMALLINT        DEFAULT 0        NOT NULL,
    create_by   VARCHAR(50)     DEFAULT ''       NULL,
    create_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NULL,
    update_by   VARCHAR(50)     DEFAULT ''       NULL,
    update_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NULL
);

COMMENT ON TABLE  sys_user              IS '用户表';
COMMENT ON COLUMN sys_user.id           IS '用户ID';
COMMENT ON COLUMN sys_user.username     IS '用户名';
COMMENT ON COLUMN sys_user.password     IS '密码（BCrypt加密）';
COMMENT ON COLUMN sys_user.nickname     IS '昵称';
COMMENT ON COLUMN sys_user.avatar       IS '头像地址';
COMMENT ON COLUMN sys_user.dept_id      IS '部门ID';
COMMENT ON COLUMN sys_user.status       IS '状态（1正常 0停用）';
COMMENT ON COLUMN sys_user.login_ip     IS '最后登录IP';
COMMENT ON COLUMN sys_user.login_time   IS '最后登录时间';
COMMENT ON COLUMN sys_user.del_flag     IS '删除标志（0存在 1删除）';

CREATE UNIQUE INDEX idx_user_username ON sys_user(username) WHERE del_flag = 0;

-- =====================================================
-- 3. 角色表
-- =====================================================
DROP TABLE IF EXISTS sys_role CASCADE;
CREATE TABLE sys_role (
    id          BIGSERIAL       PRIMARY KEY,
    role_name   VARCHAR(50)     DEFAULT ''       NOT NULL,
    role_code   VARCHAR(50)     DEFAULT ''       NOT NULL,
    sort        INT             DEFAULT 0        NOT NULL,
    status      SMALLINT        DEFAULT 1        NOT NULL,
    data_scope  SMALLINT        DEFAULT 1        NOT NULL,
    remark      VARCHAR(500)    DEFAULT NULL     NULL,
    del_flag    SMALLINT        DEFAULT 0        NOT NULL,
    create_by   VARCHAR(50)     DEFAULT ''       NULL,
    create_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NULL,
    update_by   VARCHAR(50)     DEFAULT ''       NULL,
    update_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NULL
);

COMMENT ON TABLE  sys_role              IS '角色表';
COMMENT ON COLUMN sys_role.id           IS '角色ID';
COMMENT ON COLUMN sys_role.role_name    IS '角色名称';
COMMENT ON COLUMN sys_role.role_code    IS '角色编码';
COMMENT ON COLUMN sys_role.sort         IS '显示排序';
COMMENT ON COLUMN sys_role.status       IS '状态（1正常 0停用）';
COMMENT ON COLUMN sys_role.data_scope   IS '数据范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人）';
COMMENT ON COLUMN sys_role.del_flag     IS '删除标志（0存在 1删除）';

CREATE UNIQUE INDEX idx_role_code ON sys_role(role_code) WHERE del_flag = 0;

-- =====================================================
-- 4. 菜单表
-- =====================================================
DROP TABLE IF EXISTS sys_menu CASCADE;
CREATE TABLE sys_menu (
    id          BIGSERIAL       PRIMARY KEY,
    parent_id   BIGINT          DEFAULT 0        NOT NULL,
    menu_name   VARCHAR(50)     DEFAULT ''       NOT NULL,
    menu_type   SMALLINT        DEFAULT 0        NOT NULL,
    path        VARCHAR(200)    DEFAULT ''       NULL,
    component   VARCHAR(200)    DEFAULT ''       NULL,
    perms       VARCHAR(100)    DEFAULT ''       NULL,
    icon        VARCHAR(50)     DEFAULT ''       NULL,
    sort        INT             DEFAULT 0        NOT NULL,
    visible     SMALLINT        DEFAULT 1        NOT NULL,
    status      SMALLINT        DEFAULT 1        NOT NULL,
    create_by   VARCHAR(50)     DEFAULT ''       NULL,
    create_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NULL,
    update_by   VARCHAR(50)     DEFAULT ''       NULL,
    update_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NULL
);

COMMENT ON TABLE  sys_menu              IS '菜单表';
COMMENT ON COLUMN sys_menu.id           IS '菜单ID';
COMMENT ON COLUMN sys_menu.parent_id    IS '父菜单ID（0为顶级）';
COMMENT ON COLUMN sys_menu.menu_name    IS '菜单名称';
COMMENT ON COLUMN sys_menu.menu_type    IS '类型（1目录 2菜单 3按钮）';
COMMENT ON COLUMN sys_menu.path         IS '路由地址';
COMMENT ON COLUMN sys_menu.component    IS '组件路径';
COMMENT ON COLUMN sys_menu.perms        IS '权限标识';
COMMENT ON COLUMN sys_menu.icon         IS '菜单图标';
COMMENT ON COLUMN sys_menu.sort         IS '显示排序';
COMMENT ON COLUMN sys_menu.visible      IS '是否可见（1显示 0隐藏）';
COMMENT ON COLUMN sys_menu.status       IS '状态（1正常 0停用）';

-- =====================================================
-- 5. 用户角色关联表
-- =====================================================
DROP TABLE IF EXISTS sys_user_role CASCADE;
CREATE TABLE sys_user_role (
    user_id     BIGINT      NOT NULL,
    role_id     BIGINT      NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

COMMENT ON TABLE  sys_user_role IS '用户角色关联表';

-- =====================================================
-- 6. 角色菜单关联表
-- =====================================================
DROP TABLE IF EXISTS sys_role_menu CASCADE;
CREATE TABLE sys_role_menu (
    role_id     BIGINT      NOT NULL,
    menu_id     BIGINT      NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

COMMENT ON TABLE  sys_role_menu IS '角色菜单关联表';

-- =====================================================
-- 7. 字典类型表
-- =====================================================
DROP TABLE IF EXISTS sys_dict_type CASCADE;
CREATE TABLE sys_dict_type (
    id          BIGSERIAL       PRIMARY KEY,
    dict_name   VARCHAR(100)    DEFAULT ''       NOT NULL,
    dict_type   VARCHAR(100)    DEFAULT ''       NOT NULL,
    status      SMALLINT        DEFAULT 1        NOT NULL,
    remark      VARCHAR(500)    DEFAULT NULL     NULL,
    create_by   VARCHAR(50)     DEFAULT ''       NULL,
    create_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NULL,
    update_by   VARCHAR(50)     DEFAULT ''       NULL,
    update_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NULL
);

COMMENT ON TABLE  sys_dict_type         IS '字典类型表';
COMMENT ON COLUMN sys_dict_type.dict_name IS '字典名称';
COMMENT ON COLUMN sys_dict_type.dict_type IS '字典类型';

CREATE UNIQUE INDEX idx_dict_type ON sys_dict_type(dict_type);

-- =====================================================
-- 8. 字典数据表
-- =====================================================
DROP TABLE IF EXISTS sys_dict_data CASCADE;
CREATE TABLE sys_dict_data (
    id          BIGSERIAL       PRIMARY KEY,
    dict_type   VARCHAR(100)    DEFAULT ''       NOT NULL,
    dict_label  VARCHAR(100)    DEFAULT ''       NOT NULL,
    dict_value  VARCHAR(100)    DEFAULT ''       NOT NULL,
    sort        INT             DEFAULT 0        NOT NULL,
    css_class   VARCHAR(100)    DEFAULT NULL     NULL,
    status      SMALLINT        DEFAULT 1        NOT NULL,
    remark      VARCHAR(500)    DEFAULT NULL     NULL,
    create_by   VARCHAR(50)     DEFAULT ''       NULL,
    create_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NULL
);

COMMENT ON TABLE  sys_dict_data         IS '字典数据表';
COMMENT ON COLUMN sys_dict_data.dict_type  IS '字典类型';
COMMENT ON COLUMN sys_dict_data.dict_label IS '字典标签';
COMMENT ON COLUMN sys_dict_data.dict_value IS '字典值';

-- =====================================================
-- 9. 操作日志表
-- =====================================================
DROP TABLE IF EXISTS sys_oper_log CASCADE;
CREATE TABLE sys_oper_log (
    id              BIGSERIAL       PRIMARY KEY,
    title           VARCHAR(100)    DEFAULT ''       NULL,
    method          VARCHAR(200)    DEFAULT ''       NULL,
    request_method  VARCHAR(10)     DEFAULT ''       NULL,
    oper_name       VARCHAR(50)     DEFAULT ''       NULL,
    oper_url        VARCHAR(500)    DEFAULT ''       NULL,
    oper_ip         VARCHAR(50)     DEFAULT ''       NULL,
    oper_param      TEXT            DEFAULT NULL     NULL,
    oper_result     TEXT            DEFAULT NULL     NULL,
    status          SMALLINT        DEFAULT 0        NOT NULL,
    error_msg       TEXT            DEFAULT NULL     NULL,
    oper_time       TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NOT NULL
);

COMMENT ON TABLE  sys_oper_log                 IS '操作日志表';
COMMENT ON COLUMN sys_oper_log.title           IS '操作模块';
COMMENT ON COLUMN sys_oper_log.method          IS '方法名称';
COMMENT ON COLUMN sys_oper_log.request_method  IS 'HTTP请求方法';
COMMENT ON COLUMN sys_oper_log.oper_name       IS '操作人';
COMMENT ON COLUMN sys_oper_log.oper_url        IS '请求URL';
COMMENT ON COLUMN sys_oper_log.oper_ip         IS '操作IP';
COMMENT ON COLUMN sys_oper_log.oper_param      IS '请求参数';
COMMENT ON COLUMN sys_oper_log.oper_result     IS '返回结果';
COMMENT ON COLUMN sys_oper_log.status          IS '状态（0正常 1异常）';
COMMENT ON COLUMN sys_oper_log.business_type   IS '业务操作类型';
COMMENT ON COLUMN sys_oper_log.error_msg       IS '错误消息';

-- =====================================================
-- 10. 登录日志表
-- =====================================================
DROP TABLE IF EXISTS sys_login_log CASCADE;
CREATE TABLE sys_login_log (
    id          BIGSERIAL       PRIMARY KEY,
    username    VARCHAR(50)     DEFAULT ''       NULL,
    ip          VARCHAR(50)     DEFAULT ''       NULL,
    location    VARCHAR(100)    DEFAULT ''       NULL,
    browser     VARCHAR(50)     DEFAULT ''       NULL,
    os          VARCHAR(50)     DEFAULT ''       NULL,
    status      SMALLINT        DEFAULT 0        NOT NULL,
    message     VARCHAR(200)    DEFAULT ''       NULL,
    login_time  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NOT NULL
);

COMMENT ON TABLE  sys_login_log         IS '登录日志表';
COMMENT ON COLUMN sys_login_log.username IS '登录用户名';
COMMENT ON COLUMN sys_login_log.status   IS '登录状态（0成功 1失败）';
COMMENT ON COLUMN sys_login_log.message  IS '提示消息';

-- =====================================================
-- 11. 通知公告表
-- =====================================================
DROP TABLE IF EXISTS sys_notice CASCADE;
CREATE TABLE sys_notice (
    id          BIGSERIAL       PRIMARY KEY,
    title       VARCHAR(100)    DEFAULT ''       NOT NULL,
    content     TEXT            DEFAULT NULL     NULL,
    notice_type SMALLINT        DEFAULT 1        NOT NULL,
    status      SMALLINT        DEFAULT 1        NOT NULL,
    create_by   VARCHAR(50)     DEFAULT ''       NULL,
    create_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NULL,
    update_by   VARCHAR(50)     DEFAULT ''       NULL,
    update_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NULL
);

COMMENT ON TABLE  sys_notice             IS '通知公告表';
COMMENT ON COLUMN sys_notice.title       IS '公告标题';
COMMENT ON COLUMN sys_notice.notice_type IS '类型（1通知 2公告）';
COMMENT ON COLUMN sys_notice.status      IS '状态（1正常 0关闭）';

-- =====================================================
-- 12. 系统配置表
-- =====================================================
DROP TABLE IF EXISTS sys_config CASCADE;
CREATE TABLE sys_config (
    id          BIGSERIAL       PRIMARY KEY,
    config_name VARCHAR(100)    DEFAULT ''       NOT NULL,
    config_key  VARCHAR(100)    DEFAULT ''       NOT NULL,
    config_value VARCHAR(500)   DEFAULT ''       NOT NULL,
    config_type SMALLINT        DEFAULT 1        NOT NULL,
    remark      VARCHAR(500)    DEFAULT NULL     NULL,
    create_by   VARCHAR(50)     DEFAULT ''       NULL,
    create_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NULL,
    update_by   VARCHAR(50)     DEFAULT ''       NULL,
    update_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NULL
);

COMMENT ON TABLE  sys_config              IS '系统配置表';
COMMENT ON COLUMN sys_config.config_name  IS '配置名称';
COMMENT ON COLUMN sys_config.config_key   IS '配置键名';
COMMENT ON COLUMN sys_config.config_value IS '配置键值';
COMMENT ON COLUMN sys_config.config_type  IS '系统内置（1是 0否）';

CREATE UNIQUE INDEX idx_config_key ON sys_config(config_key);


-- =====================================================
-- 初始数据
-- =====================================================

-- -----------------------------------------------------
-- 初始部门
-- -----------------------------------------------------
INSERT INTO sys_dept (id, parent_id, dept_name, sort, leader, status) VALUES
(100, 0, '总公司',    0, '管理员', 1),
(101, 100, '研发部门', 1, '', 1),
(102, 100, '市场部门', 2, '', 1),
(103, 100, '测试部门', 3, '', 1),
(104, 100, '财务部门', 4, '', 1),
(105, 101, '研发一组', 1, '', 1),
(106, 101, '研发二组', 2, '', 1);

SELECT setval('sys_dept_id_seq', (SELECT MAX(id) FROM sys_dept));

-- -----------------------------------------------------
-- 初始用户
-- 密码均为：admin123（BCrypt 加密）
-- -----------------------------------------------------
INSERT INTO sys_user (id, username, password, nickname, dept_id, status) VALUES
(1, 'admin',  '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', 100, 1),
(2, 'ry',     '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '若依',       100, 1);

SELECT setval('sys_user_id_seq', (SELECT MAX(id) FROM sys_user));

-- -----------------------------------------------------
-- 初始角色
-- -----------------------------------------------------
INSERT INTO sys_role (id, role_name, role_code, sort, status, data_scope, remark) VALUES
(1, '超级管理员', 'admin',  1, 1, 1, '超级管理员拥有所有权限'),
(2, '普通角色',   'common', 2, 1, 2, '普通角色');

SELECT setval('sys_role_id_seq', (SELECT MAX(id) FROM sys_role));

-- -----------------------------------------------------
-- 用户角色关联
-- -----------------------------------------------------
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2);

-- -----------------------------------------------------
-- 初始菜单
-- -----------------------------------------------------
-- 一级目录
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort, visible, status) VALUES
(1,  0, '系统管理',  1, 'system',     'setting',    1, 1, 1),
(2,  0, '系统监控',  1, 'monitor',    'monitor',    2, 1, 1),
(3,  0, '系统工具',  1, 'tool',       'tool',       3, 1, 1);

-- 系统管理 - 菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status) VALUES
(100, 1, '用户管理',  2, 'user',     'system/user/index',     'sys:user:list',     'user',      1, 1, 1),
(101, 1, '角色管理',  2, 'role',     'system/role/index',     'sys:role:list',     'peoples',   2, 1, 1),
(102, 1, '菜单管理',  2, 'menu',     'system/menu/index',     'sys:menu:list',     'tree-table',3, 1, 1),
(103, 1, '部门管理',  2, 'dept',     'system/dept/index',     'sys:dept:list',     'tree',      4, 1, 1),
(104, 1, '字典管理',  2, 'dict',     'system/dict/index',     'sys:dict:list',     'dict',      5, 1, 1),
(105, 1, '通知公告',  2, 'notice',   'system/notice/index',   'sys:notice:list',   'message',   6, 1, 1);

-- 用户管理 - 按钮
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort, visible, status) VALUES
(1001, 100, '用户查询', 3, 'sys:user:query',    1, 1, 1),
(1002, 100, '用户新增', 3, 'sys:user:add',      2, 1, 1),
(1003, 100, '用户修改', 3, 'sys:user:edit',     3, 1, 1),
(1004, 100, '用户删除', 3, 'sys:user:remove',   4, 1, 1),
(1005, 100, '重置密码', 3, 'sys:user:resetPwd', 5, 1, 1),
(1006, 100, '导出用户', 3, 'sys:user:export',   6, 1, 1);

-- 角色管理 - 按钮
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort, visible, status) VALUES
(1011, 101, '角色查询', 3, 'sys:role:query',   1, 1, 1),
(1012, 101, '角色新增', 3, 'sys:role:add',     2, 1, 1),
(1013, 101, '角色修改', 3, 'sys:role:edit',    3, 1, 1),
(1014, 101, '角色删除', 3, 'sys:role:remove',  4, 1, 1);

-- 菜单管理 - 按钮
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort, visible, status) VALUES
(1021, 102, '菜单查询', 3, 'sys:menu:query',   1, 1, 1),
(1022, 102, '菜单新增', 3, 'sys:menu:add',     2, 1, 1),
(1023, 102, '菜单修改', 3, 'sys:menu:edit',    3, 1, 1),
(1024, 102, '菜单删除', 3, 'sys:menu:remove',  4, 1, 1);

-- 部门管理 - 按钮
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort, visible, status) VALUES
(1031, 103, '部门查询', 3, 'sys:dept:query',   1, 1, 1),
(1032, 103, '部门新增', 3, 'sys:dept:add',     2, 1, 1),
(1033, 103, '部门修改', 3, 'sys:dept:edit',    3, 1, 1),
(1034, 103, '部门删除', 3, 'sys:dept:remove',  4, 1, 1);

-- 字典管理 - 按钮
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort, visible, status) VALUES
(1041, 104, '字典查询', 3, 'sys:dict:query',   1, 1, 1),
(1042, 104, '字典新增', 3, 'sys:dict:add',     2, 1, 1),
(1043, 104, '字典修改', 3, 'sys:dict:edit',    3, 1, 1),
(1044, 104, '字典删除', 3, 'sys:dict:remove',  4, 1, 1);

-- 通知公告 - 按钮
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort, visible, status) VALUES
(1051, 105, '公告查询', 3, 'sys:notice:query',  1, 1, 1),
(1052, 105, '公告新增', 3, 'sys:notice:add',    2, 1, 1),
(1053, 105, '公告修改', 3, 'sys:notice:edit',   3, 1, 1),
(1054, 105, '公告删除', 3, 'sys:notice:remove', 4, 1, 1);

-- 系统监控 - 菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status) VALUES
(200, 2, '操作日志',  2, 'operlog',   'monitor/operlog/index',   'sys:operlog:list',   'form',      1, 1, 1),
(201, 2, '登录日志',  2, 'loginlog',  'monitor/loginlog/index',  'sys:loginlog:list',  'logininfor',2, 1, 1);

-- 操作日志 - 按钮
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort, visible, status) VALUES
(2001, 200, '日志查询', 3, 'sys:operlog:query',   1, 1, 1),
(2002, 200, '日志删除', 3, 'sys:operlog:remove',  2, 1, 1),
(2003, 200, '日志导出', 3, 'sys:operlog:export',  3, 1, 1);

-- 登录日志 - 按钮
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort, visible, status) VALUES
(2011, 201, '日志查询', 3, 'sys:loginlog:query',  1, 1, 1),
(2012, 201, '日志删除', 3, 'sys:loginlog:remove', 2, 1, 1),
(2013, 201, '日志导出', 3, 'sys:loginlog:export', 3, 1, 1);

-- 系统工具 - 菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status) VALUES
(300, 3, '代码生成',  2, 'gen',     'tool/gen/index',        'tool:gen:list',      'edit',      1, 1, 1),
(301, 3, '系统接口',  2, 'swagger', 'tool/swagger/index',    NULL,                  'swagger',   2, 1, 1);

-- 代码生成 - 按钮
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort, visible, status) VALUES
(3001, 300, '生成查询', 3, 'tool:gen:query',  1, 1, 1),
(3002, 300, '生成代码', 3, 'tool:gen:code',   2, 1, 1);

SELECT setval('sys_menu_id_seq', (SELECT MAX(id) FROM sys_menu));

-- -----------------------------------------------------
-- 角色菜单关联（admin 角色拥有所有菜单权限）
-- -----------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- -----------------------------------------------------
-- 初始字典类型
-- -----------------------------------------------------
INSERT INTO sys_dict_type (id, dict_name, dict_type, status, remark) VALUES
(1, '用户性别',   'sys_user_sex',     1, '用户性别列表'),
(2, '通知类型',   'sys_notice_type',  1, '通知类型列表'),
(3, '通知状态',   'sys_notice_status',1, '通知状态列表'),
(4, '操作类型',   'sys_oper_type',    1, '操作类型列表'),
(5, '系统开关',   'sys_normal_disable',1, '系统开关列表'),
(6, '菜单状态',   'sys_show_hide',    1, '菜单状态列表'),
(7, '数据范围',   'sys_data_scope',   1, '数据范围列表');

SELECT setval('sys_dict_type_id_seq', (SELECT MAX(id) FROM sys_dict_type));

-- -----------------------------------------------------
-- 初始字典数据
-- -----------------------------------------------------
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort, css_class) VALUES
-- 用户性别
('sys_user_sex',      '男',   '0', 1, NULL),
('sys_user_sex',      '女',   '1', 2, NULL),
('sys_user_sex',      '未知', '2', 3, NULL),
-- 通知类型
('sys_notice_type',   '通知', '1', 1, 'el-icon-bell'),
('sys_notice_type',   '公告', '2', 2, 'el-icon-s-flag'),
-- 通知状态
('sys_notice_status', '正常', '1', 1, 'success'),
('sys_notice_status', '关闭', '0', 2, 'danger'),
-- 操作类型
('sys_oper_type',     '查询', '1', 1, 'info'),
('sys_oper_type',     '新增', '2', 2, 'success'),
('sys_oper_type',     '修改', '3', 3, 'warning'),
('sys_oper_type',     '删除', '4', 4, 'danger'),
('sys_oper_type',     '导出', '5', 5, 'info'),
-- 系统开关
('sys_normal_disable','正常', '0', 1, 'success'),
('sys_normal_disable','停用', '1', 2, 'danger'),
-- 菜单状态
('sys_show_hide',     '显示', '0', 1, 'success'),
('sys_show_hide',     '隐藏', '1', 2, 'danger'),
-- 数据范围
('sys_data_scope',    '全部数据',     '1', 1, NULL),
('sys_data_scope',    '自定义数据',   '2', 2, NULL),
('sys_data_scope',    '本部门数据',   '3', 3, NULL),
('sys_data_scope',    '本部门及以下', '4', 4, NULL),
('sys_data_scope',    '仅本人数据',   '5', 5, NULL);

-- -----------------------------------------------------
-- 初始系统配置
-- -----------------------------------------------------
INSERT INTO sys_config (id, config_name, config_key, config_value, config_type, remark) VALUES
(1, '主框架页-默认皮肤样式', 'sys.index.skinName', 'skin-blue', 1, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow'),
(2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 1, '初始化密码 123456'),
(3, '主框架页-侧边栏主题',   'sys.index.sideTheme', 'theme-dark', 1, '深色主题theme-dark，浅色主题theme-light'),
(4, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 1, '是否开启注册用户功能（true开启，false关闭）'),
(5, '账号自助-验证码开关',   'sys.account.captchaEnabled', 'true', 1, '是否开启验证码功能（true开启，false关闭）');

SELECT setval('sys_config_id_seq', (SELECT MAX(id) FROM sys_config));

-- -----------------------------------------------------
-- 初始通知公告
-- -----------------------------------------------------
INSERT INTO sys_notice (id, title, content, notice_type, status, create_by) VALUES
(1, '系统公告', '欢迎使用 RBAC 权限管理系统，初始管理员账号：admin / admin123', 2, 1, 'admin'),
(2, '新功能通知', '系统代码生成功能已上线，可根据数据库表结构一键生成 CRUD 代码。', 1, 1, 'admin');

SELECT setval('sys_notice_id_seq', (SELECT MAX(id) FROM sys_notice));
