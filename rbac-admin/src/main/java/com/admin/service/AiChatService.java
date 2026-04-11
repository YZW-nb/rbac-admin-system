package com.admin.service;

import com.admin.entity.SysAiChat;
import com.admin.entity.SysAiMessage;

import java.util.List;

/**
 * AI 聊天服务接口
 */
public interface AiChatService {

    /**
     * 获取对话历史列表
     */
    List<SysAiChat> getChatHistory(Long userId);

    /**
     * 获取对话消息
     */
    List<SysAiMessage> getChatMessages(Long chatId);

    /**
     * 创建新对话
     */
    Long createChat(String title, Long userId, String username);

    /**
     * 删除对话
     */
    void deleteChat(Long chatId);

    /**
     * 更新对话标题
     */
    void updateChat(Long chatId, String title);

    /**
     * 保存用户消息
     */
    void saveUserMessage(Long chatId, String content, Long messageId);

    /**
     * 保存 AI 消息
     */
    void saveAssistantMessage(Long chatId, String content, String sources);

    /**
     * 更新消息评价
     */
    void updateMessageRating(Long messageId, String rating);

    /**
     * AI 回复（支持多轮对话）
     * @param chatId 对话ID
     * @param userMessage 用户消息
     * @param history 历史消息列表
     */
    String aiReply(Long chatId, String userMessage, List<SysAiMessage> history);

    /**
     * AI 回复（流式，支持多轮对话）
     * @param chatId 对话ID
     * @param userMessage 用户消息
     * @param history 历史消息列表
     * @param callback 每个 chunk 的回调
     */
    void aiReplyStream(Long chatId, String userMessage, List<SysAiMessage> history, java.util.function.Consumer<String> callback);
}
