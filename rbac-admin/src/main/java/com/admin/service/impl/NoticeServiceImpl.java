package com.admin.service.impl;

import com.admin.common.page.PageParam;
import com.admin.common.page.PageResult;
import com.admin.dto.NoticeDTO;
import com.admin.entity.SysNotice;
import com.admin.mapper.SysNoticeMapper;
import com.admin.service.NoticeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 通知公告服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final SysNoticeMapper noticeMapper;

    @Override
    public PageResult<SysNotice> pageList(String title, Integer noticeType, Integer status, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, PageParam.limitPageSize(pageSize));
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(title), SysNotice::getTitle, title);
        wrapper.eq(noticeType != null, SysNotice::getNoticeType, noticeType);
        wrapper.eq(status != null, SysNotice::getStatus, status);
        wrapper.orderByDesc(SysNotice::getCreateTime);
        PageInfo<SysNotice> pageInfo = new PageInfo<>(noticeMapper.selectList(wrapper));
        return PageResult.of(pageInfo.getList(), pageInfo.getTotal());
    }

    @Override
    public SysNotice getNoticeDetail(Long id) {
        return noticeMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addNotice(NoticeDTO noticeDTO) {
        SysNotice notice = new SysNotice();
        notice.setTitle(noticeDTO.getTitle());
        notice.setContent(noticeDTO.getContent());
        notice.setNoticeType(noticeDTO.getNoticeType());
        notice.setStatus(noticeDTO.getStatus());
        notice.setCreateBy(getCurrentUsername());
        noticeMapper.insert(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNotice(NoticeDTO noticeDTO) {
        SysNotice notice = new SysNotice();
        notice.setId(noticeDTO.getId());
        notice.setTitle(noticeDTO.getTitle());
        notice.setContent(noticeDTO.getContent());
        notice.setNoticeType(noticeDTO.getNoticeType());
        notice.setStatus(noticeDTO.getStatus());
        notice.setUpdateBy(getCurrentUsername());
        noticeMapper.updateById(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotices(List<Long> ids) {
        noticeMapper.deleteBatchIds(ids);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.admin.entity.SysUser user) {
            return user.getUsername();
        }
        return null;
    }
}
