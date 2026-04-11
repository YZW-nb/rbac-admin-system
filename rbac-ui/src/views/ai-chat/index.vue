<template>
  <div class="ai-chat-page">
    <!-- 侧边栏 -->
    <aside v-if="showSidebar" class="chat-sidebar">
      <div class="sidebar-header">
        <span>对话历史</span>
        <el-button type="primary" size="small" @click="createNewChat">
          <el-icon><Plus /></el-icon> 新对话
        </el-button>
      </div>
      <div class="chat-list">
        <div
          v-for="chat in chatHistory"
          :key="chat.id"
          :class="['chat-item', { active: chat.id === currentChatId }]"
          @click="selectChat(chat.id)"
        >
          <el-icon class="chat-icon"><ChatLineSquare /></el-icon>
          <span class="chat-title">{{ chat.title || '新对话' }}</span>
          <div class="chat-actions" @click.stop>
            <el-icon @click="handleRename(chat.id, chat.title)"><Edit /></el-icon>
            <el-icon @click="handleDeleteChat(chat.id)"><Delete /></el-icon>
          </div>
        </div>
        <div v-if="chatHistory.length === 0" class="empty-tip">
          暂无对话记录
        </div>
        <div v-else class="debug-tip">
          共 {{ chatHistory.length }} 条记录
        </div>
      </div>
    </aside>

    <!-- 主对话区 -->
    <div class="chat-main">
      <!-- 欢迎页 -->
      <div v-if="messages.length === 0 && !isLoading" class="welcome-page">
        <div class="welcome-icon">
          <el-icon :size="60"><ChatDotRound /></el-icon>
        </div>
        <h2>AI 智能助手</h2>
        <p>你可以问我关于系统操作、权限管理、技术文档等问题</p>
        <div class="quick-questions">
          <el-tag
            v-for="q in quickQuestions"
            :key="q"
            class="question-tag"
            @click="sendMessage(q)"
          >
            {{ q }}
          </el-tag>
        </div>
      </div>

      <!-- 消息列表 -->
      <div v-else ref="messageListRef" class="message-list">
        <div
          v-for="msg in messages"
          :key="msg.id"
          :class="['message-item', msg.role]"
        >
          <!-- 头像 -->
          <div :class="['avatar', msg.role]">
            <el-icon v-if="msg.role === 'user'"><User /></el-icon>
            <el-icon v-else><MagicStick /></el-icon>
          </div>

          <!-- 内容 -->
          <div class="message-content">
            <div :class="['bubble', msg.role]">
              <!-- 加载中 - 打字机效果 -->
              <template v-if="msg.loading && !msg.content">
                <span class="typing-cursor"></span>
              </template>
              <!-- 消息内容（带打字机效果） -->
              <div v-else class="message-text" v-html="renderMarkdown(msg.content)"></div>
              <!-- 加载中时显示光标 -->
              <span v-if="msg.loading && msg.content" class="typing-cursor"></span>
            </div>

            <!-- 操作按钮 -->
            <div v-if="!msg.loading && msg.role === 'assistant'" class="message-actions">
              <el-button link size="small" @click="copyMessage(msg.content)">
                <el-icon><CopyDocument /></el-icon> 复制
              </el-button>
              <el-button link size="small" @click="regenerateMessage(msg.id)">
                <el-icon><RefreshRight /></el-icon> 重新生成
              </el-button>
              <div class="rating">
                <el-button
                  :type="msg.rating === 'good' ? 'primary' : 'info'"
                  size="small"
                  link
                  @click="handleRating(msg.id, 'good')"
                >
                  <el-icon><Select /></el-icon> 有帮助
                </el-button>
                <el-button
                  :type="msg.rating === 'bad' ? 'danger' : 'info'"
                  size="small"
                  link
                  @click="handleRating(msg.id, 'bad')"
                >
                  <el-icon><CloseBold /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="input-area">
        <div class="input-wrapper">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="1"
            :autosize="{ minRows: 1, maxRows: 4 }"
            placeholder="输入问题，Enter 发送，Shift+Enter 换行"
            :disabled="isLoading"
            @keydown.enter.exact.prevent="handleSend"
            @input="autoResize"
          />
          <el-button
            type="primary"
            :loading="isLoading"
            :disabled="!inputText.trim()"
            @click="handleSend"
          >
            发送
          </el-button>
        </div>
        <p class="input-tip">AI 助手会尽力提供准确信息，请酌情参考</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, ChatLineSquare, Edit, Delete, ChatDotRound,
  User, MagicStick, CopyDocument, RefreshRight,
  Select, CloseBold
} from '@element-plus/icons-vue'
import {
  getChatHistory,
  getChatMessages,
  createChat,
  deleteChat as deleteChatApi,
  updateChat,
  submitRating
} from '@/api/ai-chat'

