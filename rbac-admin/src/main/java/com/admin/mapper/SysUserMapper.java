package com.admin.mapper;

import com.admin.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户ID查询角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询权限标识列表
     */
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    /**
     * 批量插入用户角色关联
     */
    void insertUserRoleRelations(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    /**
     * 删除用户角色关联
     */
    void deleteUserRoleRelations(@Param("userId") Long userId);
}
