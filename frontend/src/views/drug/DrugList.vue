<template>
  <div class="list-page">
    <AppHeader />
    <div class="page-container section-padding">
      <div class="page-hero">
        <h1>药品<span class="gradient-text">知识库</span></h1>
        <p>了解药品功效、用法用量和注意事项</p>
      </div>

      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索药品名称、功效..." size="large" clearable @input="search">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="drugType" placeholder="药品类型" size="large" clearable @change="search" style="width: 200px;">
          <el-option v-for="t in drugTypes" :key="t.code" :label="t.name" :value="t.code" />
        </el-select>
      </div>

      <div class="drug-grid" v-if="drugs.length">
        <div class="drug-card card" v-for="d in drugs" :key="d.id" @click="$router.push(`/drugs/${d.id}`)">
          <div class="drug-header">
            <h3>{{ d.name }}</h3>
            <span class="drug-type-tag">{{ typeMap[d.drugType] || d.drugType }}</span>
          </div>
          <p class="drug-generic" v-if="d.genericName">通用名：{{ d.genericName }}</p>
          <p class="drug-efficacy">{{ truncateText(d.efficacy, 100) }}</p>
          <div class="drug-meta">
            <span v-if="d.form">{{ d.form }}</span>
            <span v-if="d.specification">{{ d.specification }}</span>
            <span v-if="d.manufacturer">{{ d.manufacturer }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无药品数据" />

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

const drugs = ref<any[]>([])
const drugTypes = ref<any[]>([])
const keyword = ref('')
const drugType = ref('')
const page = ref(1)
const pageSize = 20
const total = ref(0)

const typeMap: Record<string, string> = {
  PRESCRIPTION: '处方药', OTC: '非处方药', HERBAL: '中药', BIOLOGIC: '生物制品',
}

function truncateText(text: string, len: number) {
  return text && text.length > len ? text.substring(0, len) + '...' : text || ''
}

async function fetchData() {
  try {
    const res = await api.get('/drugs', { params: { page: page.value, pageSize, keyword: keyword.value || undefined, drugType: drugType.value || undefined } })
    const data = res.data.data
    drugs.value = data.list || []
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
    const res = await api.get('/system/dicts/drug_type')
    drugTypes.value = res.data.data || []
  } catch { /* empty */ }
})
</script>

<style scoped>
.list-page { min-height: 100vh; display: flex; flex-direction: column; }
.page-hero { text-align: center; margin-bottom: 40px; }
.page-hero h1 { font-size: 36px; font-weight: 800; margin-bottom: 12px; }
.page-hero p { color: var(--text-secondary); font-size: 16px; }

.search-bar { display: flex; gap: 16px; margin-bottom: 32px; }

.drug-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.drug-card { cursor: pointer; padding: 24px; }
.drug-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.drug-header h3 { font-size: 18px; font-weight: 700; }
.drug-type-tag { padding: 2px 10px; border-radius: 50px; font-size: 12px; font-weight: 600; background: #ede9fe; color: #6b21a8; }
.drug-generic { font-size: 13px; color: var(--text-light); margin-bottom: 8px; }
.drug-efficacy { color: var(--text-secondary); font-size: 13px; line-height: 1.6; margin-bottom: 12px; }
.drug-meta { display: flex; gap: 8px; flex-wrap: wrap; }
.drug-meta span { background: #f1f5f9; padding: 4px 10px; border-radius: 50px; font-size: 12px; color: var(--text-secondary); }

.pager { display: flex; justify-content: center; margin-top: 40px; }
.app-footer { text-align: center; padding: 32px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }

@media (max-width: 768px) {
  .drug-grid { grid-template-columns: 1fr; }
  .search-bar { flex-direction: column; }
}
</style>
