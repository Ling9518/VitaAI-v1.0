<template>
  <div class="list-page">
    <AppHeader />
    <div class="page-container section-padding">
      <div class="page-hero">
        <h1>疾病<span class="gradient-text">知识库</span></h1>
        <p>了解疾病症状、诊断方法和治疗方案</p>
      </div>

      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索疾病名称、症状..." size="large" clearable @input="search">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="classification" placeholder="疾病分类" size="large" clearable @change="search" style="width: 200px;">
          <el-option v-for="c in classifications" :key="c.code" :label="c.name" :value="c.code" />
        </el-select>
      </div>

      <div class="disease-grid" v-if="diseases.length">
        <div class="disease-card card" v-for="d in diseases" :key="d.id" @click="$router.push(`/diseases/${d.id}`)">
          <div class="disease-header">
            <h3>{{ d.name }}</h3>
            <span class="severity-tag" :class="d.severity?.toLowerCase()">{{ d.severity ? (severityMap[d.severity] || d.severity) : '' }}</span>
          </div>
          <p class="disease-symptoms">{{ truncateText(d.symptoms ?? null, 100) }}</p>
          <div class="disease-meta">
            <span class="tag">{{ d.classification }}</span>
            <span class="tag">{{ d.bodySystem }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无疾病数据" />

      <div class="pager" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchData"
        />
      </div>
    </div>
    <footer class="app-footer"><p>&copy; 2026 VitaAI 智慧医院系统. All rights reserved.</p></footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import api from '@/api/index'
import { ElMessage } from 'element-plus'
import type { Disease, DictItem } from '@/types'
import { truncateText } from '@/utils'

const severityMap: Record<string, string> = { MILD: '轻度', MODERATE: '中度', SEVERE: '重度' }

const diseases = ref<Disease[]>([])
const classifications = ref<DictItem[]>([])
const loading = ref(false)
const keyword = ref('')
const classification = ref('')
const page = ref(1)
const pageSize = 20
const total = ref(0)

async function fetchData() {
  loading.value = true
  try {
    const res = await api.get('/diseases', { params: { page: page.value, pageSize, keyword: keyword.value || undefined, classification: classification.value || undefined } })
    const data = res.data.data
    diseases.value = data.list || []
    total.value = data.pagination?.total || 0
  } catch {
    ElMessage.error('加载疾病列表失败')
  } finally {
    loading.value = false
  }
}

let searchTimer: ReturnType<typeof setTimeout> | null = null
function search() {
  if (searchTimer) clearTimeout(searchTimer)
  page.value = 1
  searchTimer = setTimeout(() => fetchData(), 300)
}

onMounted(async () => {
  fetchData()
  try {
    const res = await api.get('/system/dicts/disease_classification')
    classifications.value = res.data.data || []
  } catch { /* empty */ }
})
</script>

<style scoped>
.list-page { min-height: 100vh; display: flex; flex-direction: column; }
.page-hero { text-align: center; margin-bottom: 44px; }
.page-hero h1 { font-size: 38px; font-weight: 800; margin-bottom: 12px; letter-spacing: -0.5px; }
.page-hero p { color: var(--text-secondary); font-size: 16px; }

.search-bar { display: flex; gap: 16px; margin-bottom: 36px; }
.search-bar :deep(.el-input__wrapper) { border-radius: 14px !important; padding: 6px 16px; }

.disease-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.disease-card {
  cursor: pointer; padding: 28px; transition: all .35s cubic-bezier(.4,0,.2,1);
  border: 1px solid var(--border); position: relative; overflow: hidden;
}
.disease-card::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 3px;
  background: linear-gradient(90deg, var(--primary), var(--accent));
  transform: scaleX(0); transform-origin: left; transition: transform .35s ease;
}
.disease-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-lg); border-color: transparent; }
.disease-card:hover::before { transform: scaleX(1); }
.disease-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.disease-header h3 { font-size: 18px; font-weight: 700; }
.severity-tag { padding: 3px 12px; border-radius: 50px; font-size: 12px; font-weight: 600; }
.severity-tag.mild { background: #d1fae5; color: #065f46; }
.severity-tag.moderate { background: #fef3c7; color: #92400e; }
.severity-tag.severe { background: #fee2e2; color: #991b1b; }
.disease-symptoms { color: var(--text-secondary); font-size: 13px; line-height: 1.7; margin-bottom: 14px; }
.disease-meta { display: flex; gap: 8px; flex-wrap: wrap; }
.tag { background: #f1f5f9; padding: 4px 12px; border-radius: 50px; font-size: 12px; color: var(--text-secondary); font-weight: 500; }

.pager { display: flex; justify-content: center; margin-top: 44px; }
.app-footer { text-align: center; padding: 36px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }

@media (max-width: 768px) {
  .disease-grid { grid-template-columns: 1fr; }
  .search-bar { flex-direction: column; }
}
</style>
