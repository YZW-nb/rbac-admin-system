package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体
 */
@Data
@TableName("sys_role")
public class SysRole implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roleName;

    private String roleCode;

    private Integer sort;

    private Integer status;

    private Integer dataScope;

    private String remark;

    @TableLogic
    private Integer delFlag;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段：菜单ID列表 */
    @TableField(exist = false)
    private java.util.List<Long> menuIds;
}
