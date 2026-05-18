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
          <p class="drug-efficacy">{{ truncateText(d.efficacy ?? null, 100) }}</p>
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
import { ElMessage } from 'element-plus'
import type { Drug, DictItem } from '@/types'
import { truncateText } from '@/utils'

const typeMap: Record<string, string> = {
  PRESCRIPTION: '处方药', OTC: '非处方药', HERBAL: '中药', BIOLOGIC: '生物制品',
}

const drugs = ref<Drug[]>([])
const drugTypes = ref<DictItem[]>([])
const loading = ref(false)
const keyword = ref('')
const drugType = ref('')
const page = ref(1)
const pageSize = 20
const total = ref(0)

async function fetchData() {
  loading.value = true
  try {
    const res = await api.get('/drugs', { params: { page: page.value, pageSize, keyword: keyword.value || undefined, drugType: drugType.value || undefined } })
    const data = res.data.data
    drugs.value = data.list || []
    total.value = data.pagination?.total || 0
  } catch {
    ElMessage.error('加载药品列表失败')
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
    const res = await api.get('/system/dicts/drug_type')
    drugTypes.value = res.data.data || []
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

.drug-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.drug-card {
  cursor: pointer; padding: 28px; transition: all .35s cubic-bezier(.4,0,.2,1);
  border: 1px solid var(--border); position: relative; overflow: hidden;
}
.drug-card::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 3px;
  background: linear-gradient(90deg, #7c3aed, var(--accent));
  transform: scaleX(0); transform-origin: left; transition: transform .35s ease;
}
.drug-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-lg); border-color: transparent; }
.drug-card:hover::before { transform: scaleX(1); }
.drug-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.drug-header h3 { font-size: 18px; font-weight: 700; }
.drug-type-tag { padding: 3px 12px; border-radius: 50px; font-size: 12px; font-weight: 600; background: linear-gradient(135deg, #ede9fe, #f3e8ff); color: #6b21a8; }
.drug-generic { font-size: 13px; color: var(--text-light); margin-bottom: 10px; }
.drug-efficacy { color: var(--text-secondary); font-size: 13px; line-height: 1.7; margin-bottom: 14px; }
.drug-meta { display: flex; gap: 8px; flex-wrap: wrap; }
.drug-meta span { background: #f1f5f9; padding: 4px 12px; border-radius: 50px; font-size: 12px; color: var(--text-secondary); font-weight: 500; }

.pager { display: flex; justify-content: center; margin-top: 44px; }
.app-footer { text-align: center; padding: 36px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }

@media (max-width: 768px) {
  .drug-grid { grid-template-columns: 1fr; }
  .search-bar { flex-direction: column; }
}
</style>
