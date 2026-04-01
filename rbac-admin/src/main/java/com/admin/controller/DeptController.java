package com.admin.controller;

import com.admin.annotation.Log;
import com.admin.common.result.Result;
import com.admin.dto.DeptDTO;
import com.admin.entity.SysDept;
import com.admin.service.DeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.admin.annotation.BusinessType.*;

/**
 * 部门管理控制器
 */
@Tag(name = "部门管理")
@RestController
@RequestMapping("/api/system/dept")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    @Operation(summary = "部门列表（树形）")
    @PreAuthorize("@ss.hasPerm('sys:dept:list')")
    @GetMapping("/list")
    public Result<List<SysDept>> list(
            @RequestParam(required = false) String deptName,
            @RequestParam(required = false) Integer status) {
        return Result.success(deptService.getDeptTree(deptName, status));
    }

    @Operation(summary = "部门详情")
    @PreAuthorize("@ss.hasPerm('sys:dept:query')")
    @GetMapping("/{id}")
    public Result<SysDept> getInfo(@PathVariable Long id) {
        return Result.success(deptService.getById(id));
    }

    @Operation(summary = "新增部门")
    @Log(title = "部门管理", businessType = INSERT)
    @PreAuthorize("@ss.hasPerm('sys:dept:add')")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody DeptDTO deptDTO) {
        deptService.addDept(deptDTO);
        return Result.success();
    }

    @Operation(summary = "修改部门")
    @Log(title = "部门管理", businessType = UPDATE)
    @PreAuthorize("@ss.hasPerm('sys:dept:edit')")
    @PutMapping
    public Result<Void> edit(@Valid @RequestBody DeptDTO deptDTO) {
        deptService.updateDept(deptDTO);
        return Result.success();
    }

    @Operation(summary = "删除部门")
    @Log(title = "部门管理", businessType = DELETE)
    @PreAuthorize("@ss.hasPerm('sys:dept:remove')")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        deptService.deleteDept(id);
        return Result.success();
    }
}
