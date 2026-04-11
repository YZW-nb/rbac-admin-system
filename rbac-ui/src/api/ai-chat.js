import request from '@/utils/request'

/**
 * 获取对话历史列表
 */
export function getChatHistory() {
  return request.get('/ai/chat/history')
}

/**
 * 获取单个对话的消息列表
 */
export function getChatMessages(chatId) {
  return request.get(`/ai/chat/${chatId}/messages`)
}

/**
 * 创建新对话
 */
export function createChat(data) {
  return request.post('/ai/chat', data)
}

/**
 * 删除对话
 */
export function deleteChat(chatId) {
  return request.delete(`/ai/chat/${chatId}`)
}

/**
 * 更新对话标题
 */
export function updateChat(chatId, data) {
  return request.put(`/ai/chat/${chatId}`, data)
}

/**
 * 发送消息（普通模式）
 */
export function sendMessage(data) {
  return request.post('/ai/chat/send', data)
}

/**
 * 发送消息（流式模式）- 返回 EventSource 或 fetch 流
 */
export function sendMessageStream(data) {
  const token = localStorage.getItem('token')
  return fetch('/api/ai/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(data)
  })
}

/**
 * 提交用户评价
 */
export function submitRating(messageId, rating) {
  return request.put(`/ai/chat/message/${messageId}/rating`, { rating })
}
