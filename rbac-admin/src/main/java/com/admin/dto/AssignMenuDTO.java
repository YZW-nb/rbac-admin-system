package com.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.util.List;

/**
 * 分配菜单权限 DTO
 */
@Data
public class AssignMenuDTO {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @NotEmpty(message = "菜单ID列表不能为空")
    private List<Long> menuIds;
}
