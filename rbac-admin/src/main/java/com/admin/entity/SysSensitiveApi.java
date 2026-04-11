package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 敏感接口配置实体
 */
@Data
@TableName("sys_sensitive_api")
public class SysSensitiveApi implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接口路径模式 */
    private String apiPattern;

    /** 接口名称 */
    private String apiName;

    /** 接口类型 */
    private String apiType;

    /** 风险权重 */
    private Integer riskWeight;

    /** 状态：1启用 0禁用 */
    private Integer status;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
