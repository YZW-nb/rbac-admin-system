package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 聊天会话 DTO
 */
@Data
public class ChatDTO {

    /** 对话标题 */
    @NotBlank(message = "对话标题不能为空")
    @Size(max = 100, message = "对话标题不能超过 100 个字符")
    private String title;
}
