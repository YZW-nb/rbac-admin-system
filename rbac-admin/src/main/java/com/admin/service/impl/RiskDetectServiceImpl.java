package com.admin.service.impl;

import com.admin.dto.AccessLogDTO;
import com.admin.dto.AiAnalyzeResult;
import com.admin.entity.SysRiskAlert;
import com.admin.entity.SysSensitiveApi;
import com.admin.mapper.SysRiskAlertMapper;
import com.admin.mapper.SysSensitiveApiMapper;
import com.admin.service.RiskDetectService;
import com.admin.util.AiRiskAnalyzer;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 风险检测服务实现
 * <p>
 * 使用 Redis 计数器实现滑动窗口计数
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskDetectServiceImpl implements RiskDetectService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SysSensitiveApiMapper sensitiveApiMapper;
    private final SysRiskAlertMapper riskAlertMapper;
    private final AiRiskAnalyzer aiRiskAnalyzer;
    private final ObjectMapper objectMapper;

    /** 触发阈值：1分钟内访问敏感接口次数超过此值则触发分析 */
    @Value("${admin.risk.threshold:10}")
    private int threshold;

    /** 时间窗口（秒） */
    @Value("${admin.risk.time-window:60}")
    private int timeWindow;

    /** 是否启用风险检测 */
    @Value("${admin.risk.enabled:true}")
    private boolean riskDetectionEnabled;

    /** Redis Key 前缀 */
    private static final String REDIS_KEY_PREFIX = "risk:access:";

    @Override
    public void detectAbnormalAccess(String username, String ip, String api, String method) {
        // 未启用检测时直接返回
        if (!riskDetectionEnabled) {
            return;
        }

        // 非敏感接口跳过
        if (!isSensitiveApi(api)) {
            return;
        }

        // 构建 Redis Key：用户 + 接口模式
        String sensitivePattern = getMatchedPattern(api);
        String redisKey = REDIS_KEY_PREFIX + username + ":" + sensitivePattern;

        try {
            // Redis INCR 并设置过期时间（滑动窗口）
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count == null) {
                count = 1L;
            }

            // 首次访问设置过期时间
            if (count == 1) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(timeWindow));
            }

            log.debug("[风险检测] 用户:{} 访问:{} 当前计数:{}/{}", username, api, count, threshold);

            // 超过阈值，触发 AI 分析
            if (count > threshold) {
                // 使用 @Async 异步执行，避免阻塞主流程
                asyncTriggerAiAnalysis(username, ip, sensitivePattern);
            }

        } catch (Exception e) {
            log.error("[风险检测] Redis 操作异常: {}", e.getMessage());
        }
    }

    /**
     * 判断是否为敏感接口
     */
    private boolean isSensitiveApi(String api) {
        // 查询数据库中的敏感接口配置
        List<SysSensitiveApi> sensitiveApis = sensitiveApiMapper.selectList(
                new LambdaQueryWrapper<SysSensitiveApi>()
                        .eq(SysSensitiveApi::getStatus, 1)
        );

        for (SysSensitiveApi sensitiveApi : sensitiveApis) {
            if (matchApiPattern(api, sensitiveApi.getApiPattern())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取匹配的接口模式
     */
    private String getMatchedPattern(String api) {
        List<SysSensitiveApi> sensitiveApis = sensitiveApiMapper.selectList(
                new LambdaQueryWrapper<SysSensitiveApi>()
                        .eq(SysSensitiveApi::getStatus, 1)
        );

        for (SysSensitiveApi sensitiveApi : sensitiveApis) {
            if (matchApiPattern(api, sensitiveApi.getApiPattern())) {
                return sensitiveApi.getApiPattern();
            }
        }
        return "*";
    }

    /**
     * 匹配接口路径（支持通配符）
     * <p>
     * 例如：/api/system/user/* 匹配 /api/system/user/123
     * </p>
     */
    private boolean matchApiPattern(String api, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return false;
        }
        // 去除末尾斜杠
        api = api.replaceAll("/+$", "");
        pattern = pattern.replaceAll("/+$", "");

        if (pattern.endsWith("/*")) {
            // 通配符匹配：/api/system/user/* 匹配 /api/system/user 及以下
            String prefix = pattern.substring(0, pattern.length() - 2);
            return api.equals(prefix) || api.startsWith(prefix + "/");
        }
        return api.equals(pattern);
    }

    /**
     * 异步触发 AI 分析（避免阻塞主业务流程）
     */
    @Async
    public void asyncTriggerAiAnalysis(String username, String ip, String sensitivePattern) {
        try {
            // 防止重复触发（1分钟内同一用户只分析一次）
            String lockKey = "risk:lock:" + username;
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 60, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(locked)) {
                log.debug("[风险检测] 用户 {} 已在分析中，跳过", username);
                return;
            }

            // 收集访问日志（从 Redis 或数据库）
            List<AccessLogDTO> accessLogs = collectAccessLogs(username, sensitivePattern);

            // 调用 AI 分析
            AiAnalyzeResult aiResult = aiRiskAnalyzer.analyzeRisk(
                    username, ip,
                    accessLogs.size(),
                    timeWindow,
                    accessLogs
            );

            // 保存分析结果到数据库
            saveRiskAlert(username, ip, sensitivePattern, accessLogs, aiResult);

            log.info("[风险检测] AI 分析完成 - 用户:{} 风险等级:{} 风险类型:{}",
                    username, aiResult.getRiskLevel(), aiResult.getAlertType());

        } catch (Exception e) {
            log.error("[风险检测] AI 分析异常: {}", e.getMessage(), e);
        } finally {
            // 释放锁（异步操作，无需等待）
        }
    }

    /**
     * 收集访问日志（模拟从数据库/Redis 获取）
     */
    private List<AccessLogDTO> collectAccessLogs(String username, String sensitivePattern) {
        List<AccessLogDTO> logs = new ArrayList<>();
        String redisKeyPattern = REDIS_KEY_PREFIX + username + ":*";

        // 模拟日志数据（实际应从 sys_oper_log 表查询）
        Set<String> keys = redisTemplate.keys(redisKeyPattern);
        if (keys != null) {
            for (int i = 0; i < keys.size(); i++) {
                AccessLogDTO log = new AccessLogDTO();
                log.setUsername(username);
                log.setApi(sensitivePattern);
                log.setMethod("GET");
                log.setAccessTime(LocalDateTime.now().minusSeconds(30));
                logs.add(log);
            }
        }

        // 如果没有日志，创建一个占位
        if (logs.isEmpty()) {
            AccessLogDTO log = new AccessLogDTO();
            log.setUsername(username);
            log.setApi(sensitivePattern);
            log.setMethod("GET");
            log.setAccessTime(LocalDateTime.now());
            logs.add(log);
        }

        return logs;
    }

    /**
     * 保存风险告警到数据库
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRiskAlert(String username, String ip, String sensitivePattern,
                             List<AccessLogDTO> accessLogs, AiAnalyzeResult aiResult) {
        SysRiskAlert alert = new SysRiskAlert();
        alert.setAlertType(aiResult.getAlertType());
        alert.setRiskLevel(aiResult.getRiskLevel());
        alert.setRiskDesc(aiResult.getRiskDesc());
        alert.setHandleSuggest(aiResult.getHandleSuggest());
        alert.setUsername(username);
        alert.setIp(ip);
        alert.setTriggerCount(accessLogs.size());
        alert.setTimeWindow(timeWindow);
        alert.setSensitiveApis(sensitivePattern);
        alert.setStatus(0);  // 未处理

        // 序列化原始日志
        try {
            alert.setRawLogs(objectMapper.writeValueAsString(accessLogs));
        } catch (Exception e) {
            alert.setRawLogs("[]");
        }

        riskAlertMapper.insert(alert);
        log.info("[风险检测] 保存告警记录 - ID:{} 用户:{}", alert.getId(), username);
    }

    @Override
    public List<SysRiskAlert> getUnhandledAlerts() {
        return riskAlertMapper.selectList(
                new LambdaQueryWrapper<SysRiskAlert>()
                        .eq(SysRiskAlert::getStatus, 0)
                        .orderByDesc(SysRiskAlert::getCreateTime)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlert(Long alertId, String handledBy, Integer status) {
        SysRiskAlert alert = new SysRiskAlert();
        alert.setId(alertId);
        alert.setStatus(status);
        alert.setHandledBy(handledBy);
        alert.setHandledTime(LocalDateTime.now());
        riskAlertMapper.updateById(alert);
    }
}
