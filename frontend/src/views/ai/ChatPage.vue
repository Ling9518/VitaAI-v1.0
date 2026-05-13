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
          </div>
          <el-empty v-if="!diagnoses.length" description="暂无诊断记录" :image-size="60" />
        </div>
      </aside>

      <!-- Main chat area -->
      <main class="chat-main">
        <div v-if="!currentConversationId" class="chat-welcome">
          <span class="welcome-icon">🩺</span>
          <h2>VitaAI 智能诊断</h2>
          <p>请描述您的症状、不适感或健康问题，AI医生将为您提供专业的初步诊断建议</p>
          <div class="quick-prompts">
            <div class="quick-prompt" v-for="p in quickPrompts" :key="p" @click="sendQuickPrompt(p)">{{ p }}</div>
          </div>
        </div>

        <div v-else class="chat-messages" ref="chatBox">
          <div v-for="(msg, i) in messages" :key="i" class="message" :class="msg.role">
            <div class="message-avatar">
              <span v-if="msg.role === 'user'">👤</span>
              <span v-else>🩺</span>
            </div>
            <div class="message-content" v-html="renderMarkdown(msg.content)" />
          </div>
          <div v-if="thinking" class="message assistant">
            <div class="message-avatar"><span>🩺</span></div>
            <div class="message-content typing-indicator">
              <span></span><span></span><span></span>
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
import { ref, onMounted, nextTick, watch } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import api from '@/api/index'
import { ElMessage } from 'element-plus'
import MarkdownIt from 'markdown-it'

const md = new MarkdownIt({ breaks: true, linkify: true })

const diagnoses = ref<any[]>([])
const currentConversationId = ref('')
const messages = ref<any[]>([])
const inputText = ref('')
const thinking = ref(false)
const chatBox = ref<HTMLElement>()

const quickPrompts = [
  '我最近一直头痛，伴有恶心，是怎么回事？',
  '我有咳嗽和发烧症状，需要吃什么药？',
  '最近总是失眠，白天没精神，怎么办？',
  '我经常胃疼，饭后加重，可能是什么问题？',
]

function formatDate(date: string) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function renderMarkdown(text: string) {
  if (!text) return ''
  return md.render(text)
}

async function fetchDiagnoses() {
  try {
    const res = await api.get('/ai/diagnoses', { params: { page: 1, pageSize: 50 } })
    diagnoses.value = res.data.data.records || []
  } catch { /* empty */ }
}

async function switchChat(d: any) {
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
  } catch { /* empty */ }
}

function startNewChat() {
  currentConversationId.value = ''
  messages.value = []
  inputText.value = ''
}

async function startChat() {
  if (!inputText.value.trim()) return
  const text = inputText.value.trim()
  inputText.value = ''
  messages.value.push({ role: 'user', content: text })
  thinking.value = true
  await scrollToBottom()
  try {
    const res = await api.post('/ai/chat', { message: text })
    const data = res.data.data
    currentConversationId.value = data.conversationId
    messages.value.push({ role: 'assistant', content: data.content })
    fetchDiagnoses()
  } catch { /* error handled by interceptor */ }
  finally {
    thinking.value = false
    await scrollToBottom()
  }
}

async function handleSend() {
  if (!inputText.value.trim() || thinking.value) return
  const text = inputText.value.trim()
  inputText.value = ''
  messages.value.push({ role: 'user', content: text })
  thinking.value = true
  await scrollToBottom()
  try {
    const res = await api.post('/ai/chat', { message: text, conversationId: currentConversationId.value })
    const data = res.data.data
    messages.value.push({ role: 'assistant', content: data.content })
    fetchDiagnoses()
  } catch { /* error handled by interceptor */ }
  finally {
    thinking.value = false
    await scrollToBottom()
  }
}

