package com.admin.controller;

import com.admin.common.result.Result;
import com.admin.entity.SysRiskAlert;
import com.admin.entity.SysUser;
import com.admin.service.RiskDetectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 风险告警控制器
 */
@Tag(name = "风险告警")
@RestController
@RequestMapping("/api/risk/alert")
@RequiredArgsConstructor
public class RiskAlertController {

    private final RiskDetectService riskDetectService;

    @Operation(summary = "获取未处理告警列表")
    @GetMapping("/unhandled")
    public Result<List<SysRiskAlert>> getUnhandledAlerts() {
        return Result.success(riskDetectService.getUnhandledAlerts());
    }

    @Operation(summary = "处理告警")
    @PutMapping("/handle/{alertId}")
    public Result<Void> handleAlert(
            @PathVariable Long alertId,
            @RequestParam Integer status) {

        // 获取当前处理人
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String handledBy = "system";
        if (auth != null && auth.getPrincipal() instanceof SysUser) {
            SysUser user = (SysUser) auth.getPrincipal();
            handledBy = user.getUsername();
        }

        riskDetectService.handleAlert(alertId, handledBy, status);
        return Result.success();
    }
}
