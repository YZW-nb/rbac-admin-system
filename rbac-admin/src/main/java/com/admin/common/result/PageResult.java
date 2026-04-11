package com.admin.common.result;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 */
@Data
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    private int code;
    private String message;
    private long total;
    private long pageNum;
    private long pageSize;
    private List<T> rows;

    public PageResult(List<T> rows, long total, long pageNum, long pageSize) {
        this.code = ResultCode.SUCCESS.getCode();
        this.message = ResultCode.SUCCESS.getMessage();
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.rows = rows;
    }

    public static <T> PageResult<T> of(List<T> rows, long total, long pageNum, long pageSize) {
        return new PageResult<>(rows, total, pageNum, pageSize);
    }

    public static <T> PageResult<T> success(List<T> rows, long total) {
        return of(rows, total, 1, rows.size());
    }

    public static <T> PageResult<T> error(String message) {
        PageResult<T> result = new PageResult<>();
        result.setCode(ResultCode.ERROR.getCode());
        result.setMessage(message);
        return result;
    }
}
