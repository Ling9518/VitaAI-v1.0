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
            <span class="severity-tag" :class="d.severity?.toLowerCase()">{{ severityMap[d.severity] || d.severity }}</span>
          </div>
          <p class="disease-symptoms">{{ truncateText(d.symptoms, 100) }}</p>
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

const diseases = ref<any[]>([])
const classifications = ref<any[]>([])
const keyword = ref('')
const classification = ref('')
const page = ref(1)
const pageSize = 20
const total = ref(0)

const severityMap: Record<string, string> = { MILD: '轻度', MODERATE: '中度', SEVERE: '重度' }

function truncateText(text: string, len: number) {
  return text && text.length > len ? text.substring(0, len) + '...' : text || ''
}

async function fetchData() {
  try {
    const res = await api.get('/diseases', { params: { page: page.value, pageSize, keyword: keyword.value || undefined, classification: classification.value || undefined } })
    const data = res.data.data
    diseases.value = data.list || []
    total.value = data.pagination?.total || 0
  } catch { /* empty */ }
}

let searchTimer: any = null
function search() {
  clearTimeout(searchTimer)
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
.page-hero { text-align: center; margin-bottom: 40px; }
.page-hero h1 { font-size: 36px; font-weight: 800; margin-bottom: 12px; }
.page-hero p { color: var(--text-secondary); font-size: 16px; }

.search-bar { display: flex; gap: 16px; margin-bottom: 32px; }

.disease-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.disease-card { cursor: pointer; padding: 24px; }
.disease-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.disease-header h3 { font-size: 18px; font-weight: 700; }
.severity-tag { padding: 2px 10px; border-radius: 50px; font-size: 12px; font-weight: 600; }
.severity-tag.mild { background: #d1fae5; color: #065f46; }
.severity-tag.moderate { background: #fef3c7; color: #92400e; }
.severity-tag.severe { background: #fee2e2; color: #991b1b; }
.disease-symptoms { color: var(--text-secondary); font-size: 13px; line-height: 1.6; margin-bottom: 12px; }
.disease-meta { display: flex; gap: 8px; }
.tag { background: #f1f5f9; padding: 4px 10px; border-radius: 50px; font-size: 12px; color: var(--text-secondary); }

.pager { display: flex; justify-content: center; margin-top: 40px; }
.app-footer { text-align: center; padding: 32px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }

@media (max-width: 768px) {
  .disease-grid { grid-template-columns: 1fr; }
  .search-bar { flex-direction: column; }
}
</style>
