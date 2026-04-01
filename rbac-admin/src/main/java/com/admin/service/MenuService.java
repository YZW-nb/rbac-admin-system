package com.admin.service;

import com.admin.dto.MenuDTO;
import com.admin.entity.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService extends IService<SysMenu> {

    /**
     * 获取菜单列表（树形）
     */
    List<SysMenu> getMenuTree(String menuName, Integer status);

    /**
     * 获取菜单下拉树
     */
    List<SysMenu> getMenuSelectTree();

    /**
     * 新增菜单
     */
    void addMenu(MenuDTO menuDTO);

    /**
     * 修改菜单
     */
    void updateMenu(MenuDTO menuDTO);

    /**
     * 删除菜单
     */
    void deleteMenu(Long id);

    /**
     * 根据用户ID获取菜单路由列表
     */
    List<SysMenu> getMenusByUserId(Long userId);
}
