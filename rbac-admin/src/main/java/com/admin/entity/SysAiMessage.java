package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 聊天消息实体
 */
@Data
@TableName("sys_ai_message")
public class SysAiMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    private Long chatId;

    /** 角色：user/assistant */
    private String role;

    /** 消息内容 */
    private String content;

    /** 引用来源（JSON） */
    private String sources;

    /** 评价：up/down */
    private String rating;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
