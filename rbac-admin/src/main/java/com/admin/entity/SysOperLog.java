package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String method;

    private String requestMethod;

    private String operName;

    private String operUrl;

    private String operIp;

    private String operParam;

    private String operResult;

    /** 状态：0正常 1异常 */
    private Integer status;

    /** 业务操作类型 */
    private Integer businessType;

    private String errorMsg;

    private LocalDateTime operTime;
}
