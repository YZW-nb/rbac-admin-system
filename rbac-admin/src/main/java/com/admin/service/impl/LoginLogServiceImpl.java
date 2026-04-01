package com.admin.service.impl;

import com.admin.common.page.PageParam;
import com.admin.common.page.PageResult;
import com.admin.entity.SysLoginLog;
import com.admin.mapper.SysLoginLogMapper;
import com.admin.service.LoginLogService;
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
 * 登录日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final SysLoginLogMapper loginLogMapper;

    @Override
    public PageResult<SysLoginLog> pageList(String username, String ip, Integer status, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, PageParam.limitPageSize(pageSize));
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), SysLoginLog::getUsername, username);
        wrapper.like(StringUtils.hasText(ip), SysLoginLog::getIp, ip);
        wrapper.eq(status != null, SysLoginLog::getStatus, status);
        wrapper.orderByDesc(SysLoginLog::getLoginTime);
        PageInfo<SysLoginLog> pageInfo = new PageInfo<>(loginLogMapper.selectList(wrapper));
        return PageResult.of(pageInfo.getList(), pageInfo.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLogs(List<Long> ids) {
        loginLogMapper.deleteBatchIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanLogs() {
        loginLogMapper.delete(new LambdaQueryWrapper<>());
    }
}
