package com.admin.service;

import com.admin.entity.SysRiskAlert;

/**
 * 风险检测服务接口
 */
public interface RiskDetectService {

    /**
     * 检测异常访问
     * <p>
     * 规则：1分钟内同一用户访问敏感接口超过阈值（默认10次），触发 AI 分析
     * </p>
     *
     * @param username  用户名
     * @param ip        IP 地址
     * @param api       请求接口
     * @param method    请求方法
     */
    void detectAbnormalAccess(String username, String ip, String api, String method);

    /**
     * 获取未处理的风险告警
     */
    java.util.List<SysRiskAlert> getUnhandledAlerts();

    /**
     * 处理风险告警
     *
     * @param alertId   告警ID
     * @param handledBy 处理人
     * @param status    处理状态（1已处理 2忽略）
     */
    void handleAlert(Long alertId, String handledBy, Integer status);
}
