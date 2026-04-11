package com.admin.controller;

import com.admin.annotation.RateLimit;
import com.admin.common.controller.BaseController;
import com.admin.common.result.Result;
import com.admin.dto.ChatDTO;
import com.admin.dto.MessageDTO;
import com.admin.entity.SysAiChat;
import com.admin.entity.SysAiMessage;
import com.admin.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 聊天控制器
 */
@Slf4j
@Tag(name = "AI 聊天")
@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
public class AiChatController extends BaseController {

    private final AiChatService aiChatService;

    /**
     * 获取对话历史
     */
    @Operation(summary = "获取对话历史")
    @GetMapping("/history")
    public Result<List<SysAiChat>> getHistory() {
        Long userId = getCurrentUserId();
        return Result.success(aiChatService.getChatHistory(userId));
    }

    /**
     * 获取消息列表
     */
    @Operation(summary = "获取消息列表")
    @GetMapping("/{chatId}/messages")
    public Result<List<SysAiMessage>> getMessages(@PathVariable Long chatId) {
        return Result.success(aiChatService.getChatMessages(chatId));
    }

    /**
     * 创建新对话
     */
    @Operation(summary = "创建对话")
    @PostMapping
    public Result<Long> createChat(@Valid @RequestBody ChatDTO dto) {
        Long userId = getCurrentUserId();
        String username = getCurrentUsername();
        Long chatId = aiChatService.createChat(dto.getTitle(), userId, username);
        return Result.success(chatId);
    }

    /**
     * 删除对话
     */
    @Operation(summary = "删除对话")
    @DeleteMapping("/{chatId}")
    public Result<Void> deleteChat(@PathVariable Long chatId) {
        aiChatService.deleteChat(chatId);
        return Result.success();
    }

    /**
     * 更新对话标题
     */
    @Operation(summary = "更新对话标题")
    @PutMapping("/{chatId}")
    public Result<Void> updateChat(@PathVariable Long chatId, @Valid @RequestBody ChatDTO dto) {
        aiChatService.updateChat(chatId, dto.getTitle());
        return Result.success();
    }

    /**
     * 发送消息（非流式）
     */
    @Operation(summary = "发送消息")
    @RateLimit(time = 60, count = 20, message = "AI 调用过于频繁，请稍后再试")
    @PostMapping("/send")
    public Result<String> sendMessage(@Valid @RequestBody MessageDTO dto) {
        // 获取历史消息（用于多轮对话）
        List<SysAiMessage> history = aiChatService.getChatMessages(dto.getChatId());

        // 保存用户消息
        aiChatService.saveUserMessage(dto.getChatId(), dto.getContent(), dto.getMessageId());

        // AI 回复（带历史上下文）
        String reply = aiChatService.aiReply(dto.getChatId(), dto.getContent(), history);

        // 保存 AI 消息
        aiChatService.saveAssistantMessage(dto.getChatId(), reply, null);

        return Result.success(reply);
    }

    /**
     * 发送消息（流式）- 使用 SSE
     */
    @Operation(summary = "流式发送消息")
    @RateLimit(time = 60, count = 20, message = "AI 调用过于频繁，请稍后再试")
    @PostMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public void streamMessage(@Valid @RequestBody MessageDTO dto, HttpServletResponse response) throws Exception {
        // 设置 SSE 响应头
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");

        ServletOutputStream out = response.getOutputStream();

        // 用于收集完整 AI 回复
        StringBuilder fullReply = new StringBuilder();
        // 用于收集纯文本内容（保存到数据库）
        StringBuilder plainText = new StringBuilder();

        try {
            // 获取历史消息（用于多轮对话）
            List<SysAiMessage> history = aiChatService.getChatMessages(dto.getChatId());

            // 保存用户消息
            aiChatService.saveUserMessage(dto.getChatId(), dto.getContent(), dto.getMessageId());

            // 获取 AI 回复（流式，带历史上下文）
            aiChatService.aiReplyStream(dto.getChatId(), dto.getContent(), history, (chunk) -> {
                try {
                    fullReply.append(chunk);
                    // 从 JSON 中提取纯文本内容
                    String text = extractContentFromJson(chunk);
                    if (text != null) {
                        plainText.append(text);
                    }
                    out.write(("data: " + chunk + "\n\n").getBytes("UTF-8"));
                    out.flush();
                } catch (Exception e) {
                    log.error("[SSE] 写入失败: {}", e.getMessage());
                }
            });

            // 保存完整 AI 消息到数据库（使用纯文本）
            String finalReply = plainText.toString();
            if (!finalReply.isEmpty()) {
                aiChatService.saveAssistantMessage(dto.getChatId(), finalReply, null);
                log.info("[SSE] AI回复已保存到数据库, chatId={}, 长度={}", dto.getChatId(), finalReply.length());
            }

            out.write("data: [DONE]\n\n".getBytes("UTF-8"));
            out.flush();

        } catch (Exception e) {
            log.error("[SSE] 流式响应异常: {}", e.getMessage());
            try {
                out.write(("data: {\"error\": \"" + e.getMessage() + "\"}\n\n").getBytes("UTF-8"));
                out.flush();
            } catch (Exception ex) {
                log.error("[SSE] 写入错误响应失败: {}", ex.getMessage());
            }
        }
    }

    /**
     * 更新评价
     */
    @Operation(summary = "评价消息")
    @PutMapping("/message/{messageId}/rating")
    public Result<Void> updateRating(
            @PathVariable Long messageId,
            @RequestParam String rating) {
        aiChatService.updateMessageRating(messageId, rating);
        return Result.success();
    }

    /**
     * 从 JSON 片段中提取 content 内容
     */
    private String extractContentFromJson(String chunk) {
        if (chunk == null || chunk.isBlank()) {
            return null;
        }
        try {
            // 尝试解析 {"content": "xxx"} 格式
            if (chunk.startsWith("{")) {
                cn.hutool.json.JSONObject json = cn.hutool.json.JSONUtil.parseObj(chunk);
                return json.getStr("content");
            }
        } catch (Exception e) {
            // 解析失败，忽略
        }
        return null;
    }
}
