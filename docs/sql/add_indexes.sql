-- =============================================
-- RBAC 权限管理系统 - 索引优化 SQL
-- 执行前请确认表名与实际数据库一致
-- =============================================

-- 用户表索引
CREATE INDEX IF NOT EXISTS idx_sys_user_username ON sys_user (username);
CREATE INDEX IF NOT EXISTS idx_sys_user_status ON sys_user (status);
CREATE INDEX IF NOT EXISTS idx_sys_user_dept_id ON sys_user (dept_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_phone ON sys_user (phone);
CREATE INDEX IF NOT EXISTS idx_sys_user_create_time ON sys_user (create_time);

-- 角色表索引
CREATE INDEX IF NOT EXISTS idx_sys_role_code ON sys_role (role_code);
CREATE INDEX IF NOT EXISTS idx_sys_role_status ON sys_role (status);
CREATE INDEX IF NOT EXISTS idx_sys_role_sort ON sys_role (sort);

-- 菜单表索引
CREATE INDEX IF NOT EXISTS idx_sys_menu_parent_id ON sys_menu (parent_id);
CREATE INDEX IF NOT EXISTS idx_sys_menu_sort ON sys_menu (sort);

-- 部门表索引
CREATE INDEX IF NOT EXISTS idx_sys_dept_parent_id ON sys_dept (parent_id);
CREATE INDEX IF NOT EXISTS idx_sys_dept_status ON sys_dept (status);

-- 用户角色关联表索引（核心查询性能优化）
CREATE INDEX IF NOT EXISTS idx_sys_user_role_user_id ON sys_user_role (user_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_role_id ON sys_user_role (role_id);

-- 角色菜单关联表索引
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_role_id ON sys_role_menu (role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_menu_id ON sys_role_menu (menu_id);

-- 字典数据表索引
CREATE INDEX IF NOT EXISTS idx_sys_dict_data_type ON sys_dict_data (dict_type);
CREATE INDEX IF NOT EXISTS idx_sys_dict_data_sort ON sys_dict_data (sort);

-- 字典类型表索引
CREATE INDEX IF NOT EXISTS idx_sys_dict_type_type ON sys_dict_type (dict_type);

-- 登录日志表索引
CREATE INDEX IF NOT EXISTS idx_sys_login_log_username ON sys_login_log (username);
CREATE INDEX IF NOT EXISTS idx_sys_login_log_ip ON sys_login_log (ip);
CREATE INDEX IF NOT EXISTS idx_sys_login_log_time ON sys_login_log (login_time);
CREATE INDEX IF NOT EXISTS idx_sys_login_log_status ON sys_login_log (status);

-- 操作日志表索引
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_title ON sys_oper_log (title);
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_oper_name ON sys_oper_log (oper_name);
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_time ON sys_oper_log (oper_time);

-- 通知公告表索引
CREATE INDEX IF NOT EXISTS idx_sys_notice_type ON sys_notice (notice_type);
CREATE INDEX IF NOT EXISTS idx_sys_notice_status ON sys_notice (status);
