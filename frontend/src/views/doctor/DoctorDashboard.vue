<template>
  <div class="doctor-page">
    <AppHeader />
    <div class="page-container section-padding">
      <div class="page-hero">
        <h1>医生<span class="gradient-text">工作台</span></h1>
        <p>患者管理与诊断记录查看</p>
      </div>

      <!-- Stats -->
      <div class="stats-grid">
        <div class="stat-card card">
          <span class="stat-icon">👥</span>
          <div>
            <div class="stat-num">{{ stats.totalPatients }}</div>
            <div class="stat-label">患者数</div>
          </div>
        </div>
        <div class="stat-card card">
          <span class="stat-icon">📋</span>
          <div>
            <div class="stat-num">{{ stats.totalDiagnoses }}</div>
            <div class="stat-label">诊断数</div>
          </div>
        </div>
        <div class="stat-card card">
          <span class="stat-icon">⏳</span>
          <div>
            <div class="stat-num">{{ stats.pendingReviews }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </div>
      </div>

      <el-tabs v-model="activeTab" type="border-card">
        <!-- Patients -->
        <el-tab-pane label="患者列表" name="patients">
          <el-table :data="patients" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="realName" label="真实姓名" width="100">
              <template #default="{ row }">{{ row.realName || '-' }}</template>
            </el-table-column>
            <el-table-column prop="email" label="邮箱" width="200" />
            <el-table-column label="最后登录" width="160">
              <template #default="{ row }">{{ formatDate(row.lastLoginAt) }}</template>
            </el-table-column>
            <el-table-column label="注册时间" width="120">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="{ row }">
                <el-button size="small" @click="viewHealthRecord(row)">健康档案</el-button>
                <el-button size="small" type="primary" @click="viewDiagnoses(row)">诊断记录</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="tab-pager" v-if="patientTotal > 20">
            <el-pagination v-model:current-page="patientPage" :page-size="20" :total="patientTotal" layout="prev, pager, next" @current-change="fetchPatients" />
          </div>
        </el-tab-pane>

        <!-- Diagnoses -->
        <el-tab-pane label="诊断记录" name="diagnoses">
          <el-table :data="diagnoses" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column label="患者" width="100">
              <template #default="{ row }">{{ row.user?.username || '-' }}</template>
            </el-table-column>
            <el-table-column label="症状摘要" min-width="200">
              <template #default="{ row }">{{ truncateText(row.symptomSummary, 60) }}</template>
            </el-table-column>
            <el-table-column label="严重程度" width="100">
              <template #default="{ row }">
                <span class="severity-tag" :class="(row.severityLevel || 'LOW').toLowerCase()">{{ severityMap[row.severityLevel] || '未知' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="需就医" width="80">
              <template #default="{ row }">
                <el-tag :type="row.needsHospital ? 'danger' : 'success'" size="small">{{ row.needsHospital ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="消息数" width="80">
              <template #default="{ row }">{{ row.messageCount }}</template>
            </el-table-column>
            <el-table-column label="时间" width="160">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button size="small" @click="showDiagnosisDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="tab-pager" v-if="diagTotal > 20">
            <el-pagination v-model:current-page="diagPage" :page-size="20" :total="diagTotal" layout="prev, pager, next" @current-change="fetchDiagnoses" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- Diagnosis Detail Dialog -->
    <el-dialog v-model="detailVisible" title="诊断详情" width="700px" top="5vh">
      <div v-if="selectedDiag" class="dialog-body">
        <div class="detail-row"><span class="dl">症状摘要</span><span>{{ selectedDiag.symptomSummary }}</span></div>
        <div class="detail-row" v-if="selectedDiag.symptomsDetail"><span class="dl">症状详情</span><span>{{ selectedDiag.symptomsDetail }}</span></div>
        <div class="detail-row" v-if="selectedDiag.aiAnalysis"><span class="dl">AI分析</span><span>{{ selectedDiag.aiAnalysis }}</span></div>
        <div class="detail-row" v-if="selectedDiag.suggestedDiseases"><span class="dl">疑似疾病</span><span>{{ selectedDiag.suggestedDiseases }}</span></div>
        <div class="detail-row" v-if="selectedDiag.suggestedDrugs"><span class="dl">建议用药</span><span>{{ selectedDiag.suggestedDrugs }}</span></div>
        <div class="detail-row" v-if="selectedDiag.advice"><span class="dl">健康建议</span><span>{{ selectedDiag.advice }}</span></div>
        <div class="detail-row" v-if="selectedDiag.warningText"><span class="dl">警告</span><span class="warning">{{ selectedDiag.warningText }}</span></div>
      </div>
    </el-dialog>

    <footer class="app-footer"><p>&copy; 2026 VitaAI 智慧医院系统. All rights reserved.</p></footer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import api from '@/api/index'

const activeTab = ref('patients')
const stats = reactive({ totalPatients: 0, totalDiagnoses: 0, pendingReviews: 0 })

const patients = ref<any[]>([])
const patientPage = ref(1)
const patientTotal = ref(0)

const diagnoses = ref<any[]>([])
const diagPage = ref(1)
const diagTotal = ref(0)

const detailVisible = ref(false)
const selectedDiag = ref<any>(null)

const severityMap: Record<string, string> = { LOW: '低风险', MEDIUM: '中等风险', HIGH: '高风险', CRITICAL: '危险' }

function truncateText(text: string, len: number) {
  return text && text.length > len ? text.substring(0, len) + '...' : text || ''
}
function formatDate(date: string) {
  return date ? new Date(date).toLocaleString('zh-CN') : '-'
}

async function fetchStats() {
  try { const res = await api.get('/doctor/stats'); Object.assign(stats, res.data.data) } catch { /* */ }
}
async function fetchPatients() {
  try {
    const res = await api.get('/doctor/patients', { params: { page: patientPage.value } })
    patients.value = res.data.data.list || []
    patientTotal.value = res.data.data.pagination?.total || 0
  } catch { /* */ }
}
async function fetchDiagnoses() {
  try {
    const res = await api.get('/doctor/diagnoses', { params: { page: diagPage.value } })
    diagnoses.value = res.data.data.list || []
    diagTotal.value = res.data.data.pagination?.total || 0
  } catch { /* */ }
}
function viewHealthRecord(row: any) {
  // In a real app, navigate to patient health record
}
function viewDiagnoses(row: any) {
  // Filter diagnoses by user
}
function showDiagnosisDetail(row: any) {
  selectedDiag.value = row
  detailVisible.value = true
}

onMounted(() => { fetchStats(); fetchPatients(); fetchDiagnoses() })
</script>

<style scoped>
.doctor-page { min-height: 100vh; display: flex; flex-direction: column; }
.page-hero { text-align: center; margin-bottom: 40px; }
.page-hero h1 { font-size: 36px; font-weight: 800; margin-bottom: 12px; }
.page-hero p { color: var(--text-secondary); font-size: 16px; }

.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 32px; }
.stat-card { display: flex; align-items: center; gap: 16px; padding: 20px; }
.stat-icon { font-size: 32px; }
.stat-num { font-size: 28px; font-weight: 800; }
.stat-label { font-size: 13px; color: var(--text-light); }

.tab-pager { display: flex; justify-content: center; margin-top: 20px; }

.severity-tag { padding: 2px 10px; border-radius: 50px; font-size: 12px; font-weight: 600; }
.severity-tag.low { background: #d1fae5; color: #065f46; }
.severity-tag.medium { background: #fef3c7; color: #92400e; }
.severity-tag.high { background: #fee2e2; color: #991b1b; }
.severity-tag.critical { background: #fce7f3; color: #9d174d; }

.dialog-body { display: flex; flex-direction: column; gap: 16px; }
.detail-row { display: flex; gap: 16px; }
.dl { width: 80px; flex-shrink: 0; font-weight: 600; font-size: 14px; color: var(--text-secondary); }
.detail-row span:last-child { font-size: 14px; line-height: 1.6; }
.warning { color: var(--danger); font-weight: 600; }

.app-footer { text-align: center; padding: 32px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }
</style>
