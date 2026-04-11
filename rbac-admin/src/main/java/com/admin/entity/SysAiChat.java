package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 聊天会话实体
 */
@Data
@TableName("sys_ai_chat")
public class SysAiChat implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 对话标题 */
    private String title;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 状态：1正常 0删除 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
