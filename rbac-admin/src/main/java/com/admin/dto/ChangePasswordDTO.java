package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求 DTO
 */
@Data
public class ChangePasswordDTO {

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度为 6-20 个字符")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "新密码必须包含字母和数字")
    private String newPassword;
}
