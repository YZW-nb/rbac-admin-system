package com.admin.service;

import com.admin.dto.DeptDTO;
import com.admin.entity.SysDept;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 部门服务接口
 */
public interface DeptService extends IService<SysDept> {

    /**
     * 获取部门树形列表
     */
    List<SysDept> getDeptTree(String deptName, Integer status);

    /**
     * 新增部门
     */
    void addDept(DeptDTO deptDTO);

    /**
     * 修改部门
     */
    void updateDept(DeptDTO deptDTO);

    /**
     * 删除部门
     */
    void deleteDept(Long id);
}
