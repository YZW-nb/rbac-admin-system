package com.admin.service.impl;

import com.admin.common.constant.CommonConstant;
import com.admin.common.exception.BusinessException;
import com.admin.common.page.PageParam;
import com.admin.common.page.PageResult;
import com.admin.common.result.ResultCode;
import com.admin.dto.RoleDTO;
import com.admin.entity.SysRole;
import com.admin.mapper.SysRoleMapper;
import com.admin.mapper.SysMenuMapper;
import com.admin.service.RoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 角色服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements RoleService {

    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    @Override
    public PageResult<SysRole> pageList(String roleName, String roleCode, Integer status, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, PageParam.limitPageSize(pageSize));
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName);
        wrapper.like(StringUtils.hasText(roleCode), SysRole::getRoleCode, roleCode);
        wrapper.eq(status != null, SysRole::getStatus, status);
        wrapper.orderByAsc(SysRole::getSort);
        PageInfo<SysRole> pageInfo = new PageInfo<>(list(wrapper));
        return PageResult.of(pageInfo.getList(), pageInfo.getTotal());
    }

    @Override
    public List<SysRole> listAll() {
        return list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, CommonConstant.STATUS_NORMAL)
                .orderByAsc(SysRole::getSort));
    }

    @Override
    public SysRole getRoleDetail(Long id) {
        SysRole role = getById(id);
        if (role != null) {
            role.setMenuIds(menuMapper.selectMenuIdsByRoleId(id));
        }
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(RoleDTO roleDTO) {
        // 检查角色编码唯一
        long count = count(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleDTO.getRoleCode()));
        if (count > 0) {
            throw new BusinessException(ResultCode.ROLE_EXISTS);
        }
        SysRole role = new SysRole();
        role.setRoleName(roleDTO.getRoleName());
        role.setRoleCode(roleDTO.getRoleCode());
        role.setSort(roleDTO.getSort());
        role.setStatus(roleDTO.getStatus());
        role.setDataScope(roleDTO.getDataScope());
        role.setRemark(roleDTO.getRemark());
        save(role);

        // 保存角色菜单关联
        if (roleDTO.getMenuIds() != null && !roleDTO.getMenuIds().isEmpty()) {
            saveRoleMenuRelations(role.getId(), roleDTO.getMenuIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleDTO roleDTO) {
        SysRole role = getById(roleDTO.getId());
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }
        // 修改编码时检查唯一
        if (StringUtils.hasText(roleDTO.getRoleCode()) && !roleDTO.getRoleCode().equals(role.getRoleCode())) {
            long count = count(new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getRoleCode, roleDTO.getRoleCode()));
            if (count > 0) {
                throw new BusinessException(ResultCode.ROLE_EXISTS);
            }
        }
        role.setRoleName(roleDTO.getRoleName());
        role.setRoleCode(roleDTO.getRoleCode());
        role.setSort(roleDTO.getSort());
        role.setStatus(roleDTO.getStatus());
        role.setDataScope(roleDTO.getDataScope());
        role.setRemark(roleDTO.getRemark());
        updateById(role);

        // 更新角色菜单关联
        removeRoleMenuRelations(role.getId());
        if (roleDTO.getMenuIds() != null && !roleDTO.getMenuIds().isEmpty()) {
            saveRoleMenuRelations(role.getId(), roleDTO.getMenuIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoles(List<Long> ids) {
        if (ids.contains(1L)) {
            throw new BusinessException(ResultCode.CANNOT_DELETE_ADMIN_ROLE);
        }
        // 检查角色下是否有关联用户
        for (Long roleId : ids) {
            long userCount = roleMapper.countUsersByRoleId(roleId);
            if (userCount > 0) {
                SysRole role = getById(roleId);
                throw new BusinessException("角色 [" + (role != null ? role.getRoleName() : roleId)
                        + "] 已分配给 " + userCount + " 个用户，无法删除");
            }
        }
        removeByIds(ids);
        for (Long roleId : ids) {
            removeRoleMenuRelations(roleId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long roleId, Integer status) {
        SysRole role = new SysRole();
        role.setId(roleId);
        role.setStatus(status);
        updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenu(Long roleId, List<Long> menuIds) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }
        removeRoleMenuRelations(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            saveRoleMenuRelations(roleId, menuIds);
        }
    }

    private void saveRoleMenuRelations(Long roleId, List<Long> menuIds) {
        roleMapper.insertRoleMenuRelations(roleId, menuIds);
    }

    private void removeRoleMenuRelations(Long roleId) {
        roleMapper.deleteRoleMenuRelations(roleId);
    }
}
