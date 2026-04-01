package com.admin.service;

import com.admin.common.page.PageResult;
import com.admin.dto.UserDTO;
import com.admin.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<SysUser> {

    /**
     * 分页查询用户列表
     */
    PageResult<SysUser> pageList(String username, String phone, Integer status, Long deptId, int pageNum, int pageSize);

    /**
     * 获取用户详情（含角色、部门）
     */
    SysUser getUserDetail(Long id);

    /**
     * 新增用户
     */
    void addUser(UserDTO userDTO);

    /**
     * 修改用户
     */
    void updateUser(UserDTO userDTO);

    /**
     * 批量删除用户
     */
    void deleteUsers(List<Long> ids);

    /**
     * 修改用户状态
     */
    void changeStatus(Long userId, Integer status);

    /**
     * 重置密码，返回生成的随机密码
     */
    String resetPassword(Long userId);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 根据用户名查询用户
     */
    SysUser getByUsername(String username);

    /**
     * 获取用户权限标识列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取用户角色编码列表
     */
    List<String> getUserRoleCodes(Long userId);
}
