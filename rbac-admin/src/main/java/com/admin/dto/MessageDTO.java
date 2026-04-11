package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 聊天消息 DTO
 */
@Data
public class MessageDTO {

    /** 会话ID */
    @NotNull(message = "会话ID不能为空")
    private Long chatId;

    /** 消息内容 */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /** 消息ID（前端生成） */
    private Long messageId;
}
