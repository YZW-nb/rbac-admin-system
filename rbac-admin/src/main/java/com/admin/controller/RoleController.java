package com.admin.controller;

import com.admin.annotation.Log;
import com.admin.common.page.PageResult;
import com.admin.common.result.Result;
import com.admin.dto.AssignMenuDTO;
import com.admin.dto.ChangeStatusDTO;
import com.admin.dto.RoleDTO;
import com.admin.entity.SysRole;
import com.admin.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.admin.annotation.BusinessType.*;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "角色列表")
    @PreAuthorize("@ss.hasPerm('sys:role:list')")
    @GetMapping("/list")
    public Result<PageResult<SysRole>> list(
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(roleService.pageList(roleName, roleCode, status, pageNum, pageSize));
    }

    @Operation(summary = "所有角色（下拉选择）")
    @GetMapping("/option")
    public Result<List<SysRole>> option() {
        return Result.success(roleService.listAll());
    }

    @Operation(summary = "角色详情")
    @PreAuthorize("@ss.hasPerm('sys:role:query')")
    @GetMapping("/{id}")
    public Result<SysRole> getInfo(@PathVariable Long id) {
        return Result.success(roleService.getRoleDetail(id));
    }

    @Operation(summary = "新增角色")
    @Log(title = "角色管理", businessType = INSERT)
    @PreAuthorize("@ss.hasPerm('sys:role:add')")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody RoleDTO roleDTO) {
        roleService.addRole(roleDTO);
        return Result.success();
    }

    @Operation(summary = "修改角色")
    @Log(title = "角色管理", businessType = UPDATE)
    @PreAuthorize("@ss.hasPerm('sys:role:edit')")
    @PutMapping
    public Result<Void> edit(@Valid @RequestBody RoleDTO roleDTO) {
        roleService.updateRole(roleDTO);
        return Result.success();
    }

    @Operation(summary = "批量删除角色")
    @Log(title = "角色管理", businessType = DELETE)
    @PreAuthorize("@ss.hasPerm('sys:role:remove')")
    @DeleteMapping("/{ids}")
    public Result<Void> remove(@PathVariable List<Long> ids) {
        roleService.deleteRoles(ids);
        return Result.success();
    }

    @Operation(summary = "修改状态")
    @Log(title = "角色管理", businessType = UPDATE)
    @PreAuthorize("@ss.hasPerm('sys:role:edit')")
    @PutMapping("/changeStatus")
    public Result<Void> changeStatus(@Valid @RequestBody ChangeStatusDTO dto) {
        roleService.changeStatus(dto.getRoleId(), dto.getStatus());
        return Result.success();
    }

    @Operation(summary = "分配菜单权限")
    @Log(title = "角色管理", businessType = UPDATE)
    @PreAuthorize("@ss.hasPerm('sys:role:edit')")
    @PutMapping("/assignMenu")
    public Result<Void> assignMenu(@Valid @RequestBody AssignMenuDTO dto) {
        roleService.assignMenu(dto.getRoleId(), dto.getMenuIds());
        return Result.success();
    }
}