const props = defineProps({
  showSidebar: { type: Boolean, default: true }
})

// 状态
const chatHistory = ref([])
const currentChatId = ref(null)
const messages = ref([])
const isLoading = ref(false)
const inputText = ref('')
const messageListRef = ref(null)

// 快捷问题
const quickQuestions = [
  '如何创建新用户？',
  '权限管理有哪些最佳实践？',
  '系统日志在哪里查看？',
  '如何配置角色权限？'
]

// 加载对话历史
const loadChatHistory = async () => {
  try {
    const res = await getChatHistory()
    // 确保是数组
    chatHistory.value = Array.isArray(res.data) ? [...res.data] : []
    console.log('[AI聊天] 历史记录数量:', chatHistory.value.length)
  } catch (e) {
    console.error('加载对话历史失败', e)
  }
}

// 创建新对话
const createNewChat = () => {
  currentChatId.value = null
  messages.value = []
}

// 选择对话
const selectChat = async (chatId) => {
  currentChatId.value = chatId
  isLoading.value = true

  try {
    const res = await getChatMessages(chatId)
    messages.value = res.data || []
    scrollToBottom()
  } catch (e) {
    ElMessage.error('加载消息失败')
  } finally {
    isLoading.value = false
  }
}

// 重命名
const handleRename = async (chatId, currentTitle) => {
  try {
    const { value: newTitle } = await ElMessageBox.prompt(
      '输入新的对话标题',
      '重命名',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: currentTitle
      }
    )
    await updateChat(chatId, { title: newTitle })
    await loadChatHistory()
    ElMessage.success('重命名成功')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('重命名失败')
    }
  }
}

// 删除对话
const handleDeleteChat = async (chatId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个对话吗？', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteChatApi(chatId)
    await loadChatHistory()
    if (currentChatId.value === chatId) {
      createNewChat()
    }
    ElMessage.success('删除成功')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 发送消息
const sendMessage = async (content) => {
  if (isLoading.value || !content.trim()) return

  // 添加用户消息
  const userMsg = {
    id: Date.now(),
    role: 'user',
    content: content.trim(),
    rating: ''
  }
  messages.value.push(userMsg)
  scrollToBottom()

  isLoading.value = true

  // 创建或使用当前对话
  if (!currentChatId.value) {
    try {
      const res = await createChat({ title: content.slice(0, 20) })
      currentChatId.value = res.data
      await loadChatHistory()
    } catch (e) {
      console.error('创建对话失败', e)
    }
  }

  // AI 响应占位
  const aiMsgId = Date.now() + 1
  const aiMsg = {
    id: aiMsgId,
    role: 'assistant',
    content: '',
    sources: [],
    rating: '',
    loading: true
  }
  messages.value.push(aiMsg)

  try {
    const response = await fetch('/api/ai/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + localStorage.getItem('token')
      },
      body: JSON.stringify({
        chatId: currentChatId.value,
        content: content.trim(),
        messageId: aiMsgId
      })
    })

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let fullContent = ''
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.slice(6)
          if (data === '[DONE]') continue

          try {
            const json = JSON.parse(data)
            if (json.content) {
              fullContent += json.content
              updateMessage(aiMsgId, fullContent)
            }
          } catch (e) {
            if (!data.startsWith('{')) {
              fullContent += data
              updateMessage(aiMsgId, fullContent)
            }
          }
        }
      }
      scrollToBottom()
    }

    finishMessage(aiMsgId)

  } catch (e) {
    ElMessage.error('发送消息失败：' + e.message)
    messages.value = messages.value.filter(m => m.id !== aiMsgId)
  } finally {
    isLoading.value = false
  }
}

