package com.admin.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

/**
 * 修改角色状态 DTO
 */
@Data
public class ChangeStatusDTO {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
