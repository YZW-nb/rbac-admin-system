package com.admin.util;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.admin.dto.AccessLogDTO;
import com.admin.dto.AiAnalyzeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 风险分析客户端
 * <p>
 * 支持 OpenAI 兼容 API（如智谱 GLM、阿里通义千问等）
 * </p>
 */
@Slf4j
@Component
public class AiRiskAnalyzer {

    @Value("${admin.ai.analyze-url:}")
    private String analyzeUrl;

    @Value("${admin.ai.api-key:}")
    private String apiKey;

    @Value("${admin.ai.model:gpt-3.5-turbo}")
    private String model;

    /**
     * AI 分析风险
     *
     * @param username    触发用户
     * @param ip          操作 IP
     * @param triggerCount 触发次数
     * @param timeWindow  时间窗口（秒）
     * @param accessLogs  访问日志列表
     * @return AI 分析结果
     */
    public AiAnalyzeResult analyzeRisk(String username, String ip, int triggerCount,
                                       int timeWindow, List<AccessLogDTO> accessLogs) {
        // 未配置 AI API 时，返回默认分析结果
        if (analyzeUrl == null || analyzeUrl.isBlank()) {
            log.warn("[风险检测] AI 分析 URL 未配置，使用默认分析");
            return getDefaultResult(username, triggerCount);
        }

        try {
            // 构建 Prompt
            String prompt = buildPrompt(username, ip, triggerCount, timeWindow, accessLogs);

            // 调用 AI API
            String response = callAiApi(prompt);

            // 解析结果
            return parseAiResponse(response);

        } catch (Exception e) {
            log.error("[AI分析] 调用失败: {}", e.getMessage());
            return getDefaultResult(username, triggerCount);
        }
    }

    /**
     * 构建 AI 分析 Prompt
     * <p>
     * 简洁高效，控制 Token 消耗
     * </p>
     */
    private String buildPrompt(String username, String ip, int triggerCount,
                               int timeWindow, List<AccessLogDTO> accessLogs) {
        // 提取接口列表
        String apiList = accessLogs.stream()
                .map(log -> String.format("%s %s", log.getMethod(), log.getApi()))
                .distinct()
                .collect(Collectors.joining("\n"));

        return String.format("""
            [安全分析任务]
            
            用户:%s | IP:%s | %d次/%d秒
            
            访问接口:
            %s
            
            请分析并返回JSON格式结果:
            {
              "alertType": "brute_force/data_theft/privilege_escalation/abnormal_access/other",
              "riskLevel": "low/medium/high/critical",
              "riskDesc": "风险描述(50字内)",
              "handleSuggest": "处置建议"
            }
            
            只返回JSON，不要其他内容。
            """, username, ip, triggerCount, timeWindow, apiList);
    }

    /**
     * 调用 AI API（OpenAI 兼容格式）
     */
    private String callAiApi(String prompt) {
        JSONObject requestBody = new JSONObject()
                .set("model", model)
                .set("messages", new JSONObject[]{
                        new JSONObject().set("role", "user").set("content", prompt)
                })
                .set("temperature", 0.1)  // 低温度，保证稳定性
                .set("max_tokens", 300);  // 限制输出 Token

        return HttpUtil.createPost(analyzeUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .timeout(30000)
                .execute()
                .body();
    }

    /**
     * 解析 AI 返回结果
     */
    private AiAnalyzeResult parseAiResponse(String response) {
        AiAnalyzeResult result = new AiAnalyzeResult();

        try {
            JSONObject json = JSONUtil.parseObj(response);

            // 兼容不同 API 返回格式
            String content = json.getByPath("choices[0].message.content", String.class);
            if (content == null) {
                content = json.getByPath("choices[0].text", String.class);
            }
            if (content == null) {
                content = json.getStr("content");
            }

            // 提取 JSON 部分
            content = content.trim();
            if (content.contains("```json")) {
                content = content.substring(content.indexOf("```json") + 7);
            }
            if (content.contains("```")) {
                content = content.substring(0, content.indexOf("```"));
            }
            content = content.trim();

            JSONObject resultJson = JSONUtil.parseObj(content);
            result.setAlertType(resultJson.getStr("alertType", "other"));
            result.setRiskLevel(resultJson.getStr("riskLevel", "medium"));
            result.setRiskDesc(resultJson.getStr("riskDesc", "检测到异常访问行为"));
            result.setHandleSuggest(resultJson.getStr("handleSuggest", "建议人工复核"));

        } catch (Exception e) {
            log.warn("[AI解析] 解析失败，使用默认值: {}", e.getMessage());
            return getDefaultResult("", 10);
        }

        return result;
    }

    /**
     * 获取默认分析结果（未配置 AI 时使用）
     */
    private AiAnalyzeResult getDefaultResult(String username, int triggerCount) {
        AiAnalyzeResult result = new AiAnalyzeResult();
        result.setAlertType(triggerCount > 30 ? "brute_force" : "abnormal_access");
        result.setRiskLevel(triggerCount > 50 ? "critical" : "high");
        result.setRiskDesc(String.format("用户 %s 在短时间内高频访问敏感接口", username));
        result.setHandleSuggest("建议立即人工核查，必要时冻结账号");
        return result;
    }
}
