package com.admin.service.impl;

import com.admin.common.util.TreeBuildUtil;
import com.admin.common.exception.BusinessException;
import com.admin.common.result.ResultCode;
import com.admin.dto.DeptDTO;
import com.admin.entity.SysDept;
import com.admin.entity.SysUser;
import com.admin.mapper.SysDeptMapper;
import com.admin.mapper.SysUserMapper;
import com.admin.service.DeptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 部门服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements DeptService {

    private final SysUserMapper userMapper;

    @Override
    public List<SysDept> getDeptTree(String deptName, Integer status) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(deptName), SysDept::getDeptName, deptName);
        wrapper.eq(status != null, SysDept::getStatus, status);
        wrapper.orderByAsc(SysDept::getParentId, SysDept::getSort);
        List<SysDept> allDepts = list(wrapper);
        return buildTree(allDepts);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDept(DeptDTO deptDTO) {
        SysDept dept = new SysDept();
        dept.setParentId(deptDTO.getParentId());
        dept.setDeptName(deptDTO.getDeptName());
        dept.setSort(deptDTO.getSort());
        dept.setLeader(deptDTO.getLeader());
        dept.setPhone(deptDTO.getPhone());
        dept.setEmail(deptDTO.getEmail());
        dept.setStatus(deptDTO.getStatus());
        save(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDept(DeptDTO deptDTO) {
        // 不能将父部门设置为自己
        if (deptDTO.getId().equals(deptDTO.getParentId())) {
            throw new BusinessException("父部门不能是自己");
        }
        SysDept dept = new SysDept();
        dept.setId(deptDTO.getId());
        dept.setParentId(deptDTO.getParentId());
        dept.setDeptName(deptDTO.getDeptName());
        dept.setSort(deptDTO.getSort());
        dept.setLeader(deptDTO.getLeader());
        dept.setPhone(deptDTO.getPhone());
        dept.setEmail(deptDTO.getEmail());
        dept.setStatus(deptDTO.getStatus());
        updateById(dept);
    }

    @Override
    @Transactional
    public void deleteDept(Long id) {
        // 检查是否有子部门
        long childCount = count(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(ResultCode.DEPT_HAS_CHILDREN);
        }
        // 检查部门下是否有用户
        long userCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeptId, id));
        if (userCount > 0) {
            throw new BusinessException(ResultCode.DEPT_HAS_USERS);
        }
        removeById(id);
    }

    private List<SysDept> buildTree(List<SysDept> depts) {
        return TreeBuildUtil.build(depts, SysDept::getId, SysDept::getParentId,
                SysDept::setChildren, 0L);
    }
}
