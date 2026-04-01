package com.admin.service.impl;

import com.admin.common.page.PageParam;
import com.admin.common.page.PageResult;
import com.admin.entity.SysOperLog;
import com.admin.mapper.SysOperLogMapper;
import com.admin.service.OperLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 操作日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperLogServiceImpl implements OperLogService {

    private final SysOperLogMapper operLogMapper;

    @Override
    public PageResult<SysOperLog> pageList(String title, String operName, Integer status, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, PageParam.limitPageSize(pageSize));
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(title), SysOperLog::getTitle, title);
        wrapper.like(StringUtils.hasText(operName), SysOperLog::getOperName, operName);
        wrapper.eq(status != null, SysOperLog::getStatus, status);
        wrapper.orderByDesc(SysOperLog::getOperTime);
        PageInfo<SysOperLog> pageInfo = new PageInfo<>(operLogMapper.selectList(wrapper));
        return PageResult.of(pageInfo.getList(), pageInfo.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLogs(List<Long> ids) {
        operLogMapper.deleteBatchIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanLogs() {
        operLogMapper.delete(new LambdaQueryWrapper<>());
    }
}
