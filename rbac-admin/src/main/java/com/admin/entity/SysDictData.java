package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典数据实体
 */
@Data
@TableName("sys_dict_data")
public class SysDictData implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dictType;

    private String dictLabel;

    private String dictValue;

    private Integer sort;

    private String cssClass;

    private Integer status;

    private String remark;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