// 更新消息
const updateMessage = (msgId, content) => {
  const msg = messages.value.find(m => m.id === msgId)
  if (msg) {
    msg.content = content
    msg.loading = true
  }
}

// 完成消息
const finishMessage = (msgId) => {
  const msg = messages.value.find(m => m.id === msgId)
  if (msg) {
    msg.loading = false
  }
}

// 处理发送
const handleSend = () => {
  if (inputText.value.trim()) {
    sendMessage(inputText.value)
    inputText.value = ''
  }
}

// 自动调整高度
const autoResize = () => {
  // Element Plus 自动处理
}

// 复制消息
const copyMessage = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch (e) {
    ElMessage.error('复制失败')
  }
}

// 评价
const handleRating = async (messageId, rating) => {
  try {
    const msg = messages.value.find(m => m.id === messageId)
    const currentRating = msg?.rating === rating ? '' : rating
    await submitRating(messageId, currentRating)
    if (msg) {
      msg.rating = currentRating
    }
    if (currentRating) {
      ElMessage.success('感谢反馈')
    }
  } catch (e) {
    ElMessage.error('评价失败')
  }
}

// 重新生成
const regenerateMessage = (messageId) => {
  const msgIndex = messages.value.findIndex(m => m.id === messageId)
  if (msgIndex > 0) {
    const userMsg = messages.value[msgIndex - 1]
    if (userMsg.role === 'user') {
      messages.value = messages.value.filter(m => m.id !== messageId)
      sendMessage(userMsg.content)
    }
  }
}

