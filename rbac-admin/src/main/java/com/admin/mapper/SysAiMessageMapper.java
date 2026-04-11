package com.admin.mapper;

import com.admin.entity.SysAiMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 聊天消息 Mapper
 */
@Mapper
public interface SysAiMessageMapper extends BaseMapper<SysAiMessage> {
}
