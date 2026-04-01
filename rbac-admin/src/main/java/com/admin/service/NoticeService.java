package com.admin.service;

import com.admin.common.page.PageResult;
import com.admin.dto.NoticeDTO;
import com.admin.entity.SysNotice;

import java.util.List;

/**
 * 通知公告服务
 */
public interface NoticeService {

    /**
     * 分页查询公告列表
     */
    PageResult<SysNotice> pageList(String title, Integer noticeType, Integer status, int pageNum, int pageSize);

    /**
     * 根据ID获取公告
     */
    SysNotice getNoticeDetail(Long id);

    /**
     * 新增公告
     */
    void addNotice(NoticeDTO noticeDTO);

    /**
     * 修改公告
     */
    void updateNotice(NoticeDTO noticeDTO);

    /**
     * 批量删除公告
     */
    void deleteNotices(List<Long> ids);
}
