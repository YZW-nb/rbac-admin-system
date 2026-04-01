package com.admin.mapper;

import com.admin.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    void insertRoleMenuRelations(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

    void deleteRoleMenuRelations(@Param("roleId") Long roleId);

    /**
     * 统计角色关联的用户数
     */
    long countUsersByRoleId(@Param("roleId") Long roleId);
}
