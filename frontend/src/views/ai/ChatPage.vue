<template>
  <div class="chat-page">
    <AppHeader />
    <div class="chat-layout">
      <!-- Sidebar: diagnosis history -->
      <aside class="chat-sidebar">
        <div class="sidebar-header">
          <h3>诊断记录</h3>
          <el-button size="small" @click="startNewChat">新建对话</el-button>
        </div>
        <div class="sidebar-list">
          <div
            v-for="d in diagnoses"
            :key="d.id"
            class="sidebar-item"
            :class="{ active: d.conversationId === currentConversationId }"
            @click="switchChat(d)"
          >
            <div class="sidebar-item-title">{{ d.symptomSummary || '未命名诊断' }}</div>
            <div class="sidebar-item-date">{{ formatDate(d.createdAt) }}</div>
            <button class="sidebar-delete" @click.stop="handleDeleteDiagnosis(d)" title="删除记录">&times;</button>
          </div>
          <el-empty v-if="!diagnoses.length" description="暂无诊断记录" :image-size="60" />
        </div>
      </aside>

      <!-- Main chat area -->
      <main class="chat-main">
        <div v-if="!currentConversationId && !thinking" class="chat-welcome">
          <span class="welcome-icon">🩺</span>
          <h2>VitaAI 智能诊断</h2>
          <p>请描述您的症状、不适感或健康问题，AI医生将为您提供专业的初步诊断建议</p>
          <div class="quick-prompts">
            <div class="quick-prompt" v-for="p in quickPrompts" :key="p" @click="sendQuickPrompt(p)">{{ p }}</div>
          </div>
        </div>

        <div v-if="currentConversationId || thinking" class="chat-messages" ref="chatBox">
          <div v-for="(msg, i) in messages" :key="i" class="message" :class="msg.role">
            <div class="message-avatar">
              <span v-if="msg.role === 'user'">👤</span>
              <span v-else>🩺</span>
            </div>
            <div class="message-content" v-html="renderMarkdown(msg.content)" />
          </div>
          <div v-if="thinking" class="message assistant">
            <div class="message-avatar"><span>🩺</span></div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
              <div v-if="loadingStatus" class="loading-status">{{ loadingStatus }}</div>
            </div>
          </div>
        </div>

        <div class="chat-input-area" v-if="currentConversationId">
          <div class="chat-input-row">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="2"
              placeholder="请描述您的症状..."
              @keydown.enter.exact="handleSend"
              resize="none"
            />
            <button class="btn-primary send-btn" :disabled="!inputText.trim() || thinking" @click="handleSend">
              <el-icon><Promotion /></el-icon>
            </button>
          </div>
          <p class="chat-disclaimer">内容为AI诊断，想要更准确诊断，请去正规医院就诊。</p>
        </div>

        <div class="chat-input-area" v-else>
          <div class="chat-input-row">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="2"
              placeholder="请描述您的症状，AI医生将为您分析..."
              @keydown.enter.exact="startChat"
              resize="none"
            />
            <button class="btn-primary send-btn" :disabled="!inputText.trim() || thinking" @click="startChat">
              <el-icon><Promotion /></el-icon>
            </button>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import api from '@/api/index'
import { ElMessage } from 'element-plus'
import { formatDate } from '@/utils'
import type { DiagnosisRecord, ChatMessage } from '@/types'
import MarkdownIt from 'markdown-it'

const md = new MarkdownIt({ breaks: true, linkify: true, html: false })

const diagnoses = ref<DiagnosisRecord[]>([])
const currentConversationId = ref('')
const messages = ref<ChatMessage[]>([])
const inputText = ref('')
const thinking = ref(false)
const loadingStatus = ref('')
const chatBox = ref<HTMLElement>()

const quickPrompts = [
  '我最近一直头痛，伴有恶心，是怎么回事？',
  '我有咳嗽和发烧症状，需要吃什么药？',
  '最近总是失眠，白天没精神，怎么办？',
  '我经常胃疼，饭后加重，可能是什么问题？',
]

function renderMarkdown(text: string) {
  if (!text) return ''
  return md.render(text)
}

async function fetchDiagnoses() {
  try {
    const res = await api.get('/ai/diagnoses', { params: { page: 1, pageSize: 50 } })
    diagnoses.value = res.data.data.list || []
  } catch { ElMessage.error('加载失败') }
}

async function switchChat(d: DiagnosisRecord) {
  currentConversationId.value = d.conversationId
  messages.value = []
  try {
    const res = await api.get(`/ai/diagnoses/${d.id}`)
    const detail = res.data.data
    if (detail.messages) {
      messages.value = detail.messages.map((m: any) => ({
        role: m.role === 'USER' ? 'user' : 'assistant',
        content: m.content,
      }))
    }
    scrollToBottom()
  } catch { ElMessage.error('加载失败') }
}

