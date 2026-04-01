package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置实体
 */
@Data
@TableName("sys_config")
public class SysConfig implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String configName;

    private String configKey;

    private String configValue;

    /** 是否系统内置：1是 0否 */
    private Integer configType;

    private String remark;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
