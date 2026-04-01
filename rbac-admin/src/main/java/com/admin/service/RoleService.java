package com.admin.service;

import com.admin.common.page.PageResult;
import com.admin.dto.RoleDTO;
import com.admin.entity.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<SysRole> {

    /**
     * 分页查询角色列表
     */
    PageResult<SysRole> pageList(String roleName, String roleCode, Integer status, int pageNum, int pageSize);

    /**
     * 查询所有可用角色（下拉选择用）
     */
    List<SysRole> listAll();

    /**
     * 获取角色详情（含菜单ID列表）
     */
    SysRole getRoleDetail(Long id);

    /**
     * 新增角色
     */
    void addRole(RoleDTO roleDTO);

    /**
     * 修改角色
     */
    void updateRole(RoleDTO roleDTO);

    /**
     * 批量删除角色
     */
    void deleteRoles(List<Long> ids);

    /**
     * 修改状态
     */
    void changeStatus(Long roleId, Integer status);

    /**
     * 分配菜单权限
     */
    void assignMenu(Long roleId, List<Long> menuIds);
}
