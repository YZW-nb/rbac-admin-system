package com.admin.controller;

import com.admin.annotation.Log;
import com.admin.common.page.PageResult;
import com.admin.common.result.Result;
import com.admin.dto.ChangePasswordDTO;
import com.admin.dto.UserChangeStatusDTO;
import com.admin.dto.UserDTO;
import com.admin.entity.SysUser;
import com.admin.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.admin.annotation.BusinessType.*;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户列表")
    @PreAuthorize("@ss.hasPerm('sys:user:list')")
    @GetMapping("/list")
    public Result<PageResult<SysUser>> list(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long deptId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<SysUser> result = userService.pageList(username, phone, status, deptId, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "用户详情")
    @PreAuthorize("@ss.hasPerm('sys:user:query')")
    @GetMapping("/{id}")
    public Result<SysUser> getInfo(@PathVariable Long id) {
        return Result.success(userService.getUserDetail(id));
    }

    @Operation(summary = "新增用户")
    @Log(title = "用户管理", businessType = INSERT)
    @PreAuthorize("@ss.hasPerm('sys:user:add')")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody UserDTO userDTO) {
        userService.addUser(userDTO);
        return Result.success();
    }

    @Operation(summary = "修改用户")
    @Log(title = "用户管理", businessType = UPDATE)
    @PreAuthorize("@ss.hasPerm('sys:user:edit')")
    @PutMapping
    public Result<Void> edit(@Valid @RequestBody UserDTO userDTO) {
        userService.updateUser(userDTO);
        return Result.success();
    }

    @Operation(summary = "批量删除用户")
    @Log(title = "用户管理", businessType = DELETE)
    @PreAuthorize("@ss.hasPerm('sys:user:remove')")
    @DeleteMapping("/{ids}")
    public Result<Void> remove(@PathVariable List<Long> ids) {
        userService.deleteUsers(ids);
        return Result.success();
    }

    @Operation(summary = "修改状态")
    @Log(title = "用户管理", businessType = UPDATE)
    @PreAuthorize("@ss.hasPerm('sys:user:edit')")
    @PutMapping("/changeStatus")
    public Result<Void> changeStatus(@Valid @RequestBody UserChangeStatusDTO dto) {
        userService.changeStatus(dto.getUserId(), dto.getStatus());
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @Log(title = "用户管理", businessType = UPDATE)
    @PreAuthorize("@ss.hasPerm('sys:user:resetPwd')")
    @PutMapping("/resetPwd/{userId}")
    public Result<String> resetPassword(@PathVariable Long userId) {
        String newPassword = userService.resetPassword(userId);
        return Result.success(newPassword);
    }

    @Operation(summary = "修改密码")
    @Log(title = "个人中心", businessType = UPDATE)
    @PutMapping("/changePassword")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        // 从 SecurityContext 获取当前登录用户 ID，防止越权修改他人密码
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SysUser user)) {
            return Result.error("未获取到当前登录用户信息");
        }
        userService.changePassword(user.getId(), dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }
}
