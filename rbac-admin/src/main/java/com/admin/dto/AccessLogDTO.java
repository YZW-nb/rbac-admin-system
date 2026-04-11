package com.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访问日志 DTO（用于 AI 分析）
 */
@Data
public class AccessLogDTO {

    /** 用户名 */
    private String username;

    /** IP 地址 */
    private String ip;

    /** 请求接口 */
    private String api;

    /** 请求方法 */
    private String method;

    /** 访问时间 */
    private LocalDateTime accessTime;
}