function startNewChat() {
  currentConversationId.value = ''
  messages.value = []
  inputText.value = ''
  fetchDiagnoses()
}

async function streamAI(body: Record<string, string>) {
  const token = localStorage.getItem('token')
  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), 300_000) // 5 min timeout

  try {
    loadingStatus.value = '正在连接AI...'
    const response = await fetch('/api/ai/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
      },
      body: JSON.stringify(body),
      signal: controller.signal,
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }

    const reader = response.body!.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    loadingStatus.value = 'AI正在思考...'

    // Add placeholder message for streaming content
    const msgIndex = messages.value.length
    messages.value.push({ role: 'assistant', content: '', streaming: true })

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (!line.trim()) continue

        // Parse SSE: "event:message\ndata:{...}"
        let dataStr = ''
        if (line.startsWith('data:')) {
          dataStr = line.slice(5).trim()
        } else if (line.startsWith('event:')) {
          continue // skip event line, data follows
        } else {
          continue
        }

        try {
          const event = JSON.parse(dataStr)
          if (event.type === 'connected') {
            loadingStatus.value = 'AI正在分析您的症状...'
          } else if (event.type === 'chunk') {
            loadingStatus.value = ''
            messages.value[msgIndex].content += event.content
            if (event.conversationId) {
              currentConversationId.value = event.conversationId
            }
            await nextTick()
            scrollToBottom()
          } else if (event.type === 'done') {
            messages.value[msgIndex].streaming = false
            fetchDiagnoses()
          }
        } catch {
          // Skip unparsable lines
        }
      }
    }
  } catch (err: any) {
    if (err.name === 'AbortError') {
      ElMessage.error('AI响应超时，请重试')
    } else {
      ElMessage.error('AI服务暂时不可用，请稍后再试')
    }
    // Remove placeholder on error
    const last = messages.value[messages.value.length - 1]
    if (last?.streaming && !last.content) {
      messages.value.pop()
    }
    throw err
  } finally {
    clearTimeout(timeoutId)
    thinking.value = false
    loadingStatus.value = ''
    scrollToBottom()
  }
}

async function startChat() {
  if (!inputText.value.trim()) return
  const text = inputText.value.trim()
  inputText.value = ''
  messages.value.push({ role: 'user', content: text })
  thinking.value = true
  await scrollToBottom()
  streamAI({ message: text })
}

async function handleSend() {
  if (!inputText.value.trim() || thinking.value) return
  const text = inputText.value.trim()
  inputText.value = ''
  messages.value.push({ role: 'user', content: text })
  thinking.value = true
  await scrollToBottom()
  streamAI({ message: text, conversationId: currentConversationId.value })
}

function sendQuickPrompt(prompt: string) {
  inputText.value = prompt
  startChat()
}

async function handleDeleteDiagnosis(d: DiagnosisRecord) {
  if (!confirm('确定删除此诊断记录吗？')) return
  try {
    await api.delete(`/ai/diagnoses/${d.id}`)
    ElMessage.success('删除成功')
    if (currentConversationId.value === d.conversationId) {
      startNewChat()
    }
    fetchDiagnoses()
  } catch { /* handled by interceptor */ }
}

async function scrollToBottom() {
  await nextTick()
  if (chatBox.value) {
    chatBox.value.scrollTop = chatBox.value.scrollHeight
  }
}

onMounted(() => {
  fetchDiagnoses()
})
</script>

<style scoped>
.chat-page { min-height: 100vh; display: flex; flex-direction: column; }
.chat-layout { flex: 1; display: flex; max-width: 1400px; margin: 0 auto; width: 100%; height: calc(100vh - 64px); }

