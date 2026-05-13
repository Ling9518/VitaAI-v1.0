<template>
  <div class="history-page">
    <AppHeader />
    <div class="page-container section-padding">
      <div class="page-hero">
        <h1>诊断<span class="gradient-text">记录</span></h1>
        <p>查看您的AI诊断历史和详细报告</p>
      </div>

      <div v-if="records.length" class="history-list">
        <div class="history-card card" v-for="r in records" :key="r.id" @click="showDetail(r)">
          <div class="history-header">
            <h4>{{ r.symptomSummary || '未命名诊断' }}</h4>
            <span class="severity-tag" :class="(r.severityLevel || 'low').toLowerCase()">{{ severityMap[r.severityLevel] || '未知' }}</span>
          </div>
          <p class="history-summary">{{ truncateText(r.conversationSummary || r.symptomsDetail, 120) }}</p>
          <div class="history-meta">
            <span>{{ formatDate(r.createdAt) }}</span>
            <span>{{ r.messageCount || 0 }} 条消息</span>
            <span class="feedback-tag" :class="(r.feedbackAccuracy || 'PENDING').toLowerCase()">{{ feedbackMap[r.feedbackAccuracy] || '待评价' }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无诊断记录">
        <el-button type="primary" @click="$router.push('/ai-chat')">去AI诊断</el-button>
      </el-empty>

      <div class="pager" v-if="total > pageSize">
        <el-pagination v-model:current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="fetchData" />
      </div>
    </div>

    <!-- Detail dialog -->
    <el-dialog v-model="dialogVisible" :title="selectedRecord?.symptomSummary || '诊断详情'" width="700px" top="5vh">
      <div v-if="selectedRecord" class="dialog-body">
        <div class="detail-row"><span class="dl">症状摘要</span><span>{{ selectedRecord.symptomSummary }}</span></div>
        <div class="detail-row" v-if="selectedRecord.symptomsDetail"><span class="dl">症状详情</span><span>{{ selectedRecord.symptomsDetail }}</span></div>
        <div class="detail-row" v-if="selectedRecord.aiAnalysis"><span class="dl">AI分析</span><span>{{ selectedRecord.aiAnalysis }}</span></div>
        <div class="detail-row" v-if="selectedRecord.suggestedDiseases"><span class="dl">疑似疾病</span><span>{{ selectedRecord.suggestedDiseases }}</span></div>
        <div class="detail-row" v-if="selectedRecord.suggestedDrugs"><span class="dl">建议用药</span><span>{{ selectedRecord.suggestedDrugs }}</span></div>
        <div class="detail-row" v-if="selectedRecord.advice"><span class="dl">健康建议</span><span>{{ selectedRecord.advice }}</span></div>
        <div class="detail-row" v-if="selectedRecord.warningText"><span class="dl">⚠️ 警告</span><span class="warning">{{ selectedRecord.warningText }}</span></div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="dialogVisible = false; $router.push('/ai-chat')">继续诊断</el-button>
      </template>
    </el-dialog>

    <footer class="app-footer"><p>&copy; 2026 VitaAI 智慧医院系统. All rights reserved.</p></footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import api from '@/api/index'

const records = ref<any[]>([])
const page = ref(1)
const pageSize = 20
const total = ref(0)
const dialogVisible = ref(false)
const selectedRecord = ref<any>(null)

const severityMap: Record<string, string> = { LOW: '低风险', MEDIUM: '中等风险', HIGH: '高风险', CRITICAL: '危险' }
const feedbackMap: Record<string, string> = { ACCURATE: '准确', MOSTLY_ACCURATE: '大致准确', INACCURATE: '不准确', PENDING: '待评价' }

function truncateText(text: string, len: number) {
  return text && text.length > len ? text.substring(0, len) + '...' : text || ''
}

function formatDate(date: string) {
  return date ? new Date(date).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' }) : ''
}

async function fetchData() {
  try {
    const res = await api.get('/ai/diagnoses', { params: { page: page.value, pageSize } })
    const data = res.data.data
    records.value = data.list || []
    total.value = data.pagination?.total || 0
  } catch { /* empty */ }
}

async function showDetail(r: any) {
  try {
    const res = await api.get(`/ai/diagnoses/${r.id}`)
    selectedRecord.value = res.data.data
    dialogVisible.value = true
  } catch { /* empty */ }
}

onMounted(() => fetchData())
</script>

<style scoped>
.history-page { min-height: 100vh; display: flex; flex-direction: column; }
.page-hero { text-align: center; margin-bottom: 40px; }
.page-hero h1 { font-size: 36px; font-weight: 800; margin-bottom: 12px; }
.page-hero p { color: var(--text-secondary); font-size: 16px; }

.history-list { display: flex; flex-direction: column; gap: 16px; }
.history-card { cursor: pointer; padding: 24px; }
.history-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.history-header h4 { font-size: 18px; font-weight: 700; }
.severity-tag { padding: 4px 12px; border-radius: 50px; font-size: 12px; font-weight: 600; }
.severity-tag.low { background: #d1fae5; color: #065f46; }
.severity-tag.medium { background: #fef3c7; color: #92400e; }
.severity-tag.high { background: #fee2e2; color: #991b1b; }
.severity-tag.critical { background: #fce7f3; color: #9d174d; }
.history-summary { color: var(--text-secondary); font-size: 14px; line-height: 1.6; margin-bottom: 12px; }
.history-meta { display: flex; gap: 16px; font-size: 13px; color: var(--text-light); }
.feedback-tag.pending { color: var(--text-light); }
.feedback-tag.accurate { color: var(--success); }
.feedback-tag.mostly_accurate { color: var(--primary); }
.feedback-tag.inaccurate { color: var(--danger); }

.dialog-body { display: flex; flex-direction: column; gap: 16px; }
.detail-row { display: flex; gap: 16px; }
.dl { width: 80px; flex-shrink: 0; font-weight: 600; font-size: 14px; color: var(--text-secondary); }
.detail-row span:last-child { font-size: 14px; line-height: 1.6; }
.warning { color: var(--danger); font-weight: 600; }

.pager { display: flex; justify-content: center; margin-top: 40px; }
.app-footer { text-align: center; padding: 32px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }
</style>
