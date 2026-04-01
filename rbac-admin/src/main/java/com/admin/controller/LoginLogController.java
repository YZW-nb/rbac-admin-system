package com.admin.controller;

import com.admin.common.page.PageResult;
import com.admin.common.result.Result;
import com.admin.entity.SysLoginLog;
import com.admin.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录日志控制器
 */
@Tag(name = "登录日志")
@RestController
@RequestMapping("/api/system/loginlog")
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogService loginLogService;

    @Operation(summary = "登录日志列表")
    @PreAuthorize("@ss.hasPerm('sys:loginlog:list')")
    @GetMapping("/list")
    public Result<PageResult<SysLoginLog>> list(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(loginLogService.pageList(username, ip, status, pageNum, pageSize));
    }

    @Operation(summary = "批量删除日志")
    @PreAuthorize("@ss.hasPerm('sys:loginlog:remove')")
    @DeleteMapping("/{ids}")
    public Result<Void> remove(@PathVariable List<Long> ids) {
        loginLogService.deleteLogs(ids);
        return Result.success();
    }

    @Operation(summary = "清空日志")
    @PreAuthorize("@ss.hasPerm('sys:loginlog:remove')")
    @DeleteMapping("/clean")
    public Result<Void> clean() {
        loginLogService.cleanLogs();
        return Result.success();
    }
}