.chat-sidebar {
  width: 280px; border-right: 1px solid var(--border); background: rgba(255,255,255,.6);
  backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px);
  display: flex; flex-direction: column; flex-shrink: 0;
}
.sidebar-header {
  padding: 20px; border-bottom: 1px solid var(--border);
  display: flex; align-items: center; justify-content: space-between;
}
.sidebar-header h3 { font-size: 16px; font-weight: 700; }
.sidebar-list { flex: 1; overflow-y: auto; padding: 12px; }
.sidebar-item {
  padding: 14px; border-radius: var(--radius-sm); cursor: pointer; margin-bottom: 4px;
  transition: all .2s ease; border: 1px solid transparent;
}
.sidebar-item:hover { background: #f8faff; border-color: rgba(37,99,235,.08); }
.sidebar-item.active { background: linear-gradient(135deg, #eff6ff, #f0f9ff); border-color: rgba(37,99,235,.12); }
.sidebar-item { position: relative; }
.sidebar-item-title { font-size: 14px; font-weight: 600; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding-right: 24px; }
.sidebar-item-date { font-size: 12px; color: var(--text-light); }
.sidebar-delete {
  position: absolute; top: 8px; right: 8px;
  width: 20px; height: 20px; border-radius: 50%;
  border: none; background: transparent; color: var(--text-light);
  font-size: 18px; line-height: 1; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: all .2s ease;
}
.sidebar-item:hover .sidebar-delete { opacity: 1; }
.sidebar-delete:hover { background: #fee2e2; color: #dc2626; }

.chat-main {
  flex: 1; display: flex; flex-direction: column; background: var(--bg);
  min-width: 0;
}
.chat-welcome {
  flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 48px; text-align: center; animation: fadeInUp .5s ease-out;
}
.welcome-icon { font-size: 80px; margin-bottom: 24px; animation: float 4s ease-in-out infinite; }
.chat-welcome h2 { font-size: 30px; font-weight: 800; margin-bottom: 12px; letter-spacing: -0.5px; }
.chat-welcome p { color: var(--text-secondary); max-width: 500px; margin-bottom: 36px; font-size: 15px; line-height: 1.7; }
.quick-prompts { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; max-width: 580px; }
.quick-prompt {
  padding: 16px 20px; background: var(--bg-card); border: 1px solid var(--border);
  border-radius: 14px; cursor: pointer; font-size: 14px; transition: all .25s ease;
  text-align: left; color: var(--text-secondary); line-height: 1.5;
}
.quick-prompt:hover { border-color: var(--primary); color: var(--primary); background: #f8faff; transform: translateY(-2px); box-shadow: var(--shadow-md); }

.chat-messages { flex: 1; overflow-y: auto; padding: 28px 36px; display: flex; flex-direction: column; gap: 24px; }
.message { display: flex; gap: 14px; max-width: 78%; animation: fadeInUp .3s ease-out; }
.message.user { align-self: flex-end; flex-direction: row-reverse; }
.message.assistant { align-self: flex-start; }
.message-avatar {
  width: 42px; height: 42px; border-radius: 50%;
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  display: flex; align-items: center; justify-content: center; font-size: 22px; flex-shrink: 0;
  box-shadow: var(--shadow);
}
.message.user .message-avatar { background: linear-gradient(135deg, #e8f5e9, #c8e6c9); }
.message-content {
  background: var(--bg-card); padding: 18px 22px; border-radius: 20px;
  font-size: 15px; line-height: 1.8; box-shadow: var(--shadow); border: 1px solid var(--border);
}
.message.user .message-content {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: white; border: none; box-shadow: 0 4px 16px rgba(37,99,235,.3);
}
.message-content :deep(p) { margin: 0 0 8px; }
.message-content :deep(p:last-child) { margin-bottom: 0; }
.message-content :deep(ul), .message-content :deep(ol) { margin: 8px 0; padding-left: 20px; }
.message-content :deep(li) { margin-bottom: 4px; }
.message-content :deep(strong) { font-weight: 700; }
.message-content :deep(h3) { font-size: 17px; font-weight: 700; margin: 12px 0 6px; }
.message.user .message-content :deep(strong) { color: #fff; }
.typing-indicator { display: flex; gap: 6px; padding: 20px 28px; }
.typing-indicator span {
  width: 8px; height: 8px; border-radius: 50%; background: var(--text-light);
  animation: bounce 1.4s infinite both;
}
.typing-indicator span:nth-child(2) { animation-delay: .2s; }
.typing-indicator span:nth-child(3) { animation-delay: .4s; }
.loading-status { text-align: center; font-size: 13px; color: var(--text-light); margin-top: 10px; }
@keyframes bounce { 0%,80%,100% { transform: translateY(0); } 40% { transform: translateY(-8px); } }

.chat-input-area { padding: 18px 36px 22px; border-top: 1px solid var(--border); background: rgba(255,255,255,.8); backdrop-filter: blur(12px); }
.chat-input-row { display: flex; gap: 14px; align-items: flex-end; }
.chat-input-row :deep(.el-textarea__inner) { border-radius: 18px; font-size: 15px; resize: none; }
.send-btn {
  width: 48px; height: 48px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; padding: 0; transition: all .2s ease;
}
.send-btn:hover { transform: scale(1.05); }
.chat-disclaimer { font-size: 12px; color: var(--text-light); text-align: center; margin-top: 12px; }

@media (max-width: 768px) {
  .chat-sidebar { display: none; }
  .message { max-width: 90%; }
  .chat-messages { padding: 16px; }
  .chat-input-area { padding: 12px 16px; }
  .quick-prompts { grid-template-columns: 1fr; }
}
</style>
