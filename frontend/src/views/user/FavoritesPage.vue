<template>
  <div class="favorites-page">
    <AppHeader />
    <div class="page-container section-padding">
      <div class="page-hero">
        <h1>我的<span class="gradient-text">收藏</span></h1>
        <p>收藏的疾病和药品，方便快速查阅</p>
      </div>

      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="疾病收藏" name="diseases">
          <div v-if="loading" class="loading-state"><el-icon class="is-loading"><Loading /></el-icon> 加载中...</div>
          <div class="favorites-grid" v-else-if="diseases.length">
            <div class="fav-card card" v-for="item in diseases" :key="item.id" @click="$router.push(`/diseases/${item.targetId}`)">
              <div class="fav-card-header">
                <h3>{{ item.name }}</h3>
                <span class="fav-tag">{{ item.classification }}</span>
              </div>
              <span class="fav-arrow">→</span>
            </div>
          </div>
          <el-empty v-else description="暂无收藏的疾病" :image-size="60" />
        </el-tab-pane>

        <el-tab-pane label="药品收藏" name="drugs">
          <div class="favorites-grid" v-if="drugs.length">
            <div class="fav-card card" v-for="item in drugs" :key="item.id" @click="$router.push(`/drugs/${item.targetId}`)">
              <div class="fav-card-header">
                <h3>{{ item.name }}</h3>
                <span class="fav-tag drug-tag">{{ item.drugType }}</span>
              </div>
              <span class="fav-arrow">→</span>
            </div>
          </div>
          <el-empty v-else description="暂无收藏的药品" :image-size="60" />
        </el-tab-pane>
      </el-tabs>
    </div>
    <footer class="app-footer"><p>&copy; 2026 VitaAI 智慧医院系统. All rights reserved.</p></footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import api from '@/api/index'
import { ElMessage } from 'element-plus'
import type { FavoriteItem } from '@/types'
import { Loading } from '@element-plus/icons-vue'

const activeTab = ref('diseases')
const diseases = ref<FavoriteItem[]>([])
const drugs = ref<FavoriteItem[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const [dRes, dgRes] = await Promise.all([
      api.get('/favorites', { params: { targetType: 'DISEASE' } }),
      api.get('/favorites', { params: { targetType: 'DRUG' } })
    ])
    diseases.value = dRes.data.data || []
    drugs.value = dgRes.data.data || []
  } catch {
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.favorites-page { min-height: 100vh; display: flex; flex-direction: column; }
.page-hero { text-align: center; margin-bottom: 36px; }
.page-hero h1 { font-size: 38px; font-weight: 800; margin-bottom: 8px; letter-spacing: -0.5px; }
.page-hero p { color: var(--text-secondary); font-size: 16px; }

.favorites-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.fav-card {
  display: flex; align-items: center; justify-content: space-between;
  padding: 20px 28px; cursor: pointer; transition: all .3s cubic-bezier(.4,0,.2,1);
  border: 1px solid var(--border); overflow: hidden; position: relative;
}
.fav-card::before {
  content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 3px;
  background: linear-gradient(180deg, var(--primary), var(--accent));
  transform: scaleY(0); transition: transform .3s ease;
}
.fav-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); border-color: transparent; }
.fav-card:hover::before { transform: scaleY(1); }
.fav-card-header { display: flex; align-items: center; gap: 14px; }
.fav-card-header h3 { font-size: 16px; font-weight: 600; }
.fav-tag {
  background: linear-gradient(135deg, #dbeafe, #e0e7ff); color: var(--primary);
  padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 600;
}
.drug-tag { background: linear-gradient(135deg, #ede9fe, #f3e8ff); color: #6b21a8; }
.fav-arrow { color: var(--text-light); font-size: 20px; transition: transform .2s ease; }
.fav-card:hover .fav-arrow { transform: translateX(4px); color: var(--primary); }

.loading-state { text-align: center; padding: 60px; color: var(--text-light); font-size: 15px; }

.app-footer { text-align: center; padding: 36px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }

@media (max-width: 768px) {
  .favorites-grid { grid-template-columns: 1fr; }
}
</style>
