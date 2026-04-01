package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门实体
 */
@Data
@TableName("sys_dept")
public class SysDept implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private String deptName;

    private Integer sort;

    private String leader;

    private String phone;

    private String email;

    private Integer status;

    @TableLogic
    private Integer delFlag;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段：子部门列表 */
    @TableField(exist = false)
    private List<SysDept> children = new ArrayList<>();
}
