package com.admin.handler;

import com.admin.common.result.Result;
import com.admin.common.result.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 无权限处理器（@PreAuthorize 校验不通过时触发）
 * 返回 JSON 格式的 403 错误
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        log.warn("无权限访问: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(ResultCode.FORBIDDEN);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
