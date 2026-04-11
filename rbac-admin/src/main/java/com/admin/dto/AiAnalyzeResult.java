package com.admin.dto;

import lombok.Data;

/**
 * AI 风险分析结果 DTO
 */
@Data
public class AiAnalyzeResult {

    /**
     * 风险类型
     * - brute_force: 暴力破解
     * - data_theft: 数据窃取
     * - privilege_escalation: 权限提升
     * - abnormal_access: 异常访问
     * - other: 其他
     */
    private String alertType;

    /**
     * 风险等级：low/medium/high/critical
     */
    private String riskLevel;

    /**
     * 风险描述（50字以内）
     */
    private String riskDesc;

    /**
     * 处置建议
     */
    private String handleSuggest;
}
