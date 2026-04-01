package com.admin.vo;

import lombok.Data;

import java.util.List;

/**
 * 前端路由 VO
 */
@Data
public class RouterVO {

    private String name;

    private String path;

    private String component;

    private Boolean hidden;

    private String redirect;

    private MetaVO meta;

    private List<RouterVO> children;
}