function sendQuickPrompt(prompt: string) {
  inputText.value = prompt
  startChat()
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
  width: 280px; border-right: 1px solid var(--border); background: var(--bg-card);
  display: flex; flex-direction: column; flex-shrink: 0;
}
.sidebar-header {
  padding: 20px; border-bottom: 1px solid var(--border);
  display: flex; align-items: center; justify-content: space-between;
}
.sidebar-header h3 { font-size: 16px; font-weight: 700; }
.sidebar-list { flex: 1; overflow-y: auto; padding: 12px; }
.sidebar-item {
  padding: 12px; border-radius: var(--radius-sm); cursor: pointer; margin-bottom: 4px;
  transition: var(--transition);
}
.sidebar-item:hover, .sidebar-item.active { background: #eff6ff; }
.sidebar-item-title { font-size: 14px; font-weight: 600; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.sidebar-item-date { font-size: 12px; color: var(--text-light); }

.chat-main {
  flex: 1; display: flex; flex-direction: column; background: var(--bg);
  min-width: 0;
}
.chat-welcome {
  flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 48px; text-align: center;
}
.welcome-icon { font-size: 72px; margin-bottom: 24px; }
.chat-welcome h2 { font-size: 28px; font-weight: 800; margin-bottom: 12px; }
.chat-welcome p { color: var(--text-secondary); max-width: 500px; margin-bottom: 32px; font-size: 15px; }
.quick-prompts { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; max-width: 560px; }
.quick-prompt {
  padding: 14px 18px; background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius-sm); cursor: pointer; font-size: 14px; transition: var(--transition);
  text-align: left; color: var(--text-secondary);
}
.quick-prompt:hover { border-color: var(--primary); color: var(--primary); background: #f8faff; }

.chat-messages { flex: 1; overflow-y: auto; padding: 24px 32px; display: flex; flex-direction: column; gap: 20px; }
.message { display: flex; gap: 12px; max-width: 80%; }
.message.user { align-self: flex-end; flex-direction: row-reverse; }
.message.assistant { align-self: flex-start; }
.message-avatar { width: 40px; height: 40px; border-radius: 50%; background: #eff6ff; display: flex; align-items: center; justify-content: center; font-size: 20px; flex-shrink: 0; }
.message.user .message-avatar { background: #e8f5e9; }
.message-content {
  background: var(--bg-card); padding: 16px 20px; border-radius: 18px;
  font-size: 15px; line-height: 1.7; box-shadow: var(--shadow); border: 1px solid var(--border);
}
.message.user .message-content { background: var(--primary); color: white; border: none; }
.message-content :deep(p) { margin: 0 0 8px; }
.message-content :deep(p:last-child) { margin-bottom: 0; }
.message-content :deep(ul), .message-content :deep(ol) { margin: 8px 0; padding-left: 20px; }
.message-content :deep(li) { margin-bottom: 4px; }
.message-content :deep(strong) { font-weight: 700; }
.message-content :deep(h3) { font-size: 17px; font-weight: 700; margin: 12px 0 6px; }
.typing-indicator { display: flex; gap: 6px; padding: 20px 28px; }
.typing-indicator span {
  width: 8px; height: 8px; border-radius: 50%; background: var(--text-light);
  animation: bounce 1.4s infinite both;
}
.typing-indicator span:nth-child(2) { animation-delay: .2s; }
.typing-indicator span:nth-child(3) { animation-delay: .4s; }
@keyframes bounce { 0%,80%,100% { transform: translateY(0); } 40% { transform: translateY(-8px); } }

.chat-input-area { padding: 16px 32px 20px; border-top: 1px solid var(--border); background: var(--bg-card); }
.chat-input-row { display: flex; gap: 12px; align-items: flex-end; }
.chat-input-row :deep(.el-textarea__inner) { border-radius: 16px; font-size: 15px; }
.send-btn { width: 48px; height: 48px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; padding: 0; }
.chat-disclaimer { font-size: 12px; color: var(--text-light); text-align: center; margin-top: 10px; }

@media (max-width: 768px) {
  .chat-sidebar { display: none; }
  .message { max-width: 90%; }
  .chat-messages { padding: 16px; }
  .chat-input-area { padding: 12px 16px; }
  .quick-prompts { grid-template-columns: 1fr; }
}
</style>
