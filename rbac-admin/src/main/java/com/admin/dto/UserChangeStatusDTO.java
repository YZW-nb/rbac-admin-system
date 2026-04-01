package com.admin.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

/**
 * 修改用户状态 DTO
 */
@Data
public class UserChangeStatusDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
