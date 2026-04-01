package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 通知公告创建/更新 DTO
 */
@Data
public class NoticeDTO {

    private Long id;

    @NotBlank(message = "公告标题不能为空")
    @Size(max = 100, message = "公告标题长度不能超过 100 个字符")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    @NotNull(message = "公告类型不能为空")
    private Integer noticeType;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
