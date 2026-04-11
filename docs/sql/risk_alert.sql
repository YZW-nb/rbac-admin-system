-- =====================================================
-- 风险告警表 - 存储 AI 分析后的风险检测结果
-- =====================================================
DROP TABLE IF EXISTS sys_risk_alert CASCADE;
CREATE TABLE sys_risk_alert (
    id              BIGSERIAL       PRIMARY KEY,
    alert_type      VARCHAR(50)     DEFAULT ''       NOT NULL,  -- 风险类型
    risk_level      VARCHAR(20)     DEFAULT ''       NOT NULL,  -- 风险等级：low/medium/high/critical
    risk_desc       VARCHAR(200)    DEFAULT ''       NOT NULL,  -- 风险描述（50字以内）
    handle_suggest  VARCHAR(500)    DEFAULT ''       NOT NULL,  -- 处置建议
    username        VARCHAR(50)     DEFAULT ''       NULL,      -- 触发用户名
    ip              VARCHAR(50)     DEFAULT ''       NULL,      -- 操作IP
    trigger_count   INT             DEFAULT 0        NOT NULL,  -- 触发次数
    time_window     INT             DEFAULT 60       NOT NULL,  -- 时间窗口（秒）
    sensitive_apis  TEXT            DEFAULT NULL     NULL,      -- 涉及的敏感接口列表（JSON）
    raw_logs        TEXT            DEFAULT NULL     NULL,      -- 原始日志摘要
    status          SMALLINT        DEFAULT 0        NOT NULL,  -- 处理状态：0未处理 1已处理 2忽略
    handled_by      VARCHAR(50)     DEFAULT ''       NULL,      -- 处理人
    handled_time    TIMESTAMP       DEFAULT NULL     NULL,      -- 处理时间
    create_time     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NOT NULL,
    update_time     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NOT NULL
);

COMMENT ON TABLE  sys_risk_alert IS '风险告警表';
COMMENT ON COLUMN sys_risk_alert.alert_type    IS '风险类型：brute_force/data_theft/privilege_escalation/abnormal_access';
COMMENT ON COLUMN sys_risk_alert.risk_level    IS '风险等级：low/medium/high/critical';
COMMENT ON COLUMN sys_risk_alert.risk_desc     IS '风险描述（50字以内）';
COMMENT ON COLUMN sys_risk_alert.handle_suggest IS '处置建议';
COMMENT ON COLUMN sys_risk_alert.username       IS '触发用户名';
COMMENT ON COLUMN sys_risk_alert.trigger_count IS '触发次数';
COMMENT ON COLUMN sys_risk_alert.time_window   IS '时间窗口（秒）';
COMMENT ON COLUMN sys_risk_alert.sensitive_apis IS '涉及的敏感接口列表';
COMMENT ON COLUMN sys_risk_alert.raw_logs      IS '原始日志摘要';
COMMENT ON COLUMN sys_risk_alert.status        IS '处理状态：0未处理 1已处理 2忽略';

-- 索引
CREATE INDEX idx_risk_alert_username ON sys_risk_alert(username);
CREATE INDEX idx_risk_alert_status ON sys_risk_alert(status);
CREATE INDEX idx_risk_alert_create_time ON sys_risk_alert(create_time DESC);

-- =====================================================
-- 敏感接口配置表
-- =====================================================
DROP TABLE IF EXISTS sys_sensitive_api CASCADE;
CREATE TABLE sys_sensitive_api (
    id              BIGSERIAL       PRIMARY KEY,
    api_pattern     VARCHAR(200)    DEFAULT ''       NOT NULL,  -- 接口路径模式（如：/api/system/user/*）
    api_name        VARCHAR(100)    DEFAULT ''       NOT NULL,  -- 接口名称
    api_type        VARCHAR(50)     DEFAULT ''       NOT NULL,  -- 接口类型：data_access/data_modify/system_config
    risk_weight     INT             DEFAULT 1        NOT NULL,  -- 风险权重
    status          SMALLINT        DEFAULT 1        NOT NULL,  -- 状态：1启用 0禁用
    remark          VARCHAR(200)    DEFAULT ''       NULL,
    create_time     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NOT NULL,
    update_time     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NOT NULL
);

COMMENT ON TABLE  sys_sensitive_api IS '敏感接口配置表';
COMMENT ON COLUMN sys_sensitive_api.api_pattern   IS '接口路径模式（支持通配符）';
COMMENT ON COLUMN sys_sensitive_api.api_type     IS '接口类型';
COMMENT ON COLUMN sys_sensitive_api.risk_weight  IS '风险权重';

-- 默认敏感接口数据
INSERT INTO sys_sensitive_api (api_pattern, api_name, api_type, risk_weight, status) VALUES
('/api/system/user/*', '用户管理', 'data_access', 2, 1),
('/api/system/role/*', '角色管理', 'data_access', 2, 1),
('/api/system/menu/*', '菜单管理', 'system_config', 3, 1),
('/api/system/dept/*', '部门管理', 'data_access', 1, 1),
('/api/system/config/*', '系统配置', 'system_config', 3, 1),
('/api/auth/login', '用户登录', 'data_access', 1, 1),
('/api/system/user/export', '用户导出', 'data_modify', 2, 1),
('/api/system/role/export', '角色导出', 'data_modify', 2, 1);

SELECT setval('sys_sensitive_api_id_seq', (SELECT MAX(id) FROM sys_sensitive_api));
