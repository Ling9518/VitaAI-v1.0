<template>
  <div class="history-page">
    <AppHeader />
    <div class="page-container section-padding">
      <div class="page-hero">
        <h1>诊断<span class="gradient-text">记录</span></h1>
        <p>查看您的AI诊断历史和详细报告</p>
      </div>

      <div v-if="loading" class="loading-state"><el-icon class="is-loading"><Loading /></el-icon> 加载中...</div>
      <div v-else-if="records.length" class="history-list">
        <div class="history-card card" v-for="r in records" :key="r.id" @click="showDetail(r)">
          <button class="history-delete" @click.stop="handleDelete(r)" title="删除记录">&times;</button>
          <div class="history-header">
            <h4>{{ r.symptomSummary || '未命名诊断' }}</h4>
            <span class="severity-tag" :class="(r.severityLevel || 'LOW').toLowerCase()">{{ severityMap[r.severityLevel ?? 'LOW'] || '未知' }}</span>
          </div>
          <p class="history-summary">{{ truncateText((r.conversationSummary || r.symptomsDetail) ?? null, 120) }}</p>
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
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import type { DiagnosisRecord, DiagnosisDetail } from '@/types'
import { truncateText, formatDate, severityMap, feedbackMap } from '@/utils'

const records = ref<DiagnosisRecord[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = 20
const total = ref(0)
const dialogVisible = ref(false)
const selectedRecord = ref<DiagnosisDetail | null>(null)

async function fetchData() {
  loading.value = true
  try {
    const res = await api.get('/ai/diagnoses', { params: { page: page.value, pageSize } })
    const data = res.data.data
    records.value = data.list || []
    total.value = data.pagination?.total || 0
  } catch {
    ElMessage.error('加载诊断记录失败')
  } finally {
    loading.value = false
  }
}

async function handleDelete(r: DiagnosisRecord) {
  if (!confirm('确定删除此诊断记录吗？')) return
  try {
    await api.delete(`/ai/diagnoses/${r.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function showDetail(r: DiagnosisRecord) {
  try {
    const res = await api.get(`/ai/diagnoses/${r.id}`)
    selectedRecord.value = res.data.data
    dialogVisible.value = true
  } catch {
    ElMessage.error('加载诊断详情失败')
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.history-page { min-height: 100vh; display: flex; flex-direction: column; }
.page-hero { text-align: center; margin-bottom: 44px; }
.page-hero h1 { font-size: 38px; font-weight: 800; margin-bottom: 12px; letter-spacing: -0.5px; }
.page-hero p { color: var(--text-secondary); font-size: 16px; }

.history-list { display: flex; flex-direction: column; gap: 16px; }
.history-card {
  cursor: pointer; padding: 28px 32px; transition: all .3s cubic-bezier(.4,0,.2,1);
  border: 1px solid var(--border); position: relative; overflow: hidden;
}
.history-delete {
  position: absolute; top: 12px; right: 16px; z-index: 2;
  width: 28px; height: 28px; border-radius: 50%;
  border: none; background: transparent; color: var(--text-light);
  font-size: 22px; line-height: 1; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: all .2s ease;
}
.history-card:hover .history-delete { opacity: 1; }
.history-delete:hover { background: #fee2e2; color: #dc2626; }
.history-card::before {
  content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 3px;
  background: linear-gradient(180deg, var(--primary), var(--accent));
  transform: scaleY(0); transition: transform .3s ease;
}
.history-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); border-color: transparent; }
.history-card:hover::before { transform: scaleY(1); }
.history-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.history-header h4 { font-size: 18px; font-weight: 700; }
.severity-tag { padding: 4px 14px; border-radius: 50px; font-size: 12px; font-weight: 600; }
.severity-tag.low { background: #d1fae5; color: #065f46; }
.severity-tag.medium { background: #fef3c7; color: #92400e; }
.severity-tag.high { background: #fee2e2; color: #991b1b; }
.severity-tag.critical { background: #fce7f3; color: #9d174d; }
.history-summary { color: var(--text-secondary); font-size: 14px; line-height: 1.7; margin-bottom: 14px; }
.history-meta { display: flex; gap: 20px; font-size: 13px; color: var(--text-light); flex-wrap: wrap; }
.feedback-tag.pending { color: var(--text-light); }
.feedback-tag.accurate { color: var(--success); font-weight: 600; }
.feedback-tag.mostly_accurate { color: var(--primary); font-weight: 600; }
.feedback-tag.inaccurate { color: var(--danger); font-weight: 600; }

.dialog-body { display: flex; flex-direction: column; gap: 18px; }
.detail-row { display: flex; gap: 16px; }
.dl { width: 80px; flex-shrink: 0; font-weight: 600; font-size: 14px; color: var(--text-secondary); }
.detail-row span:last-child { font-size: 14px; line-height: 1.7; }
.warning { color: var(--danger); font-weight: 600; }

.loading-state { text-align: center; padding: 60px; color: var(--text-light); font-size: 15px; }
.pager { display: flex; justify-content: center; margin-top: 44px; }
.app-footer { text-align: center; padding: 36px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }
</style>
