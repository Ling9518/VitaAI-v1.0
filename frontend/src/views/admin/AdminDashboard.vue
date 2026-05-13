<template>
  <div class="admin-page">
    <AppHeader />
    <div class="page-container section-padding">
      <div class="page-hero">
        <h1>管理<span class="gradient-text">后台</span></h1>
        <p>系统管理、用户管理和内容审核</p>
      </div>

      <!-- Stats cards -->
      <div class="stats-grid">
        <div class="stat-card card">
          <span class="stat-icon">👥</span>
          <div>
            <div class="stat-num">{{ stats.totalUsers }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </div>
        <div class="stat-card card">
          <span class="stat-icon">🩺</span>
          <div>
            <div class="stat-num">{{ stats.totalDoctors }}</div>
            <div class="stat-label">医生数</div>
          </div>
        </div>
        <div class="stat-card card">
          <span class="stat-icon">🦠</span>
          <div>
            <div class="stat-num">{{ stats.totalDiseases }}</div>
            <div class="stat-label">疾病数</div>
          </div>
        </div>
        <div class="stat-card card">
          <span class="stat-icon">💊</span>
          <div>
            <div class="stat-num">{{ stats.totalDrugs }}</div>
            <div class="stat-label">药品数</div>
          </div>
        </div>
        <div class="stat-card card">
          <span class="stat-icon">📋</span>
          <div>
            <div class="stat-num">{{ stats.totalDiagnoses }}</div>
            <div class="stat-label">诊断数</div>
          </div>
        </div>
        <div class="stat-card card highlight">
          <span class="stat-icon">⏳</span>
          <div>
            <div class="stat-num">{{ stats.pendingReviews }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <el-tabs v-model="activeTab" type="border-card">
        <!-- User Management -->
        <el-tab-pane label="用户管理" name="users">
          <div class="tab-header">
            <el-select v-model="userRoleFilter" placeholder="角色筛选" clearable @change="fetchUsers" style="width: 160px;">
              <el-option label="全部用户" value="" />
              <el-option label="管理员" value="ADMIN" />
              <el-option label="医生" value="DOCTOR" />
              <el-option label="用户" value="USER" />
            </el-select>
          </div>
          <el-table :data="users" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="email" label="邮箱" width="200" />
            <el-table-column prop="realName" label="真实姓名" width="100" />
            <el-table-column label="角色" width="90">
              <template #default="{ row }">
                <el-tag :type="roleType(row.role)" size="small">{{ roleMap[row.role] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.isDisabled ? 'danger' : 'success'" size="small">{{ row.isDisabled ? '已禁用' : '正常' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="最后登录" width="160">
              <template #default="{ row }">{{ formatDate(row.lastLoginAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" @click="toggleUser(row)">{{ row.isDisabled ? '启用' : '禁用' }}</el-button>
                <el-button size="small" v-if="row.role !== 'ADMIN'" @click="changeRole(row)">改角色</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="tab-pager" v-if="userTotal > 20">
            <el-pagination v-model:current-page="userPage" :page-size="20" :total="userTotal" layout="prev, pager, next" @current-change="fetchUsers" />
          </div>
        </el-tab-pane>

        <!-- Content Review -->
        <el-tab-pane label="内容审核" name="content">
          <div class="review-section">
            <h3>待审核疾病</h3>
            <el-table :data="pendingDiseases" stripe style="width: 100%">
              <el-table-column prop="name" label="名称" width="160" />
              <el-table-column prop="classification" label="分类" width="120" />
              <el-table-column label="症状" min-width="200">
                <template #default="{ row }">{{ truncateText(row.symptoms, 60) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button size="small" type="success" @click="reviewDisease(row, 'APPROVED')">通过</el-button>
                  <el-button size="small" type="danger" @click="reviewDisease(row, 'REJECTED')">拒绝</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!pendingDiseases.length" description="无待审核疾病" :image-size="40" />
          </div>
          <div class="review-section">
            <h3>待审核药品</h3>
            <el-table :data="pendingDrugs" stripe style="width: 100%">
              <el-table-column prop="name" label="名称" width="160" />
              <el-table-column prop="drugType" label="类型" width="100" />
              <el-table-column label="功效" min-width="200">
                <template #default="{ row }">{{ truncateText(row.efficacy, 60) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button size="small" type="success" @click="reviewDrug(row, 'APPROVED')">通过</el-button>
                  <el-button size="small" type="danger" @click="reviewDrug(row, 'REJECTED')">拒绝</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!pendingDrugs.length" description="无待审核药品" :image-size="40" />
          </div>
        </el-tab-pane>

        <!-- Audit Logs -->
        <el-tab-pane label="审计日志" name="audit">
          <el-table :data="auditLogs" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="action" label="操作" width="140" />
            <el-table-column prop="entityType" label="实体类型" width="120" />
            <el-table-column prop="entityId" label="实体ID" width="80" />
            <el-table-column prop="detail" label="详情" min-width="200">
              <template #default="{ row }">{{ truncateText(row.detail, 80) }}</template>
            </el-table-column>
            <el-table-column label="时间" width="160">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
    <footer class="app-footer"><p>&copy; 2026 VitaAI 智慧医院系统. All rights reserved.</p></footer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import api from '@/api/index'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('users')

const stats = reactive({ totalUsers: 0, totalDoctors: 0, totalDiseases: 0, totalDrugs: 0, totalDiagnoses: 0, pendingReviews: 0 })
const roleMap: Record<string, string> = { ADMIN: '管理员', DOCTOR: '医生', USER: '用户', VISITOR: '访客' }

// Users
const users = ref<any[]>([])
const userPage = ref(1)
const userTotal = ref(0)
const userRoleFilter = ref('')

// Content
const pendingDiseases = ref<any[]>([])
const pendingDrugs = ref<any[]>([])

// Audit
const auditLogs = ref<any[]>([])

function truncateText(text: string, len: number) {
  return text && text.length > len ? text.substring(0, len) + '...' : text || ''
}
function formatDate(date: string) {
  return date ? new Date(date).toLocaleString('zh-CN') : '-'
}
function roleType(role: string) {
  return role === 'ADMIN' ? 'danger' : role === 'DOCTOR' ? 'warning' : 'info'
}

async function fetchStats() {
  try { const res = await api.get('/admin/stats'); Object.assign(stats, res.data.data) } catch { /* */ }
}
async function fetchUsers() {
  try {
    const res = await api.get('/admin/users', { params: { page: userPage.value, role: userRoleFilter.value || undefined } })
    users.value = res.data.data.list || []
    userTotal.value = res.data.data.pagination?.total || 0
  } catch { /* */ }
}
async function fetchPending() {
  try {
    const res = await api.get('/admin/content/pending')
    pendingDiseases.value = res.data.data.diseases?.list || []
    pendingDrugs.value = res.data.data.drugs?.list || []
  } catch { /* */ }
}
async function fetchAuditLogs() {
  try {
    const res = await api.get('/admin/audit-logs')
    auditLogs.value = res.data.data.list || []
  } catch { /* */ }
}

async function toggleUser(row: any) {
  try {
    await api.put(`/admin/users/${row.id}`, { isDisabled: !row.isDisabled })
    ElMessage.success(row.isDisabled ? '已启用' : '已禁用')
    fetchUsers()
  } catch { /* */ }
}
async function changeRole(row: any) {
  try {
    const { value } = await ElMessageBox.prompt('输入新角色 (USER/DOCTOR)', '修改角色', { inputValue: row.role })
    if (value) {
      await api.put(`/admin/users/${row.id}`, { role: value.toUpperCase() })
      ElMessage.success('角色已更新')
      fetchUsers()
    }
  } catch { /* cancelled */ }
}
async function reviewDisease(row: any, action: string) {
  try {
    await api.put(`/admin/content/diseases/${row.id}/review`, { action })
    ElMessage.success(action === 'APPROVED' ? '已通过' : '已拒绝')
    fetchPending()
    fetchStats()
  } catch { /* */ }
}
async function reviewDrug(row: any, action: string) {
  try {
    await api.put(`/admin/content/drugs/${row.id}/review`, { action })
    ElMessage.success(action === 'APPROVED' ? '已通过' : '已拒绝')
    fetchPending()
    fetchStats()
  } catch { /* */ }
}

onMounted(() => { fetchStats(); fetchUsers(); fetchPending(); fetchAuditLogs() })
</script>

<style scoped>
.admin-page { min-height: 100vh; display: flex; flex-direction: column; }
.page-hero { text-align: center; margin-bottom: 40px; }
.page-hero h1 { font-size: 36px; font-weight: 800; margin-bottom: 12px; }
.page-hero p { color: var(--text-secondary); font-size: 16px; }

.stats-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; margin-bottom: 32px; }
.stat-card { display: flex; align-items: center; gap: 16px; padding: 20px; }
.stat-card.highlight { border: 2px solid var(--warning); }
.stat-icon { font-size: 32px; }
.stat-num { font-size: 28px; font-weight: 800; }
.stat-label { font-size: 13px; color: var(--text-light); }

.tab-header { margin-bottom: 16px; display: flex; gap: 12px; }
.tab-pager { display: flex; justify-content: center; margin-top: 20px; }
.review-section { margin-bottom: 32px; }
.review-section h3 { font-size: 18px; font-weight: 700; margin-bottom: 16px; }
.app-footer { text-align: center; padding: 32px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
