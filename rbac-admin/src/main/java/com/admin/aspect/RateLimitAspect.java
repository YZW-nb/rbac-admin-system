package com.admin.aspect;

import com.admin.annotation.RateLimit;
import com.admin.common.exception.BusinessException;
import com.admin.entity.SysUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * 接口限流切面
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;

    public RateLimitAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        // 构建限流 key
        String key = buildKey(point, rateLimit);

        // 获取当前时间窗口内的访问次数
        String countStr = redisTemplate.opsForValue().get(key);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);

        if (count >= rateLimit.count()) {
            log.warn("[限流] 请求过于频繁, key={}, count={}, limit={}", key, count, rateLimit.count());
            throw new BusinessException(rateLimit.message());
        }

        // 原子递增
        Long newCount = redisTemplate.opsForValue().increment(key);

        // 设置过期时间
        if (newCount != null && newCount == 1) {
            redisTemplate.expire(key, rateLimit.time(), TimeUnit.SECONDS);
        }

        log.debug("[限流] key={}, count={}, limit={}", key, newCount, rateLimit.count());

        return point.proceed();
    }

    /**
     * 构建限流 key
     * 格式: rate_limit:ip:method 或 rate_limit:userId:method
     */
    private String buildKey(ProceedingJoinPoint point, RateLimit rateLimit) {
        String prefix = "rate_limit:";

        // 获取请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String ip = "unknown";
        String userId = "unknown";

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ip = getIp(request);

            // 获取当前用户
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof SysUser user) {
                userId = String.valueOf(user.getId());
            }
        }

        // 获取方法名
        MethodSignature signature = (MethodSignature) point.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();

        // 优先使用自定义 key
        if (rateLimit.key() != null && !rateLimit.key().isEmpty()) {
            return prefix + rateLimit.key().replace("#ip", ip).replace("#userId", userId);
        }

        // 默认按 IP 限流
        return prefix + ip + ":" + methodName;
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
