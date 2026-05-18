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

        <!-- Messages -->
        <el-tab-pane label="在线问诊" name="messages">
          <div class="tab-header">
            <el-radio-group v-model="msgFilter" @change="fetchMessages">
              <el-radio-button label="UNRESOLVED">未解决 ({{ msgStats.unresolved }})</el-radio-button>
              <el-radio-button label="RESOLVED">已解决 ({{ msgStats.resolved }})</el-radio-button>
            </el-radio-group>
          </div>
          <div class="message-cards" v-if="msgList.length">
            <div class="msg-card card" v-for="m in msgList" :key="m.id" :class="{ resolved: m.status === 'RESOLVED' }">
              <div class="msg-header">
                <div>
                  <span class="msg-user">{{ m.user?.username }}</span>
                  <span class="msg-date">{{ formatDate(m.createdAt) }}</span>
                </div>
                <div class="msg-header-right">
                  <el-button
                    v-if="m.status === 'UNRESOLVED'"
                    size="small"
                    type="warning"
                    @click="markStatus(m.id, 'RESOLVED')"
                  >标记已解决</el-button>
                  <el-button
                    v-else
                    size="small"
                    type="info"
                    @click="markStatus(m.id, 'UNRESOLVED')"
                  >标记未解决</el-button>
                </div>
              </div>
              <div class="msg-content">{{ m.content }}</div>

              <!-- Existing reply (not editing) -->
              <div class="msg-reply-section" v-if="m.reply && editingReplyId !== m.id">
                <div class="reply-label">我的回复：</div>
                <div class="reply-text">{{ m.reply }}</div>
                <div class="reply-date" v-if="m.repliedAt">回复于 {{ formatDate(m.repliedAt) }}</div>
                <div class="reply-actions">
                  <el-button size="small" text type="primary" @click="startEditReply(m)">编辑回复</el-button>
                  <el-button size="small" text type="danger" @click="handleWithdrawReply(m.id)">撤回回复</el-button>
                </div>
              </div>

              <!-- Editing reply mode -->
              <div class="edit-reply-area" v-if="editingReplyId === m.id">
                <el-input v-model="editReplyContent" type="textarea" :rows="3" placeholder="编辑回复内容..." resize="none" />
                <div class="edit-reply-actions">
                  <el-button size="small" @click="cancelEditReply">取消</el-button>
                  <el-button size="small" type="primary" :disabled="!editReplyContent.trim()" :loading="replyingId === m.id" @click="handleEditReply(m.id)">保存修改</el-button>
                </div>
              </div>

              <!-- No reply yet: show reply input -->
              <div class="msg-action-bar" v-if="!m.reply">
                <div class="reply-input-wrap">
                  <el-input v-model="replyTexts[m.id]" type="textarea" :rows="2" placeholder="输入回复内容..." resize="none" />
                  <el-button type="primary" :disabled="!replyTexts[m.id]?.trim()" :loading="replyingId === m.id" @click="handleReply(m.id)">保存回复</el-button>
                </div>
                <el-button type="success" :icon="ChatDotRound" @click="goDiagnose(m)">AI辅助诊断</el-button>
              </div>

              <!-- Has reply: still show AI button -->
              <div class="msg-action-bar" v-if="m.reply && editingReplyId !== m.id">
                <el-button type="success" :icon="ChatDotRound" @click="goDiagnose(m)">AI辅助诊断</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无留言" :image-size="60" />
        </el-tab-pane>

        <!-- Disease Feedback -->
        <el-tab-pane label="疾病反馈" name="diseaseFeedback">
          <div class="tab-header">
            <el-input v-model="docDiseaseSearch" placeholder="搜索疾病..." clearable @keyup.enter="fetchDocDiseases" style="width: 300px;" />
            <el-button type="primary" @click="openDocDiseaseCreate">新增疾病建议</el-button>
          </div>
          <el-table :data="docDiseaseList" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="name" label="名称" width="180" />
            <el-table-column prop="classification" label="分类" width="120" />
            <el-table-column label="严重程度" width="90">
              <template #default="{ row }">
                <span class="severity-tag" :class="(row.severity || 'MODERATE').toLowerCase()">{{ sevMap[row.severity] || '中等' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'PENDING' ? 'warning' : 'danger'" size="small">{{ statusMap[row.status] || row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="openDocDiseaseEdit(row)">修改建议</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="tab-pager" v-if="docDiseaseTotal > 20">
            <el-pagination v-model:current-page="docDiseasePage" :page-size="20" :total="docDiseaseTotal" layout="prev, pager, next" @current-change="fetchDocDiseases" />
          </div>
        </el-tab-pane>

        <!-- Drug Feedback -->
        <el-tab-pane label="药品反馈" name="drugFeedback">
          <div class="tab-header">
            <el-input v-model="docDrugSearch" placeholder="搜索药品..." clearable @keyup.enter="fetchDocDrugs" style="width: 300px;" />
            <el-button type="primary" @click="openDocDrugCreate">新增药品建议</el-button>
          </div>
          <el-table :data="docDrugList" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="name" label="名称" width="180" />
            <el-table-column prop="genericName" label="通用名" width="140" />
            <el-table-column label="类型" width="90">
              <template #default="{ row }">
                <el-tag size="small">{{ drugTypeMap[row.drugType] || row.drugType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'PENDING' ? 'warning' : 'danger'" size="small">{{ statusMap[row.status] || row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="openDocDrugEdit(row)">修改建议</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="tab-pager" v-if="docDrugTotal > 20">
            <el-pagination v-model:current-page="docDrugPage" :page-size="20" :total="docDrugTotal" layout="prev, pager, next" @current-change="fetchDocDrugs" />
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- Disease Feedback Dialog -->
      <el-dialog v-model="docDiseaseDialogVisible" :title="docDiseaseDialogTitle" width="700px" top="3vh">
        <el-form :model="docDiseaseForm" label-width="90px" label-position="top">
          <el-row :gutter="16">
            <el-col :span="12"><el-form-item label="疾病名称"><el-input v-model="docDiseaseForm.name" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="别名"><el-input v-model="docDiseaseForm.alias" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="8"><el-form-item label="ICD编码"><el-input v-model="docDiseaseForm.icdCode" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="分类"><el-input v-model="docDiseaseForm.classification" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="身体系统"><el-input v-model="docDiseaseForm.bodySystem" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="8"><el-form-item label="严重程度"><el-select v-model="docDiseaseForm.severity" style="width:100%"><el-option label="轻度" value="MILD" /><el-option label="中度" value="MODERATE" /><el-option label="重度" value="SEVERE" /></el-select></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="传染性"><el-switch v-model="docDiseaseForm.isInfectious" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="慢性病"><el-switch v-model="docDiseaseForm.isChronic" /></el-form-item></el-col>
          </el-row>
          <el-form-item label="病因"><el-input v-model="docDiseaseForm.cause" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="症状"><el-input v-model="docDiseaseForm.symptoms" type="textarea" :rows="3" /></el-form-item>
          <el-form-item label="诊断方法"><el-input v-model="docDiseaseForm.diagnosis" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="治疗方案"><el-input v-model="docDiseaseForm.treatment" type="textarea" :rows="3" /></el-form-item>
          <el-form-item label="预防措施"><el-input v-model="docDiseaseForm.prevention" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="并发症"><el-input v-model="docDiseaseForm.complications" type="textarea" :rows="2" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="docDiseaseDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="docDiseaseSaving" @click="saveDocDisease">提交反馈</el-button>
        </template>
      </el-dialog>

      <!-- Drug Feedback Dialog -->
      <el-dialog v-model="docDrugDialogVisible" :title="docDrugDialogTitle" width="700px" top="3vh">
        <el-form :model="docDrugForm" label-width="90px" label-position="top">
          <el-row :gutter="16">
            <el-col :span="8"><el-form-item label="药品名称"><el-input v-model="docDrugForm.name" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="通用名"><el-input v-model="docDrugForm.genericName" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="品牌名"><el-input v-model="docDrugForm.brandName" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="8"><el-form-item label="类型"><el-select v-model="docDrugForm.drugType" style="width:100%"><el-option label="处方药" value="PRESCRIPTION" /><el-option label="非处方药" value="OTC" /><el-option label="草药" value="HERBAL" /><el-option label="生物制品" value="BIOLOGIC" /></el-select></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="剂型"><el-input v-model="docDrugForm.form" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="规格"><el-input v-model="docDrugForm.specification" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12"><el-form-item label="生产厂家"><el-input v-model="docDrugForm.manufacturer" /></el-form-item></el-col>
            <el-col :span="6"><el-form-item label="批准文号"><el-input v-model="docDrugForm.approvalNo" /></el-form-item></el-col>
            <el-col :span="6"><el-form-item label="价格"><el-input-number v-model="docDrugForm.price" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          </el-row>
          <el-form-item label="功效"><el-input v-model="docDrugForm.efficacy" type="textarea" :rows="3" /></el-form-item>
          <el-form-item label="用法用量"><el-input v-model="docDrugForm.usage2" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="剂量"><el-input v-model="docDrugForm.dosage" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="副作用"><el-input v-model="docDrugForm.sideEffect" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="禁忌"><el-input v-model="docDrugForm.contraindication" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="储存条件"><el-input v-model="docDrugForm.storage" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="docDrugDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="docDrugSaving" @click="saveDocDrug">提交反馈</el-button>
        </template>
      </el-dialog>
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
import { useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import api from '@/api/index'
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import { truncateText, formatDate, severityMap } from '@/utils'

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

const sevMap: Record<string, string> = { MILD: '轻度', MODERATE: '中度', SEVERE: '重度' }
const statusMap: Record<string, string> = { APPROVED: '已审核', PENDING: '待审核', REJECTED: '已拒绝' }
const drugTypeMap: Record<string, string> = { PRESCRIPTION: '处方药', OTC: '非处方药', HERBAL: '草药', BIOLOGIC: '生物制品' }

async function fetchStats() {
  try { const res = await api.get('/doctor/stats'); Object.assign(stats, res.data.data) } catch { ElMessage.error('加载统计数据失败') }
}
async function fetchPatients() {
  try {
    const res = await api.get('/doctor/patients', { params: { page: patientPage.value } })
    patients.value = res.data.data.list || []
    patientTotal.value = res.data.data.pagination?.total || 0
  } catch { ElMessage.error('加载患者列表失败') }
}
async function fetchDiagnoses() {
  try {
    const res = await api.get('/doctor/diagnoses', { params: { page: diagPage.value } })
    diagnoses.value = res.data.data.list || []
    diagTotal.value = res.data.data.pagination?.total || 0
  } catch { ElMessage.error('加载诊断列表失败') }
}
function viewHealthRecord(row: any) {
  router.push({ path: '/health/record', query: { userId: row.id } })
}
function viewDiagnoses(row: any) {
  activeTab.value = 'diagnoses'
  fetchDiagnoses()
}
function showDiagnosisDetail(row: any) {
  selectedDiag.value = row
  detailVisible.value = true
}

// Messages
const msgFilter = ref('UNRESOLVED')
const msgList = ref<any[]>([])
const msgStats = reactive({ unresolved: 0, resolved: 0 })
const router = useRouter()
const replyingId = ref<number | null>(null)
const replyTexts = ref<Record<number, string>>({})
const editingReplyId = ref<number | null>(null)
const editReplyContent = ref('')

// Disease Feedback
const docDiseaseSearch = ref('')
const docDiseaseList = ref<any[]>([])
const docDiseasePage = ref(1)
const docDiseaseTotal = ref(0)
const docDiseaseDialogVisible = ref(false)
const docDiseaseDialogMode = ref<'create' | 'edit'>('create')
const docDiseaseDialogTitle = ref('新增疾病建议')
const docDiseaseSaving = ref(false)
const docDiseaseForm = reactive({
  id: null as number | null, name: '', alias: '', icdCode: '', classification: '', bodySystem: '',
  severity: 'MODERATE', isInfectious: false, isChronic: false,
  cause: '', symptoms: '', diagnosis: '', treatment: '', prevention: '', complications: ''
})

// Drug Feedback
const docDrugSearch = ref('')
const docDrugList = ref<any[]>([])
const docDrugPage = ref(1)
const docDrugTotal = ref(0)
const docDrugDialogVisible = ref(false)
const docDrugDialogMode = ref<'create' | 'edit'>('create')
const docDrugDialogTitle = ref('新增药品建议')
const docDrugSaving = ref(false)
const docDrugForm = reactive({
  id: null as number | null, name: '', genericName: '', brandName: '', drugType: 'PRESCRIPTION',
  form: '', specification: '', manufacturer: '', approvalNo: '', price: undefined as number | undefined,
  efficacy: '', usage2: '', dosage: '', sideEffect: '', contraindication: '', storage: ''
})

function resetDocDiseaseForm() {
  Object.assign(docDiseaseForm, {
    id: null, name: '', alias: '', icdCode: '', classification: '', bodySystem: '',
    severity: 'MODERATE', isInfectious: false, isChronic: false,
    cause: '', symptoms: '', diagnosis: '', treatment: '', prevention: '', complications: ''
  })
}
function resetDocDrugForm() {
  Object.assign(docDrugForm, {
    id: null, name: '', genericName: '', brandName: '', drugType: 'PRESCRIPTION',
    form: '', specification: '', manufacturer: '', approvalNo: '', price: undefined,
    efficacy: '', usage2: '', dosage: '', sideEffect: '', contraindication: '', storage: ''
  })
}

async function fetchDocDiseases() {
  try {
    const params: any = { page: docDiseasePage.value }
    if (docDiseaseSearch.value) params.keyword = docDiseaseSearch.value
    const res = await api.get('/doctor/diseases', { params })
    docDiseaseList.value = res.data.data.list || []
    docDiseaseTotal.value = res.data.data.pagination?.total || 0
  } catch {
    ElMessage.error('操作失败')
  }
}
function openDocDiseaseCreate() {
  resetDocDiseaseForm()
  docDiseaseDialogMode.value = 'create'
  docDiseaseDialogTitle.value = '新增疾病建议'
  docDiseaseDialogVisible.value = true
}
function openDocDiseaseEdit(row: any) {
  docDiseaseDialogMode.value = 'edit'
  docDiseaseDialogTitle.value = '修改疾病建议'
  Object.assign(docDiseaseForm, {
    id: row.id, name: row.name || '', alias: row.alias || '', icdCode: row.icdCode || '',
    classification: row.classification || '', bodySystem: row.bodySystem || '',
    severity: row.severity || 'MODERATE', isInfectious: row.isInfectious || false,
    isChronic: row.isChronic || false, cause: row.cause || '', symptoms: row.symptoms || '',
    diagnosis: row.diagnosis || '', treatment: row.treatment || '', prevention: row.prevention || '',
    complications: row.complications || ''
  })
  docDiseaseDialogVisible.value = true
}
async function saveDocDisease() {
  docDiseaseSaving.value = true
  try {
    if (docDiseaseDialogMode.value === 'create') {
      await api.post('/doctor/diseases', docDiseaseForm)
      ElMessage.success('疾病建议已提交，待管理员审核')
    } else {
      await api.put(`/doctor/diseases/${docDiseaseForm.id}`, docDiseaseForm)
      ElMessage.success('修改建议已提交，待管理员审核')
    }
    docDiseaseDialogVisible.value = false
    fetchDocDiseases()
  } catch {
    ElMessage.error('提交失败，请重试')
  }
  finally { docDiseaseSaving.value = false }
}

async function fetchDocDrugs() {
  try {
    const params: any = { page: docDrugPage.value }
    if (docDrugSearch.value) params.keyword = docDrugSearch.value
    const res = await api.get('/doctor/drugs', { params })
    docDrugList.value = res.data.data.list || []
    docDrugTotal.value = res.data.data.pagination?.total || 0
  } catch {
    ElMessage.error('操作失败')
  }
}
function openDocDrugCreate() {
  resetDocDrugForm()
  docDrugDialogMode.value = 'create'
  docDrugDialogTitle.value = '新增药品建议'
  docDrugDialogVisible.value = true
}
function openDocDrugEdit(row: any) {
  docDrugDialogMode.value = 'edit'
  docDrugDialogTitle.value = '修改药品建议'
  Object.assign(docDrugForm, {
    id: row.id, name: row.name || '', genericName: row.genericName || '', brandName: row.brandName || '',
    drugType: row.drugType || 'PRESCRIPTION', form: row.form || '', specification: row.specification || '',
    manufacturer: row.manufacturer || '', approvalNo: row.approvalNo || '', price: row.price,
    efficacy: row.efficacy || '', usage2: row.usage2 || '', dosage: row.dosage || '',
    sideEffect: row.sideEffect || '', contraindication: row.contraindication || '', storage: row.storage || ''
  })
  docDrugDialogVisible.value = true
}
async function saveDocDrug() {
  docDrugSaving.value = true
  try {
    if (docDrugDialogMode.value === 'create') {
      await api.post('/doctor/drugs', docDrugForm)
      ElMessage.success('药品建议已提交，待管理员审核')
    } else {
      await api.put(`/doctor/drugs/${docDrugForm.id}`, docDrugForm)
      ElMessage.success('修改建议已提交，待管理员审核')
    }
    docDrugDialogVisible.value = false
    fetchDocDrugs()
  } catch {
    ElMessage.error('提交失败，请重试')
  }
  finally { docDrugSaving.value = false }
}

async function fetchMsgStats() {
  try { const res = await api.get('/messages/stats'); Object.assign(msgStats, res.data.data) } catch { /* */ }
}
async function fetchMessages() {
  try {
    const res = await api.get('/messages', { params: { status: msgFilter.value } })
    msgList.value = res.data.data.list || []
  } catch {
    ElMessage.error('操作失败')
  }
}
async function handleReply(id: number) {
  const reply = replyTexts.value[id]?.trim()
  if (!reply) return
  replyingId.value = id
  try {
    await api.put(`/messages/${id}/reply`, { reply })
    ElMessage.success('已回复')
    replyTexts.value[id] = ''
    fetchMessages()
    fetchMsgStats()
  } catch {
    ElMessage.error('回复失败，请重试')
  }
  finally { replyingId.value = null }
}
function startEditReply(m: any) {
  editingReplyId.value = m.id
  editReplyContent.value = m.reply
}
function cancelEditReply() {
  editingReplyId.value = null
  editReplyContent.value = ''
}
async function handleEditReply(id: number) {
  const reply = editReplyContent.value.trim()
  if (!reply) return
  replyingId.value = id
  try {
    await api.put(`/messages/${id}/reply`, { reply })
    ElMessage.success('回复已更新')
    cancelEditReply()
    fetchMessages()
    fetchMsgStats()
  } catch {
    ElMessage.error('编辑回复失败')
  }
  finally { replyingId.value = null }
}
async function handleWithdrawReply(id: number) {
  try {
    await api.delete(`/messages/${id}/reply`)
    ElMessage.success('回复已撤回')
    fetchMessages()
    fetchMsgStats()
  } catch {
    ElMessage.error('操作失败')
  }
}
async function markStatus(id: number, status: string) {
  try {
    await api.put(`/messages/${id}/status`)
    ElMessage.success(status === 'RESOLVED' ? '已标记为已解决' : '已标记为未解决')
    fetchMessages()
    fetchMsgStats()
  } catch {
    ElMessage.error('操作失败')
  }
}
function goDiagnose(m: any) {
  router.push({ path: '/ai-chat', query: { symptom: m.content } })
}

onMounted(() => { fetchStats(); fetchPatients(); fetchDiagnoses(); fetchMsgStats(); fetchMessages(); fetchDocDiseases(); fetchDocDrugs() })
</script>

<style scoped>
.doctor-page { min-height: 100vh; display: flex; flex-direction: column; }
.page-hero { text-align: center; margin-bottom: 44px; }
.page-hero h1 { font-size: 38px; font-weight: 800; margin-bottom: 12px; letter-spacing: -0.5px; }
.page-hero p { color: var(--text-secondary); font-size: 16px; }

.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 36px; }
.stat-card { display: flex; align-items: center; gap: 18px; padding: 24px 28px; border: 1px solid var(--border); transition: all .25s ease; }
.stat-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.stat-icon { font-size: 38px; }
.stat-num { font-size: 30px; font-weight: 800; }
.stat-label { font-size: 13px; color: var(--text-light); }

.tab-pager { display: flex; justify-content: center; margin-top: 24px; }

.severity-tag { padding: 3px 12px; border-radius: 50px; font-size: 12px; font-weight: 600; }
.severity-tag.low { background: #d1fae5; color: #065f46; }
.severity-tag.medium { background: #fef3c7; color: #92400e; }
.severity-tag.high { background: #fee2e2; color: #991b1b; }
.severity-tag.critical { background: #fce7f3; color: #9d174d; }

.dialog-body { display: flex; flex-direction: column; gap: 18px; }
.detail-row { display: flex; gap: 16px; }
.dl { width: 80px; flex-shrink: 0; font-weight: 600; font-size: 14px; color: var(--text-secondary); }
.detail-row span:last-child { font-size: 14px; line-height: 1.7; }
.warning { color: var(--danger); font-weight: 600; }

.tab-header { margin-bottom: 20px; }

.message-cards { display: flex; flex-direction: column; gap: 16px; }
.msg-card { padding: 24px 28px; border: 1px solid var(--border); }
.msg-card.resolved { background: #f9fafb; opacity: 0.85; }
.msg-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.msg-header > div { display: flex; align-items: center; gap: 12px; }
.msg-user { font-weight: 700; font-size: 15px; }
.msg-date { font-size: 13px; color: var(--text-light); }
.msg-status-tag { padding: 3px 12px; border-radius: 50px; font-size: 12px; font-weight: 600; }
.msg-status-tag.unresolved { background: #fef3c7; color: #92400e; }
.msg-status-tag.resolved { background: #d1fae5; color: #065f46; }
.msg-content { font-size: 15px; line-height: 1.7; color: var(--text); white-space: pre-wrap; margin-bottom: 16px; }
.msg-reply-section { padding: 16px 20px; background: #f0fdf4; border-radius: 12px; border: 1px solid #bbf7d0; margin-bottom: 16px; }
.reply-label { font-size: 13px; font-weight: 700; color: #065f46; margin-bottom: 6px; }
.reply-text { font-size: 15px; line-height: 1.7; color: var(--text); white-space: pre-wrap; }
.msg-header-right { display: flex; align-items: center; gap: 8px; }
.reply-input-wrap { display: flex; gap: 12px; align-items: flex-start; flex: 1; }
.reply-input-wrap :deep(.el-textarea) { flex: 1; }
.msg-action-bar { display: flex; gap: 12px; align-items: flex-start; margin-top: 8px; }
.reply-date { font-size: 12px; color: var(--text-light); margin-top: 4px; }
.reply-actions { display: flex; gap: 8px; margin-top: 10px; padding-top: 10px; border-top: 1px solid #d1fae5; }
.edit-reply-area { margin-top: 12px; }
.edit-reply-area :deep(.el-textarea__inner) { border-radius: 12px; font-size: 14px; }
.edit-reply-actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 10px; }

.app-footer { text-align: center; padding: 36px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }
</style>
