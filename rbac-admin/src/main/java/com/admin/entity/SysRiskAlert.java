package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 风险告警实体
 */
@Data
@TableName("sys_risk_alert")
public class SysRiskAlert implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 风险类型 */
    private String alertType;

    /** 风险等级：low/medium/high/critical */
    private String riskLevel;

    /** 风险描述（50字以内） */
    private String riskDesc;

    /** 处置建议 */
    private String handleSuggest;

    /** 触发用户名 */
    private String username;

    /** 操作IP */
    private String ip;

    /** 触发次数 */
    private Integer triggerCount;

    /** 时间窗口（秒） */
    private Integer timeWindow;

    /** 涉及的敏感接口列表（JSON） */
    private String sensitiveApis;

    /** 原始日志摘要 */
    private String rawLogs;

    /** 处理状态：0未处理 1已处理 2忽略 */
    private Integer status;

    /** 处理人 */
    private String handledBy;

    /** 处理时间 */
    private LocalDateTime handledTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