// Markdown 渲染（增强版）
const renderMarkdown = (text) => {
  if (!text) return ''

  let html = text
    // 转义 HTML 特殊字符（安全处理）
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // 代码块 ```language\ncode\n```
  html = html.replace(/```(\w*)\n?([\s\S]*?)```/g, (match, lang, code) => {
    return `<pre><code class="${lang ? 'language-' + lang : ''}">${code.trim()}</code></pre>`
  })

  // 行内代码 `code`
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>')

  // 加粗 **text**
  html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')

  // 斜体 *text*
  html = html.replace(/(?<!\*)\*([^*\n]+)\*(?!\*)/g, '<em>$1</em>')

  // 删除线 ~~text~~
  html = html.replace(/~~([^~]+)~~/g, '<del>$1</del>')

  // 无序列表 - item
  html = html.replace(/^- (.+)$/gm, '<li>$1</li>')
  html = html.replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>')

  // 有序列表 1. item
  html = html.replace(/^\d+\. (.+)$/gm, '<li>$1</li>')

  // 链接 [text](url)
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')

  // 标题 ### text
  html = html.replace(/^### (.+)$/gm, '<h4>$1</h4>')
  html = html.replace(/^## (.+)$/gm, '<h3>$1</h3>')
  html = html.replace(/^# (.+)$/gm, '<h2>$1</h2>')

  // 引用 > text
  html = html.replace(/^&gt; (.+)$/gm, '<blockquote>$1</blockquote>')

  // 换行（保留空行）
  html = html.replace(/\n\n/g, '</p><p>')
  html = html.replace(/\n/g, '<br>')

  // 包裹段落
  if (!html.startsWith('<')) {
    html = '<p>' + html + '</p>'
  }

  return html
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

onMounted(() => {
  loadChatHistory()
})
</script>

<style lang="scss" scoped>
.ai-chat-page {
  display: flex;
  height: 100%;
  background: #f5f7fa;
}

// 侧边栏
.chat-sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e4e7ed;

  span {
    font-weight: 600;
    color: #303133;
  }
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.chat-item {
  display: flex !important;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: #606266;
  margin-bottom: 4px;
  background: #fff;
  border: 1px solid #ddd;
  visibility: visible !important;

  &:hover {
    background: #f5f7fa;
    color: #303133;

    .chat-actions {
      opacity: 1;
    }
  }

  &.active {
    background: #ecf5ff;
    color: #409eff;
  }
}

.chat-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.chat-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.chat-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;

  .el-icon {
    padding: 4px;
    font-size: 14px;
    border-radius: 4px;

    &:hover {
      background: rgba(0, 0, 0, 0.05);
    }
  }
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 20px;
  font-size: 14px;
}

.debug-tip {
  text-align: center;
  color: #409eff;
  padding: 10px;
  font-size: 12px;
  background: #f0f9ff;
  margin-top: 8px;
  border-radius: 4px;
}

// 主对话区
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.welcome-page {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;

  .welcome-icon {
    color: #409eff;
    margin-bottom: 20px;
  }

  h2 {
    font-size: 24px;
    color: #303133;
    margin-bottom: 12px;
  }

  p {
    color: #909399;
    font-size: 15px;
    margin-bottom: 30px;
  }
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  max-width: 600px;
}

.question-tag {
  cursor: pointer;
  padding: 10px 16px;
  font-size: 14px;
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

// 消息列表
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message-item {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;

  &.user {
    flex-direction: row-reverse;

    .bubble {
      background: #409eff;
      color: #fff;
      border-radius: 18px 18px 4px 18px;
    }

    .message-actions {
      justify-content: flex-end;
    }
  }

  &.assistant .bubble {
    background: #fff;
    border: 1px solid #e4e7ed;
    border-radius: 18px 18px 18px 4px;
  }
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;

  &.user {
    background: #409eff;
    color: #fff;
  }

  &.assistant {
    background: #f0f9ff;
    color: #409eff;
  }
}

.message-content {
  flex: 1;
  max-width: 700px;
}

.bubble {
  padding: 14px 18px;
  line-height: 1.7;
  font-size: 15px;

  :deep(pre) {
    background: #f6f8fa;
    border-radius: 6px;
    padding: 12px;
    overflow-x: auto;
    margin: 10px 0;
  }

  :deep(code) {
    font-family: 'Fira Code', monospace;
    font-size: 13px;
  }

  :deep(ul), :deep(ol) {
    margin: 8px 0;
    padding-left: 20px;
  }

  :deep(li) {
    margin: 4px 0;
  }

  :deep(blockquote) {
    border-left: 3px solid #409eff;
    padding-left: 12px;
    margin: 8px 0;
    color: #606266;
    background: #f5f7fa;
    padding: 8px 12px;
    border-radius: 0 4px 4px 0;
  }

  :deep(h2), :deep(h3), :deep(h4) {
    margin: 12px 0 8px;
    font-weight: 600;
  }

  :deep(a) {
    color: #409eff;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  :deep(strong) {
    font-weight: 600;
    color: #303133;
  }

  :deep(del) {
    color: #909399;
    text-decoration: line-through;
  }

  :deep(p) {
    margin: 8px 0;
  }
}

.message-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  flex-wrap: wrap;
}

.rating {
  display: flex;
  gap: 4px;
  margin-left: 8px;
  padding-left: 8px;
  border-left: 1px solid #e4e7ed;
}

// 打字机光标
.typing-cursor {
  display: inline-block;
  width: 2px;
  height: 1.2em;
  background: #409eff;
  margin-left: 2px;
  vertical-align: text-bottom;
  animation: blink 0.8s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

// 输入区
.input-area {
  padding: 16px 20px 20px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;

  .el-textarea {
    flex: 1;
  }
}

.input-tip {
  text-align: center;
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}
</style>
