<template>
  <div class="detail-page">
    <AppHeader />
    <div class="page-container section-padding">
      <div v-if="loading" class="loading-state">加载中...</div>
      <template v-else-if="disease">
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/diseases' }">疾病库</el-breadcrumb-item>
          <el-breadcrumb-item>{{ disease.name }}</el-breadcrumb-item>
        </el-breadcrumb>

        <div class="detail-header">
          <div>
            <h1>{{ disease.name }}</h1>
            <div class="header-meta">
              <span v-if="disease.alias" class="alias">别名：{{ disease.alias }}</span>
              <span v-if="disease.icdCode" class="icd">ICD：{{ disease.icdCode }}</span>
            </div>
          </div>
          <div class="header-actions">
            <button class="favorite-btn" :class="{ active: isFavorited }" @click="toggleFavorite">
              {{ isFavorited ? '❤️ 已收藏' : '🤍 收藏' }}
            </button>
            <div class="header-tags">
              <span class="severity-tag" :class="disease.severity?.toLowerCase()">{{ diseaseSevMap[disease.severity || ''] }}</span>
              <span class="tag" v-if="disease.isInfectious">传染病</span>
              <span class="tag" v-if="disease.isChronic">慢性病</span>
            </div>
          </div>
        </div>

        <div class="detail-sections">
          <section class="detail-section card" v-if="disease.cause">
            <h3>📋 病因</h3>
            <p>{{ disease.cause }}</p>
          </section>
          <section class="detail-section card" v-if="disease.symptoms">
            <h3>🩺 症状表现</h3>
            <p>{{ disease.symptoms }}</p>
          </section>
          <section class="detail-section card" v-if="disease.diagnosis">
            <h3>🔍 诊断方法</h3>
            <p>{{ disease.diagnosis }}</p>
          </section>
          <section class="detail-section card" v-if="disease.treatment">
            <h3>💊 治疗方案</h3>
            <p>{{ disease.treatment }}</p>
          </section>
          <section class="detail-section card" v-if="disease.prevention">
            <h3>🛡️ 预防措施</h3>
            <p>{{ disease.prevention }}</p>
          </section>
          <section class="detail-section card" v-if="disease.complications">
            <h3>⚠️ 并发症</h3>
            <p>{{ disease.complications }}</p>
          </section>
        </div>

        <div class="detail-footer">
          <span>分类：{{ disease.classification }} | {{ disease.bodySystem }}</span>
          <span>浏览：{{ disease.viewsCount }} | 收藏：{{ disease.favoritesCount }}</span>
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
import type { Disease } from '@/types'

const diseaseSevMap: Record<string, string> = { MILD: '轻度', MODERATE: '中度', SEVERE: '重度' }

const route = useRoute()
const disease = ref<Disease | null>(null)
const loading = ref(false)
const isFavorited = ref(false)

async function toggleFavorite() {
  try {
    const res = await api.post('/favorites/toggle', { targetType: 'DISEASE', targetId: disease.value!.id })
    isFavorited.value = res.data.data.isFavorited
    if (disease.value) disease.value.favoritesCount = res.data.data.favoritesCount
  } catch {
    ElMessage.error('操作失败，请稍后重试')
  }
}

async function checkFavorite() {
  try {
    const res = await api.get('/favorites/check', { params: { targetType: 'DISEASE', targetId: route.params.id } })
    isFavorited.value = res.data.data
  } catch { isFavorited.value = false }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await api.get(`/diseases/${route.params.id}`)
    disease.value = res.data.data
    checkFavorite()
  } catch {
    ElMessage.error('加载疾病信息失败')
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
.header-meta .alias, .header-meta .icd { padding: 4px 12px; background: var(--bg); border-radius: 8px; }
.header-actions { display: flex; align-items: center; gap: 16px; }
.favorite-btn {
  background: var(--bg-card); border: 2px solid var(--border); border-radius: 50px;
  padding: 10px 24px; cursor: pointer; font-size: 14px; font-weight: 600;
  transition: all .25s ease; white-space: nowrap;
}
.favorite-btn:hover { border-color: #ef4444; color: #ef4444; background: #fef2f2; transform: translateY(-1px); }
.favorite-btn.active { border-color: #ef4444; color: #ef4444; background: linear-gradient(135deg, #fef2f2, #fee2e2); box-shadow: 0 2px 8px rgba(239,68,68,.15); }
.header-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.severity-tag { padding: 6px 18px; border-radius: 50px; font-size: 14px; font-weight: 600; }
.severity-tag.mild { background: #d1fae5; color: #065f46; }
.severity-tag.moderate { background: #fef3c7; color: #92400e; }
.severity-tag.severe { background: #fee2e2; color: #991b1b; }
.tag { background: var(--bg); padding: 6px 16px; border-radius: 50px; font-size: 13px; color: var(--text-secondary); font-weight: 500; }

.detail-sections { display: flex; flex-direction: column; gap: 20px; margin-bottom: 44px; }
.detail-section {
  padding: 32px 36px; border: 1px solid var(--border);
  transition: all .25s ease; border-radius: var(--radius);
}
.detail-section:hover { border-color: rgba(37,99,235,.1); box-shadow: var(--shadow-md); }
.detail-section h3 { font-size: 20px; font-weight: 700; margin-bottom: 18px; display: flex; align-items: center; gap: 8px; }
.detail-section p { color: var(--text-secondary); line-height: 1.9; font-size: 15px; white-space: pre-line; }

.detail-footer { display: flex; justify-content: space-between; color: var(--text-light); font-size: 13px; padding: 24px 0; border-top: 1px solid var(--border); flex-wrap: wrap; gap: 12px; }
.detail-disclaimer { background: linear-gradient(135deg, #fffbeb, #fef3c7); padding: 14px 24px; border-radius: var(--radius-sm); text-align: center; font-size: 13px; color: #92400e; margin-bottom: 48px; border: 1px solid #fde68a; }
.app-footer { text-align: center; padding: 36px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }

@media (max-width: 768px) {
  .detail-header h1 { font-size: 24px; }
  .detail-section { padding: 24px; }
}
</style>
