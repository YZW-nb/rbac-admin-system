package com.admin.service;

import com.admin.common.page.PageResult;
import com.admin.entity.SysOperLog;

import java.util.List;

/**
 * 操作日志服务
 */
public interface OperLogService {

    /**
     * 分页查询操作日志列表
     */
    PageResult<SysOperLog> pageList(String title, String operName, Integer status, int pageNum, int pageSize);

    /**
     * 批量删除日志
     */
    void deleteLogs(List<Long> ids);

    /**
     * 清空日志
     */
    void cleanLogs();
}
