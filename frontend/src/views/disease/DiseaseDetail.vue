<template>
  <div class="detail-page">
    <AppHeader />
    <div class="page-container section-padding" v-if="disease">
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
        <div class="header-tags">
          <span class="severity-tag" :class="disease.severity?.toLowerCase()">{{ severityMap[disease.severity] }}</span>
          <span class="tag" v-if="disease.isInfectious">传染病</span>
          <span class="tag" v-if="disease.isChronic">慢性病</span>
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
    </div>
    <footer class="app-footer"><p>&copy; 2026 VitaAI 智慧医院系统. All rights reserved.</p></footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import api from '@/api/index'

const route = useRoute()
const disease = ref<any>(null)

const severityMap: Record<string, string> = { MILD: '轻度', MODERATE: '中度', SEVERE: '重度' }

onMounted(async () => {
  try {
    const res = await api.get(`/diseases/${route.params.id}`)
    disease.value = res.data.data
  } catch { /* empty */ }
})
</script>

<style scoped>
.detail-page { min-height: 100vh; display: flex; flex-direction: column; }
.breadcrumb { margin-bottom: 24px; }

.detail-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 40px; flex-wrap: wrap; gap: 16px; }
.detail-header h1 { font-size: 36px; font-weight: 800; margin-bottom: 8px; }
.header-meta { display: flex; gap: 20px; color: var(--text-secondary); font-size: 14px; }
.header-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.severity-tag { padding: 6px 16px; border-radius: 50px; font-size: 14px; font-weight: 600; }
.severity-tag.mild { background: #d1fae5; color: #065f46; }
.severity-tag.moderate { background: #fef3c7; color: #92400e; }
.severity-tag.severe { background: #fee2e2; color: #991b1b; }
.tag { background: #f1f5f9; padding: 6px 14px; border-radius: 50px; font-size: 13px; color: var(--text-secondary); }

.detail-sections { display: flex; flex-direction: column; gap: 20px; margin-bottom: 40px; }
.detail-section { padding: 28px 32px; }
.detail-section h3 { font-size: 20px; font-weight: 700; margin-bottom: 16px; }
.detail-section p { color: var(--text-secondary); line-height: 1.8; font-size: 15px; white-space: pre-line; }

.detail-footer { display: flex; justify-content: space-between; color: var(--text-light); font-size: 13px; padding: 24px 0; border-top: 1px solid var(--border); flex-wrap: wrap; gap: 8px; }
.detail-disclaimer { background: #fef3c7; padding: 12px 20px; border-radius: var(--radius-sm); text-align: center; font-size: 13px; color: #92400e; margin-bottom: 40px; }
.app-footer { text-align: center; padding: 32px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }

@media (max-width: 768px) {
  .detail-header h1 { font-size: 24px; }
}
</style>
