<template>
  <div class="messages-page">
    <AppHeader />
    <div class="page-container section-padding">
      <div class="page-hero">
        <h1>在线<span class="gradient-text">问诊</span></h1>
        <p>向在线医生留言咨询，医生将尽快为您解答</p>
      </div>

      <div class="new-message-card card">
        <h3>发起咨询</h3>
        <el-input v-model="newContent" type="textarea" :rows="3" placeholder="请描述您的问题或症状，在线医生将为您解答..." resize="none" />
        <div class="form-actions">
          <button class="btn-primary" :disabled="!newContent.trim() || sending" @click="handleCreate">
            {{ sending ? '提交中...' : '提交留言' }}
          </button>
        </div>
        <p class="wait-hint">您的需求正在排队解决，请耐心等待回复</p>
      </div>

      <div class="message-list" v-if="messages.length">
        <div class="message-card card" v-for="m in messages" :key="m.id" :class="{ resolved: m.status === 'RESOLVED' }">
          <div class="message-header">
            <div class="message-meta">
              <span class="status-tag" :class="m.status === 'RESOLVED' ? 'resolved' : 'unresolved'">
                {{ m.status === 'RESOLVED' ? '已解决' : '未解决' }}
              </span>
              <span class="message-date">{{ formatDate(m.createdAt) }}</span>
            </div>
            <div class="message-actions" v-if="m.status !== 'RESOLVED'">
              <el-button size="small" text @click="startEdit(m)">编辑</el-button>
              <el-button size="small" text type="danger" @click="handleDelete(m.id)">删除</el-button>
            </div>
          </div>
          <div class="message-content" v-if="editingId !== m.id">{{ m.content }}</div>
          <div class="edit-area" v-else>
            <el-input v-model="editContent" type="textarea" :rows="3" resize="none" />
            <div class="edit-actions">
              <el-button size="small" @click="cancelEdit">取消</el-button>
              <el-button size="small" type="primary" :disabled="!editContent.trim()" @click="handleUpdate(m.id)">保存</el-button>
            </div>
          </div>
          <div class="reply-section" v-if="m.reply">
            <div class="reply-label">医生回复：</div>
            <div class="reply-content">{{ m.reply }}</div>
            <div class="reply-date" v-if="m.repliedAt">回复于 {{ formatDate(m.repliedAt) }}</div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无咨询记录">
        <el-button type="primary" @click="focusInput">发起咨询</el-button>
      </el-empty>

      <div class="pager" v-if="total > pageSize">
        <el-pagination v-model:current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="fetchData" />
      </div>
    </div>
    <footer class="app-footer"><p>&copy; 2026 VitaAI 智慧医院系统. All rights reserved.</p></footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import api from '@/api/index'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '@/utils'

const messages = ref<any[]>([])
const loading = ref(false)
const newContent = ref('')
const sending = ref(false)
const editingId = ref<number | null>(null)
const editContent = ref('')
const page = ref(1)
const pageSize = 20
const total = ref(0)

async function focusInput() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

async function fetchData() {
  loading.value = true
  try {
    const res = await api.get('/messages', { params: { page: page.value, pageSize } })
    messages.value = res.data.data.list || []
    total.value = res.data.data.pagination?.total || 0
  } catch {
    ElMessage.error('加载消息列表失败')
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!newContent.value.trim()) return
  sending.value = true
  try {
    await api.post('/messages', { content: newContent.value.trim() })
    ElMessage.success('留言已提交')
    newContent.value = ''
    page.value = 1
    fetchData()
  } catch {
    ElMessage.error('提交留言失败')
  }
  finally { sending.value = false }
}

function startEdit(m: any) {
  editingId.value = m.id
  editContent.value = m.content
}

function cancelEdit() {
  editingId.value = null
  editContent.value = ''
}

async function handleUpdate(id: number) {
  try {
    await api.put(`/messages/${id}`, { content: editContent.value })
    ElMessage.success('留言已更新')
    cancelEdit()
    fetchData()
  } catch {
    ElMessage.error('更新留言失败')
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除此留言？', '确认删除', { type: 'warning' })
  } catch { return }
  try {
    await api.delete(`/messages/${id}`)
    ElMessage.success('留言已删除')
    fetchData()
  } catch {
    ElMessage.error('删除留言失败')
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.messages-page { min-height: 100vh; display: flex; flex-direction: column; }
.page-hero { text-align: center; margin-bottom: 36px; }
.page-hero h1 { font-size: 38px; font-weight: 800; margin-bottom: 8px; letter-spacing: -0.5px; }
.page-hero p { color: var(--text-secondary); font-size: 16px; }

.new-message-card { padding: 28px 32px; margin-bottom: 28px; border: 1px solid var(--border); }
.new-message-card h3 { font-size: 18px; font-weight: 700; margin-bottom: 16px; }
.new-message-card :deep(.el-textarea__inner) { border-radius: 12px; font-size: 15px; }
.form-actions { margin-top: 16px; }
.wait-hint { margin-top: 14px; font-size: 13px; color: var(--text-light); text-align: center; }

.message-list { display: flex; flex-direction: column; gap: 16px; }
.message-card { padding: 24px 28px; border: 1px solid var(--border); transition: all .25s ease; }
.message-card.resolved { background: #f9fafb; }
.message-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.message-meta { display: flex; align-items: center; gap: 12px; }
.status-tag { padding: 3px 12px; border-radius: 50px; font-size: 12px; font-weight: 600; }
.status-tag.unresolved { background: #fef3c7; color: #92400e; }
.status-tag.resolved { background: #d1fae5; color: #065f46; }
.message-date { font-size: 13px; color: var(--text-light); }
.message-content { color: var(--text); font-size: 15px; line-height: 1.7; white-space: pre-wrap; }
.edit-area { display: flex; flex-direction: column; gap: 12px; }
.edit-area :deep(.el-textarea__inner) { border-radius: 12px; }
.edit-actions { display: flex; gap: 8px; justify-content: flex-end; }

.reply-section { margin-top: 20px; padding: 18px 20px; background: linear-gradient(135deg, #eff6ff, #f0f9ff); border-radius: 12px; border: 1px solid #dbeafe; }
.reply-label { font-size: 13px; font-weight: 700; color: var(--primary); margin-bottom: 8px; }
.reply-content { font-size: 15px; line-height: 1.7; color: var(--text); white-space: pre-wrap; }
.reply-date { margin-top: 8px; font-size: 12px; color: var(--text-light); }

.pager { display: flex; justify-content: center; margin-top: 36px; }
.app-footer { text-align: center; padding: 36px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }
</style>
