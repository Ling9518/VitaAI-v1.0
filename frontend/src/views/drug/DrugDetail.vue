<template>
  <div class="detail-page">
    <AppHeader />
    <div class="page-container section-padding">
      <div v-if="loading" class="loading-state">加载中...</div>
      <template v-else-if="drug">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/drugs' }">药品库</el-breadcrumb-item>
        <el-breadcrumb-item>{{ drug.name }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="detail-header">
        <div>
          <h1>{{ drug.name }}</h1>
          <div class="header-meta">
            <span v-if="drug.genericName">通用名：{{ drug.genericName }}</span>
            <span v-if="drug.brandName">品牌名：{{ drug.brandName }}</span>
          </div>
        </div>
        <div class="header-actions">
          <button class="favorite-btn" :class="{ active: isFavorited }" @click="toggleFavorite">
            {{ isFavorited ? '❤️ 已收藏' : '🤍 收藏' }}
          </button>
          <div class="header-tags">
            <span class="drug-type-tag">{{ typeMap[drug.drugType] }}</span>
            <span class="info-tag" v-if="drug.form">{{ drug.form }}</span>
          </div>
        </div>
      </div>

      <div class="detail-sections">
        <section class="detail-section card" v-if="drug.efficacy">
          <h3>💊 功效主治</h3>
          <p>{{ drug.efficacy }}</p>
        </section>
        <section class="detail-section card" v-if="drug.usage2">
          <h3>📋 用法说明</h3>
          <p>{{ drug.usage2 }}</p>
        </section>
        <section class="detail-section card" v-if="drug.dosage">
          <h3>⚖️ 用法用量</h3>
          <p>{{ drug.dosage }}</p>
        </section>
        <section class="detail-section card" v-if="drug.sideEffect">
          <h3>⚠️ 不良反应</h3>
          <p>{{ drug.sideEffect }}</p>
        </section>
        <section class="detail-section card" v-if="drug.contraindication">
          <h3>🚫 禁忌</h3>
          <p>{{ drug.contraindication }}</p>
        </section>
      </div>

      <div class="detail-meta-grid" v-if="drug.specification || drug.storage || drug.manufacturer || drug.approvalNo">
        <div class="meta-item" v-if="drug.specification">
          <span class="meta-label">规格</span>
          <span>{{ drug.specification }}</span>
        </div>
        <div class="meta-item" v-if="drug.storage">
          <span class="meta-label">贮藏</span>
          <span>{{ drug.storage }}</span>
        </div>
        <div class="meta-item" v-if="drug.manufacturer">
          <span class="meta-label">生产厂家</span>
          <span>{{ drug.manufacturer }}</span>
        </div>
        <div class="meta-item" v-if="drug.approvalNo">
          <span class="meta-label">批准文号</span>
          <span>{{ drug.approvalNo }}</span>
        </div>
        <div class="meta-item" v-if="drug.price">
          <span class="meta-label">参考价格</span>
          <span class="price">¥{{ drug.price }}</span>
        </div>
      </div>

      <div class="detail-disclaimer">
        <p>内容为AI诊断，想要更准确诊断，请去正规医院就诊。</p>
      </div>
      </template>
    </div>
    <footer class="app-footer"><p>&copy; 2026 VitaAI 智慧医院系统. All rights reserved.</p></footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import api from '@/api/index'
import { ElMessage } from 'element-plus'
import type { Drug } from '@/types'

const route = useRoute()
const drug = ref<Drug | null>(null)
const loading = ref(false)
const isFavorited = ref(false)

const typeMap: Record<string, string> = {
  PRESCRIPTION: '处方药', OTC: '非处方药', HERBAL: '中药', BIOLOGIC: '生物制品',
}

async function toggleFavorite() {
  try {
    const res = await api.post('/favorites/toggle', { targetType: 'DRUG', targetId: drug.value!.id })
    isFavorited.value = res.data.data.isFavorited
    if (drug.value) drug.value.favoritesCount = res.data.data.favoritesCount
  } catch {
    ElMessage.error('操作失败，请稍后重试')
  }
}

async function checkFavorite() {
  try {
    const res = await api.get('/favorites/check', { params: { targetType: 'DRUG', targetId: route.params.id } })
    isFavorited.value = res.data.data
  } catch { isFavorited.value = false }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await api.get(`/drugs/${route.params.id}`)
    drug.value = res.data.data
    checkFavorite()
  } catch {
    ElMessage.error('加载药品信息失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.detail-page { min-height: 100vh; display: flex; flex-direction: column; }
.breadcrumb { margin-bottom: 24px; padding: 0; }

.detail-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 44px; flex-wrap: wrap; gap: 20px; }
.detail-header h1 { font-size: 38px; font-weight: 800; margin-bottom: 10px; letter-spacing: -0.5px; }
.header-meta { display: flex; gap: 24px; color: var(--text-secondary); font-size: 14px; flex-wrap: wrap; }
.header-meta span { padding: 4px 12px; background: var(--bg); border-radius: 8px; }
.header-actions { display: flex; align-items: center; gap: 16px; }
.favorite-btn {
  background: var(--bg-card); border: 2px solid var(--border); border-radius: 50px;
  padding: 10px 24px; cursor: pointer; font-size: 14px; font-weight: 600;
  transition: all .25s ease; white-space: nowrap;
}
.favorite-btn:hover { border-color: #ef4444; color: #ef4444; background: #fef2f2; transform: translateY(-1px); }
.favorite-btn.active { border-color: #ef4444; color: #ef4444; background: linear-gradient(135deg, #fef2f2, #fee2e2); box-shadow: 0 2px 8px rgba(239,68,68,.15); }
.header-tags { display: flex; gap: 8px; }
.drug-type-tag { padding: 6px 18px; border-radius: 50px; font-size: 14px; font-weight: 600; background: linear-gradient(135deg, #ede9fe, #f3e8ff); color: #6b21a8; }
.info-tag { padding: 6px 16px; border-radius: 50px; font-size: 13px; background: var(--bg); color: var(--text-secondary); font-weight: 500; }

.detail-sections { display: flex; flex-direction: column; gap: 20px; margin-bottom: 36px; }
.detail-section {
  padding: 32px 36px; border: 1px solid var(--border);
  transition: all .25s ease; border-radius: var(--radius);
}
.detail-section:hover { border-color: rgba(124,58,237,.1); box-shadow: var(--shadow-md); }
.detail-section h3 { font-size: 20px; font-weight: 700; margin-bottom: 18px; display: flex; align-items: center; gap: 8px; }
.detail-section p { color: var(--text-secondary); line-height: 1.9; font-size: 15px; white-space: pre-line; }

.detail-meta-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 36px; }
.meta-item {
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  padding: 18px 20px; border-radius: var(--radius-sm); display: flex; flex-direction: column; gap: 6px;
  border: 1px solid var(--border); transition: all .2s ease;
}
.meta-item:hover { border-color: rgba(124,58,237,.1); box-shadow: var(--shadow); }
.meta-label { font-size: 12px; color: var(--text-light); font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }
.meta-item span:last-child { font-size: 15px; font-weight: 600; color: var(--text); }
.price { color: var(--danger); font-size: 18px !important; }

.detail-disclaimer { background: linear-gradient(135deg, #fffbeb, #fef3c7); padding: 14px 24px; border-radius: var(--radius-sm); text-align: center; font-size: 13px; color: #92400e; margin-bottom: 48px; border: 1px solid #fde68a; }
.app-footer { text-align: center; padding: 36px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }

@media (max-width: 768px) {
  .detail-header h1 { font-size: 24px; }
  .detail-meta-grid { grid-template-columns: 1fr; }
  .detail-section { padding: 24px; }
}
</style>
