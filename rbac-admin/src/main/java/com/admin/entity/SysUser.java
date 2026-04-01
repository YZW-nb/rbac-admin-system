package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    @JsonIgnore
    private String password;

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private Long deptId;

    private Integer status;

    private String loginIp;

    private LocalDateTime loginTime;

    @TableLogic
    private Integer delFlag;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段：部门名称 */
    @TableField(exist = false)
    private String deptName;

    /** 非数据库字段：角色列表 */
    @TableField(exist = false)
    private java.util.List<SysRole> roles;

    /** 非数据库字段：角色ID列表 */
    @TableField(exist = false)
    private java.util.List<Long> roleIds;
}
