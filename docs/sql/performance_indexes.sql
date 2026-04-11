-- =============================================
-- 数据库性能优化：添加索引
-- =============================================

-- 1. 用户表索引优化
-- 用户名唯一索引（已有）
CREATE UNIQUE INDEX IF NOT EXISTS idx_sys_user_username ON sys_user(username);
-- 部门索引（查询用户时按部门筛选）
CREATE INDEX IF NOT EXISTS idx_sys_user_dept_id ON sys_user(dept_id);
-- 状态索引（查询正常用户时使用）
CREATE INDEX IF NOT EXISTS idx_sys_user_status ON sys_user(status);
-- 手机号索引（登录时按手机号查询）
CREATE INDEX IF NOT EXISTS idx_sys_user_phone ON sys_user(phone);

-- 2. 角色表索引优化
-- 角色编码唯一索引（已有）
CREATE UNIQUE INDEX IF NOT EXISTS idx_sys_role_code ON sys_role(code);

-- 3. 菜单表索引优化
-- 菜单编码索引
CREATE INDEX IF NOT EXISTS idx_sys_menu_perms ON sys_menu(perms);
-- 父菜单索引（构建菜单树时使用）
CREATE INDEX IF NOT EXISTS idx_sys_menu_parent_id ON sys_menu(parent_id);
-- 菜单状态索引
CREATE INDEX IF NOT EXISTS idx_sys_menu_status ON sys_menu(status);

-- 4. 部门表索引优化
-- 父部门索引（构建部门树时使用）
CREATE INDEX IF NOT EXISTS idx_sys_dept_parent_id ON sys_dept(parent_id);
-- 部门状态索引
CREATE INDEX IF NOT EXISTS idx_sys_dept_status ON sys_dept(status);

-- 5. 字典类型表索引优化
-- 字典编码唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_sys_dict_type_code ON sys_dict_type(dict_type);

-- 6. 字典数据表索引优化
-- 字典类型索引（按类型查询数据）
CREATE INDEX IF NOT EXISTS idx_sys_dict_data_dict_type ON sys_dict_data(dict_type);
-- 字典状态和排序索引
CREATE INDEX IF NOT EXISTS idx_sys_dict_data_status_sort ON sys_dict_data(status, dict_sort);

-- 7. 操作日志表索引优化（历史数据量大，需要索引）
-- 操作人索引
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_oper_name ON sys_oper_log(oper_name);
-- 操作时间索引（按时间范围查询）
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_oper_time ON sys_oper_log(oper_time);
-- 业务类型索引
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_business_type ON sys_oper_log(business_type);

-- 8. 登录日志表索引优化
-- 登录时间索引
CREATE INDEX IF NOT EXISTS idx_sys_login_log_login_time ON sys_login_log(login_time);
-- 用户名索引
CREATE INDEX IF NOT EXISTS idx_sys_login_log_username ON sys_login_log(username);
-- IP 地址索引（查询 IP 归属时使用）
CREATE INDEX IF NOT EXISTS idx_sys_login_log_ipaddr ON sys_login_log(ipaddr);

-- 9. AI 聊天表索引优化
-- 用户 ID 索引（查询用户聊天记录）
CREATE INDEX IF NOT EXISTS idx_sys_ai_chat_user_id ON sys_ai_chat(user_id);
-- 创建时间索引（按时间排序）
CREATE INDEX IF NOT EXISTS idx_sys_ai_chat_create_time ON sys_ai_chat(create_time DESC);

-- 10. AI 消息表索引优化
-- 会话 ID 索引（查询会话中的消息）
CREATE INDEX IF NOT EXISTS idx_sys_ai_message_chat_id ON sys_ai_message(chat_id);
-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_sys_ai_message_create_time ON sys_ai_message(create_time);
-- 消息角色索引（按角色筛选）
CREATE INDEX IF NOT EXISTS idx_sys_ai_message_role ON sys_ai_message(role);

-- 11. 系统配置表索引优化
-- 配置键索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_sys_config_config_key ON sys_config(config_key);

-- 12. 风险告警表索引优化
-- 告警类型索引
CREATE INDEX IF NOT EXISTS idx_sys_risk_alert_alert_type ON sys_risk_alert(alert_type);
-- 告警状态索引
CREATE INDEX IF NOT EXISTS idx_sys_risk_alert_status ON sys_risk_alert(status);
-- 告警时间索引
CREATE INDEX IF NOT EXISTS idx_sys_risk_alert_create_time ON sys_risk_alert(create_time DESC);
-- 用户 ID 索引
CREATE INDEX IF NOT EXISTS idx_sys_risk_alert_user_id ON sys_risk_alert(user_id);

-- 13. 敏感 API 表索引优化
-- API 路径索引
CREATE INDEX IF NOT EXISTS idx_sys_sensitive_api_api_path ON sys_sensitive_api(api_path);
-- API 类型索引
CREATE INDEX IF NOT EXISTS idx_sys_sensitive_api_api_type ON sys_sensitive_api(api_type);

-- =============================================
-- 公告表索引
-- =============================================
-- 公告类型索引
CREATE INDEX IF NOT EXISTS idx_sys_notice_notice_type ON sys_notice(notice_type);
-- 公告状态索引
CREATE INDEX IF NOT EXISTS idx_sys_notice_status ON sys_notice(status);
