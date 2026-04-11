package com.admin.service.impl;

import com.admin.entity.SysAiChat;
import com.admin.entity.SysAiMessage;
import com.admin.mapper.SysAiChatMapper;
import com.admin.mapper.SysAiMessageMapper;
import com.admin.service.AiChatService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI 聊天服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final SysAiChatMapper chatMapper;
    private final SysAiMessageMapper messageMapper;

    @Value("${admin.ai.analyze-url:}")
    private String aiUrl;

    @Value("${admin.ai.api-key:}")
    private String aiKey;

    @Value("${admin.ai.model:gpt-3.5-turbo}")
    private String model;

    @Override
    public List<SysAiChat> getChatHistory(Long userId) {
        log.info("[AI历史] 查询用户ID: {}", userId);
        List<SysAiChat> result = chatMapper.selectList(
                new LambdaQueryWrapper<SysAiChat>()
                        .eq(SysAiChat::getUserId, userId)
                        .eq(SysAiChat::getStatus, 1)
                        .orderByDesc(SysAiChat::getCreateTime)
        );
        log.info("[AI历史] 查询结果数量: {}, 数据: {}", result.size(), result);
        return result;
    }

    @Override
    public List<SysAiMessage> getChatMessages(Long chatId) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<SysAiMessage>()
                        .eq(SysAiMessage::getChatId, chatId)
                        .orderByAsc(SysAiMessage::getCreateTime)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createChat(String title, Long userId, String username) {
        SysAiChat chat = new SysAiChat();
        chat.setTitle(title != null ? title : "新对话");
        chat.setUserId(userId);
        chat.setUsername(username);
        chat.setStatus(1);
        chatMapper.insert(chat);
        return chat.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChat(Long chatId) {
        SysAiChat chat = new SysAiChat();
        chat.setId(chatId);
        chat.setStatus(0);
        chatMapper.updateById(chat);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChat(Long chatId, String title) {
        SysAiChat chat = new SysAiChat();
        chat.setId(chatId);
        chat.setTitle(title);
        chatMapper.updateById(chat);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserMessage(Long chatId, String content, Long messageId) {
        SysAiMessage message = new SysAiMessage();
        message.setChatId(chatId);
        message.setRole("user");
        message.setContent(content);
        messageMapper.insert(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAssistantMessage(Long chatId, String content, String sources) {
        SysAiMessage message = new SysAiMessage();
        message.setChatId(chatId);
        message.setRole("assistant");
        message.setContent(content);
        message.setSources(sources);
        messageMapper.insert(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMessageRating(Long messageId, String rating) {
        SysAiMessage message = new SysAiMessage();
        message.setId(messageId);
        message.setRating(rating);
        messageMapper.updateById(message);
    }

    @Override
    public String aiReply(Long chatId, String userMessage, List<SysAiMessage> history) {
        log.info("[AI回复] 收到请求: chatId={}, message={}, aiUrl={}", chatId, userMessage, aiUrl);

        // 如果未配置 AI API，返回默认回复
        if (aiUrl == null || aiUrl.isBlank()) {
            log.info("[AI回复] 未配置AI，使用默认回复");
            return getDefaultReply(userMessage);
        }

        try {
            // 构建消息列表（包含历史上下文）
            cn.hutool.json.JSONObject[] messages = buildMessages(userMessage, history);

            // 调用 AI API（简化版，非流式）
            cn.hutool.json.JSONObject requestBody = new cn.hutool.json.JSONObject()
                    .set("model", model)
                    .set("messages", messages)
                    .set("max_tokens", 2000);

            log.info("[AI回复] 请求体: {}", requestBody);

            cn.hutool.http.HttpResponse response = cn.hutool.http.HttpUtil.createPost(aiUrl)
                    .header("Authorization", "Bearer " + aiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(120000)
                    .execute();

            String body = response.body();
            log.info("[AI回复] 响应状态: {}, 响应体: {}", response.getStatus(), body);

            cn.hutool.json.JSONObject json = cn.hutool.json.JSONUtil.parseObj(body);
            // SiliconFlow 兼容格式
            String content = json.getByPath("choices[0].message.content", String.class);
            if (content == null) {
                content = json.getByPath("choices[0].delta.content", String.class);
            }
            if (content == null) {
                // 尝试提取 error.message
                content = json.getByPath("error.message", String.class);
            }

            if (content != null && !content.isBlank()) {
                log.info("[AI回复] 成功返回: {}", content);
                return content;
            }

            log.warn("[AI回复] AI返回内容为空");
            return getDefaultReply(userMessage);

        } catch (Exception e) {
            log.error("[AI回复] 调用失败: {}", e.getMessage(), e);
            return getDefaultReply(userMessage);
        }
    }

    @Override
    public void aiReplyStream(Long chatId, String userMessage, List<SysAiMessage> history, java.util.function.Consumer<String> callback) {
        log.info("[AI流式回复] 收到请求: chatId={}, message={}", chatId, userMessage);

        // 如果未配置 AI API，返回默认回复
        if (aiUrl == null || aiUrl.isBlank()) {
            String reply = getDefaultReply(userMessage);
            callback.accept("{\"content\": \"" + escapeJson(reply) + "\"}");
            return;
        }

        try {
            // 构建消息列表（包含历史上下文）
            cn.hutool.json.JSONObject[] messages = buildMessages(userMessage, history);

            // 启用流式
            cn.hutool.json.JSONObject requestBody = new cn.hutool.json.JSONObject()
                    .set("model", model)
                    .set("messages", messages)
                    .set("max_tokens", 2000)
                    .set("stream", true);

            cn.hutool.http.HttpRequest request = cn.hutool.http.HttpUtil.createPost(aiUrl)
                    .header("Authorization", "Bearer " + aiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(120000);

            // 禁用自动关闭以支持流读取
            cn.hutool.http.HttpResponse response = request.execute();

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(response.bodyStream(), java.nio.charset.StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6);
                    if ("[DONE]".equals(data)) {
                        break;
                    }
                    try {
                        cn.hutool.json.JSONObject json = cn.hutool.json.JSONUtil.parseObj(data);
                        String content = json.getByPath("choices[0].delta.content", String.class);
                        if (content != null && !content.isBlank()) {
                            // 发送 JSON 格式给前端
                            callback.accept("{\"content\": \"" + escapeJson(content) + "\"}");
                        }
                    } catch (Exception e) {
                        // 忽略解析错误
                    }
                }
            }
            reader.close();

        } catch (Exception e) {
            log.error("[AI流式回复] 调用失败: {}", e.getMessage(), e);
            callback.accept("{\"content\": \"" + escapeJson(getDefaultReply(userMessage)) + "\"}");
        }
    }

    /**
     * 构建消息列表（包含历史上下文）
     */
    private cn.hutool.json.JSONObject[] buildMessages(String userMessage, List<SysAiMessage> history) {
        java.util.List<cn.hutool.json.JSONObject> messagesList = new java.util.ArrayList<>();

        // 系统提示
        messagesList.add(new cn.hutool.json.JSONObject()
                .set("role", "system")
                .set("content", "你是一个专业的权限管理系统助手。请简洁、专业地回答用户问题。如果涉及系统操作，提供清晰的步骤指导。"));

        // 添加历史消息（限制最近 10 条，避免 token 过多）
        if (history != null && !history.isEmpty()) {
            int start = Math.max(0, history.size() - 10);
            for (int i = start; i < history.size(); i++) {
                SysAiMessage msg = history.get(i);
                messagesList.add(new cn.hutool.json.JSONObject()
                        .set("role", msg.getRole())
                        .set("content", msg.getContent()));
            }
        }

        // 添加当前用户消息
        messagesList.add(new cn.hutool.json.JSONObject()
                .set("role", "user")
                .set("content", userMessage));

        return messagesList.toArray(new cn.hutool.json.JSONObject[0]);
    }
    
    /**
     * JSON 字符串转义
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * 默认回复（未配置 AI 时使用）
     */
    private String getDefaultReply(String userMessage) {
        if (userMessage.contains("用户")) {
            return "在权限管理系统中，用户管理功能允许管理员创建、编辑和删除用户账号。你可以在【系统管理】→【用户管理】中进行相关操作。\n\n" +
                    "**常用操作：**\n- 新增用户：填写用户名、密码和基本信息\n- 分配角色：为用户分配相应的角色以获取权限\n- 状态管理：可以启用或禁用用户账号";
        }
        if (userMessage.contains("权限") || userMessage.contains("角色")) {
            return "角色用于对用户进行权限分组管理。建议遵循最小权限原则：\n\n" +
                    "1. **创建角色**：在【系统管理】→【角色管理】中创建\n" +
                    "2. **分配权限**：为角色勾选需要的菜单权限\n" +
                    "3. **分配用户**：将用户关联到对应角色\n\n" +
                    "这样可以简化权限管理，提高安全性。";
        }
        if (userMessage.contains("日志")) {
            return "系统日志可以在【系统监控】→【操作日志】中查看，主要记录：\n\n" +
                    "- 用户登录/登出信息\n" +
                    "- 数据增删改操作\n" +
                    "- 异常错误信息\n\n" +
                    "支持按时间、操作人等条件筛选。";
        }
        return "您好！我是 AI 助手，可以帮你解答权限管理系统相关问题。你可以问我：\n\n" +
                "- 如何创建和管理用户\n" +
                "- 角色和权限的配置方法\n" +
                "- 系统日志的查看\n" +
                "- 常见操作流程\n\n" +
                "请描述你的具体问题。";
    }
}
