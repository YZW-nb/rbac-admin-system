package com.admin.vo;

import lombok.Data;

/**
 * 路由元信息 VO
 */
@Data
public class MetaVO {

    private String title;

    private String icon;

    /** 是否缓存 */
    private Boolean keepAlive;

    /** 链接地址（外链） */
    private String link;
}
