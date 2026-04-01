package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单实体
 */
@Data
@TableName("sys_menu")
public class SysMenu implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private String menuName;

    /** 类型：1目录 2菜单 3按钮 */
    private Integer menuType;

    private String path;

    private String component;

    private String perms;

    private String icon;

    private Integer sort;

    /** 是否可见：1显示 0隐藏 */
    private Integer visible;

    private Integer status;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段：子菜单列表 */
    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();
}
