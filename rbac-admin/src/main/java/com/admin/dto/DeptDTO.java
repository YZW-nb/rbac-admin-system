package com.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 部门创建/更新 DTO
 */
@Data
public class DeptDTO {

    private Long id;

    private Long parentId;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 50, message = "部门名称长度不能超过 50 个字符")
    private String deptName;

    private Integer sort;

    private String leader;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100 个字符")
    private String email;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
