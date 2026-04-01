package com.admin.controller;

import com.admin.common.page.PageResult;
import com.admin.common.result.Result;
import com.admin.entity.SysOperLog;
import com.admin.service.OperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志控制器
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/api/system/operlog")
@RequiredArgsConstructor
public class OperLogController {

    private final OperLogService operLogService;

    @Operation(summary = "操作日志列表")
    @PreAuthorize("@ss.hasPerm('sys:operlog:list')")
    @GetMapping("/list")
    public Result<PageResult<SysOperLog>> list(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String operName,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(operLogService.pageList(title, operName, status, pageNum, pageSize));
    }

    @Operation(summary = "批量删除日志")
    @PreAuthorize("@ss.hasPerm('sys:operlog:remove')")
    @DeleteMapping("/{ids}")
    public Result<Void> remove(@PathVariable List<Long> ids) {
        operLogService.deleteLogs(ids);
        return Result.success();
    }

    @Operation(summary = "清空日志")
    @PreAuthorize("@ss.hasPerm('sys:operlog:remove')")
    @DeleteMapping("/clean")
    public Result<Void> clean() {
        operLogService.cleanLogs();
        return Result.success();
    }
}
