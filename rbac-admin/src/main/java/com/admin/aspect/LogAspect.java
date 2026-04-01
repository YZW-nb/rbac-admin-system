package com.admin.aspect;

import com.admin.annotation.Log;
import com.admin.annotation.BusinessType;
import com.admin.common.constant.CommonConstant;
import com.admin.entity.SysOperLog;
import com.admin.entity.SysUser;
import com.admin.mapper.SysOperLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    /** 日志参数/错误信息最大长度 */
    private static final int MAX_LOG_LENGTH = 2000;

    private final SysOperLogMapper operLogMapper;
    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(com.admin.annotation.Log)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();

        SysOperLog operLog = new SysOperLog();
        operLog.setOperTime(LocalDateTime.now());

        try {
            // 获取操作信息
            Log controllerLog = getAnnotation(point);
            if (controllerLog != null) {
                operLog.setTitle(controllerLog.title());
                operLog.setBusinessType(getBusinessType(controllerLog.businessType()));
            }

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operLog.setOperUrl(request.getRequestURI());
                operLog.setRequestMethod(request.getMethod());
                operLog.setOperIp(getIp(request));
            }

            // 获取当前用户
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof SysUser user) {
                operLog.setOperName(user.getUsername());
            }

            // 获取方法名
            String className = point.getTarget().getClass().getName();
            String methodName = point.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");

            // 请求参数
            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                try {
                    String param = objectMapper.writeValueAsString(args[0]);
                    operLog.setOperParam(param.length() > 2000 ? param.substring(0, 2000) : param);
                } catch (Exception ignored) {
                }
            }

            // 执行方法
            Object result = point.proceed();
            operLog.setStatus(CommonConstant.STATUS_SUCCESS);
            return result;

        } catch (Exception e) {
            operLog.setStatus(CommonConstant.STATUS_FAIL);
            operLog.setErrorMsg(e.getMessage().length() > MAX_LOG_LENGTH ? e.getMessage().substring(0, MAX_LOG_LENGTH) : e.getMessage());
            throw e;
        } finally {
            // 记录耗时
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[操作日志] {} - {}ms", operLog.getTitle(), elapsed);

            // 异步保存日志（简单起见直接保存，生产环境应用 @Async）
            try {
                operLogMapper.insert(operLog);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }
    }

    private Log getAnnotation(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        return signature.getMethod().getAnnotation(Log.class);
    }

    private Integer getBusinessType(BusinessType type) {
        return switch (type) {
            case QUERY -> 1;
            case INSERT -> 2;
            case UPDATE -> 3;
            case DELETE -> 4;
            case EXPORT -> 5;
            case IMPORT -> 6;
            default -> 0;
        };
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}
