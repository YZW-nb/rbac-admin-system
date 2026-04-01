package com.admin.controller;

import com.admin.annotation.Log;
import com.admin.common.result.Result;
import com.admin.dto.MenuDTO;
import com.admin.entity.SysMenu;
import com.admin.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.admin.annotation.BusinessType.*;

/**
 * 菜单管理控制器
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/system/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "菜单列表（树形）")
    @PreAuthorize("@ss.hasPerm('sys:menu:list')")
    @GetMapping("/list")
    public Result<List<SysMenu>> list(
            @RequestParam(required = false) String menuName,
            @RequestParam(required = false) Integer status) {
        return Result.success(menuService.getMenuTree(menuName, status));
    }

    @Operation(summary = "菜单下拉树")
    @GetMapping("/treeselect")
    public Result<List<SysMenu>> treeselect() {
        return Result.success(menuService.getMenuSelectTree());
    }

    @Operation(summary = "菜单详情")
    @PreAuthorize("@ss.hasPerm('sys:menu:query')")
    @GetMapping("/{id}")
    public Result<SysMenu> getInfo(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @Operation(summary = "新增菜单")
    @Log(title = "菜单管理", businessType = INSERT)
    @PreAuthorize("@ss.hasPerm('sys:menu:add')")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody MenuDTO menuDTO) {
        menuService.addMenu(menuDTO);
        return Result.success();
    }

    @Operation(summary = "修改菜单")
    @Log(title = "菜单管理", businessType = UPDATE)
    @PreAuthorize("@ss.hasPerm('sys:menu:edit')")
    @PutMapping
    public Result<Void> edit(@Valid @RequestBody MenuDTO menuDTO) {
        menuService.updateMenu(menuDTO);
        return Result.success();
    }

    @Operation(summary = "删除菜单")
    @Log(title = "菜单管理", businessType = DELETE)
    @PreAuthorize("@ss.hasPerm('sys:menu:remove')")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success();
    }
}
