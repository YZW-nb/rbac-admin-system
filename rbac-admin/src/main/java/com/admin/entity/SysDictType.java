package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典类型实体
 */
@Data
@TableName("sys_dict_type")
public class SysDictType implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dictName;

    private String dictType;

    private Integer status;

    private String remark;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
