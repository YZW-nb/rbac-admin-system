-- =====================================================
-- AI 聊天会话表
-- =====================================================
DROP TABLE IF EXISTS sys_ai_chat CASCADE;
CREATE TABLE sys_ai_chat (
    id          BIGSERIAL       PRIMARY KEY,
    title       VARCHAR(200)    DEFAULT ''       NOT NULL,  -- 对话标题
    user_id     BIGINT          DEFAULT NULL     NULL,      -- 用户ID
    username    VARCHAR(50)     DEFAULT ''       NULL,      -- 用户名
    status      SMALLINT        DEFAULT 1        NOT NULL,  -- 状态：1正常 0删除
    create_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NOT NULL,
    update_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NOT NULL
);

COMMENT ON TABLE sys_ai_chat IS 'AI 聊天会话表';
CREATE INDEX idx_ai_chat_user ON sys_ai_chat(user_id);
CREATE INDEX idx_ai_chat_create ON sys_ai_chat(create_time DESC);

-- =====================================================
-- AI 聊天消息表
-- =====================================================
DROP TABLE IF EXISTS sys_ai_message CASCADE;
CREATE TABLE sys_ai_message (
    id          BIGSERIAL       PRIMARY KEY,
    chat_id     BIGINT          DEFAULT NULL     NOT NULL,  -- 会话ID
    role        VARCHAR(20)     DEFAULT ''       NOT NULL,  -- 角色：user/assistant
    content     TEXT            DEFAULT ''       NOT NULL,  -- 消息内容
    sources     TEXT            DEFAULT NULL     NULL,      -- 引用来源（JSON）
    rating      VARCHAR(10)     DEFAULT ''       NULL,      -- 评价：up/down
    create_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NOT NULL
);

COMMENT ON TABLE sys_ai_message IS 'AI 聊天消息表';
CREATE INDEX idx_ai_message_chat ON sys_ai_message(chat_id);
CREATE INDEX idx_ai_message_create ON sys_ai_message(create_time);
