package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知公告实体
 */
@Data
@TableName("sys_notice")
public class SysNotice implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    /** 类型：1通知 2公告 */
    private Integer noticeType;

    /** 状态：1正常 0关闭 */
    private Integer status;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
