package com.admin.service;

import com.admin.common.page.PageResult;
import com.admin.entity.SysLoginLog;

import java.util.List;

/**
 * 登录日志服务
 */
public interface LoginLogService {

    /**
     * 分页查询登录日志列表
     */
    PageResult<SysLoginLog> pageList(String username, String ip, Integer status, int pageNum, int pageSize);

    /**
     * 批量删除日志
     */
    void deleteLogs(List<Long> ids);

    /**
     * 清空日志
     */
    void cleanLogs();
}
