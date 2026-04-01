package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典数据 DTO
 */
@Data
public class DictDataDTO {

    private Long id;

    @NotBlank(message = "字典类型不能为空")
    @Size(max = 50, message = "字典类型长度不能超过 50 个字符")
    private String dictType;

    @NotBlank(message = "字典标签不能为空")
    @Size(max = 50, message = "字典标签长度不能超过 50 个字符")
    private String dictLabel;

    @NotBlank(message = "字典键值不能为空")
    @Size(max = 50, message = "字典键值长度不能超过 50 个字符")
    private String dictValue;

    private Integer sort;

    private String cssClass;

    private Integer status;
}
