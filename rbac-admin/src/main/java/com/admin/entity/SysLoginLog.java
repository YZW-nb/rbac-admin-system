package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志实体
 */
@Data
@TableName("sys_login_log")
public class SysLoginLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String ip;

    private String location;

    private String browser;

    private String os;

    /** 状态：0成功 1失败 */
    private Integer status;

    private String message;

    private LocalDateTime loginTime;
}
