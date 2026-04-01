package com.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 用户创建/更新 DTO
 */
@Data
public class UserDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度为 2-50 个字符")
    private String username;

    /** 密码（新增时必填，修改时不填则不修改） */
    private String password;

    /** 昵称 */
    @Size(max = 50, message = "昵称长度不能超过 50 个字符")
    private String nickname;

    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100 个字符")
    private String email;

    /** 手机号 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String avatar;

    private Long deptId;

    @NotNull(message = "状态不能为空")
    private Integer status;

    /** 角色ID列表 */
    private List<Long> roleIds;
}
