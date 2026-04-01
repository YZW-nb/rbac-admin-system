package com.admin.common.page;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装
 */
@Data
public class PageResult<T> implements Serializable {

    private List<T> list;
    private long total;

    public PageResult() {
    }

    public PageResult(List<T> list, long total) {
        this.list = list;
        this.total = total;
    }

    public static <T> PageResult<T> of(List<T> list, long total) {
        return new PageResult<>(list, total);
    }
}
