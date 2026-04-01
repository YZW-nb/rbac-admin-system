package com.admin.controller;

import com.admin.common.constant.CommonConstant;
import com.admin.common.result.Result;
import com.admin.service.MenuService;
import com.admin.service.RoleService;
import com.admin.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页统计控制器
 */
@Tag(name = "首页统计")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final RoleService roleService;
    private final MenuService menuService;

    @Operation(summary = "获取首页统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userService.count());
        data.put("roleCount", roleService.count());
        data.put("menuCount", menuService.count());
        data.put("onlineCount", 1);
        return Result.success(data);
    }
}
