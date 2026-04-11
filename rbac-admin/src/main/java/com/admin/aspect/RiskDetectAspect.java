package com.admin.aspect;

import com.admin.entity.SysUser;
import com.admin.service.RiskDetectService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 风险检测 AOP 切面
 * <p>
 * 在 Controller 方法执行后，检测异常访问行为
 * </p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RiskDetectAspect {

    private final RiskDetectService riskDetectService;

    /**
     * 定义切点：所有 Controller 的 public 方法
     */
    @Pointcut("execution(public * com.admin.controller..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 方法正常返回后触发风险检测
     */
    @AfterReturning(pointcut = "controllerPointcut()", returning = "result")
    public void afterReturning(Object result) {
        try {
            // 获取当前请求
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            // 排除登录、静态资源等非敏感接口
            String uri = request.getRequestURI();
            if (isExcludedUri(uri)) {
                return;
            }

            // 获取当前用户
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getPrincipal().equals("anonymousUser")) {
                return;
            }

            String username = null;
            if (auth.getPrincipal() instanceof SysUser user) {
                username = user.getUsername();
            }

            if (username == null) {
                return;
            }

            // 获取客户端 IP
            String ip = getClientIp(request);

            // 触发风险检测
            riskDetectService.detectAbnormalAccess(
                    username,
                    ip,
                    uri,
                    request.getMethod()
            );

        } catch (Exception e) {
            // 风险检测不应影响正常业务
            log.warn("[风险检测] 检测异常: {}", e.getMessage());
        }
    }

    /**
     * 排除不需要检测的 URI
     */
    private boolean isExcludedUri(String uri) {
        return uri.startsWith("/auth/")
                || uri.startsWith("/login")
                || uri.startsWith("/swagger")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/doc.html")
                || uri.startsWith("/webjars")
                || uri.startsWith("/druid")
                || uri.endsWith(".js")
                || uri.endsWith(".css")
                || uri.endsWith(".png")
                || uri.endsWith(".jpg")
                || uri.endsWith(".ico")
                || uri.equals("/")
                || uri.equals("/api/dashboard/stats");
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
