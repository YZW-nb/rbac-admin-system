package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典类型 DTO
 */
@Data
public class DictTypeDTO {

    private Long id;

    @NotBlank(message = "字典名称不能为空")
    @Size(max = 50, message = "字典名称长度不能超过 50 个字符")
    private String dictName;

    @NotBlank(message = "字典类型编码不能为空")
    @Size(max = 50, message = "字典类型编码长度不能超过 50 个字符")
    private String dictType;

    private Integer status;

    private String remark;
}
