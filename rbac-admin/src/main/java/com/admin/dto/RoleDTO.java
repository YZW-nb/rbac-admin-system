package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色创建/更新 DTO
 */
@Data
public class RoleDTO {

    private Long id;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    private Integer sort;

    @NotNull(message = "状态不能为空")
    private Integer status;

    /** 数据范围 */
    private Integer dataScope;

    private String remark;

    /** 菜单ID列表 */
    private List<Long> menuIds;
}
