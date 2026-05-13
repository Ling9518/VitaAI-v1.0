<template>
  <div class="home">
    <AppHeader />
    <!-- Hero Section -->
    <section class="hero section-padding">
      <div class="page-container hero-content">
        <div class="hero-text">
          <div class="hero-badge">🩺 AI-Powered Medical Assistant</div>
          <h1>用人工智能<span class="gradient-text">守护健康</span></h1>
          <p>VitaAI智慧医院系统，整合全球医学知识与DeepSeek大模型，为您提供专业的AI诊断、健康管理和用药指导服务。</p>
          <div class="hero-buttons">
            <button class="btn-primary btn-lg" @click="$router.push('/ai-chat')">
              <el-icon><ChatDotRound /></el-icon> 开始AI诊断
            </button>
            <el-button class="btn-outline" size="large" round @click="$router.push('/diseases')">
              浏览疾病库
            </el-button>
          </div>
          <div class="hero-stats">
            <div class="stat-item"><span class="stat-num">90+</span><span class="stat-label">疾病覆盖</span></div>
            <div class="stat-item"><span class="stat-num">120+</span><span class="stat-label">药品分类</span></div>
            <div class="stat-item"><span class="stat-num">24/7</span><span class="stat-label">全天候服务</span></div>
          </div>
        </div>
        <div class="hero-visual">
          <div class="hero-orb"></div>
          <div class="hero-orb-2"></div>
          <div class="hero-icon-wrap">
            <span class="hero-icon">⚕️</span>
          </div>
        </div>
      </div>
    </section>
    <!-- Features -->
    <section class="features section-padding">
      <div class="page-container">
        <h2 class="section-title">核心<span class="gradient-text">功能</span></h2>
        <div class="features-grid">
          <div class="feature-card card" v-for="f in features" :key="f.title">
            <span class="feature-icon">{{ f.icon }}</span>
            <h3>{{ f.title }}</h3>
            <p>{{ f.desc }}</p>
          </div>
        </div>
      </div>
    </section>
    <!-- Popular Diseases -->
    <section class="popular section-padding" v-if="topDiseases.length">
      <div class="page-container">
        <h2 class="section-title">热门<span class="gradient-text">疾病</span></h2>
        <div class="disease-grid">
          <div class="disease-card card" v-for="d in topDiseases" :key="d.id" @click="$router.push(`/diseases/${d.id}`)">
            <h4>{{ d.name }}</h4>
            <p class="disease-symptoms">{{ truncateText(d.symptoms, 80) }}</p>
            <span class="disease-tag">{{ d.classification }}</span>
          </div>
        </div>
      </div>
    </section>
    <!-- CTA -->
    <section class="cta section-padding">
      <div class="page-container">
        <div class="cta-card">
          <h2>立即体验<span class="gradient-text"> AI智能诊断</span></h2>
          <p>描述您的症状，AI医生将为您提供专业的初步诊断和建议</p>
          <button class="btn-primary btn-lg" @click="$router.push('/ai-chat')">开始诊断</button>
          <p class="cta-disclaimer">内容为AI诊断，想要更准确诊断，请去正规医院就诊。</p>
        </div>
      </div>
    </section>
    <footer class="app-footer"><p>&copy; 2026 VitaAI 智慧医院系统. All rights reserved.</p></footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import api from '@/api/index'

const features = [
  { icon: '🤖', title: 'AI智能诊断', desc: '基于DeepSeek大模型，整合医学知识库，提供专业的多轮对话诊断服务' },
  { icon: '📋', title: '健康档案', desc: '建立个人健康档案，记录病史、过敏史、用药记录，AI诊断更精准' },
  { icon: '🔍', title: '症状自测', desc: '结构化症状问卷，智能引导准确描述病情，初步评估健康风险' },
  { icon: '💊', title: '疾病药品库', desc: '涵盖90+疾病和120+药品分类，专业的疾病介绍和用药指导' },
  { icon: '📊', title: '诊断报告', desc: '生成结构化诊断报告，包含诊断建议、用药方案和注意事项' },
  { icon: '🔒', title: '数据安全', desc: '医疗数据加密存储，操作审计全覆盖，保障您的隐私安全' },
]

const topDiseases = ref<any[]>([])
onMounted(async () => {
  try { const res = await api.get('/diseases/top', { params: { limit: 6 } }); topDiseases.value = res.data.data } catch {}
})
function truncateText(text: string, len: number) {
  return text && text.length > len ? text.substring(0, len) + '...' : text || ''
}
</script>

<style scoped>
.hero { min-height: 85vh; display: flex; align-items: center; }
.hero-content { display: grid; grid-template-columns: 1fr 1fr; gap: 60px; align-items: center; }
.hero-badge {
  display: inline-block; background: linear-gradient(135deg, #dbeafe, #e0e7ff);
  color: var(--primary); padding: 8px 20px; border-radius: 50px; font-size: 14px; font-weight: 600; margin-bottom: 24px;
}
.hero-text h1 { font-size: 52px; font-weight: 800; line-height: 1.2; margin-bottom: 20px; }
.hero-text p { font-size: 18px; color: var(--text-secondary); margin-bottom: 36px; max-width: 540px; line-height: 1.7; }
.hero-buttons { display: flex; gap: 16px; margin-bottom: 48px; }
.btn-lg { padding: 16px 36px; font-size: 17px; display: flex; align-items: center; gap: 8px; }
.btn-outline { font-weight: 600; }
.hero-stats { display: flex; gap: 48px; }
.stat-item { display: flex; flex-direction: column; }
.stat-num { font-size: 32px; font-weight: 800; color: var(--primary); }
.stat-label { font-size: 14px; color: var(--text-secondary); }
.hero-visual { position: relative; display: flex; align-items: center; justify-content: center; }
.hero-orb {
  width: 320px; height: 320px; border-radius: 50%;
  background: radial-gradient(circle, rgba(37,99,235,.15), transparent 70%);
  position: absolute;
}
.hero-orb-2 {
  width: 200px; height: 200px; border-radius: 50%;
  background: radial-gradient(circle, rgba(6,182,212,.2), transparent 70%);
  position: absolute; transform: translate(60px, -40px);
}
.hero-icon-wrap { position: relative; z-index: 1; }
.hero-icon { font-size: 160px; filter: drop-shadow(0 20px 40px rgba(37,99,235,.2)); }

.section-title { text-align: center; font-size: 36px; font-weight: 800; margin-bottom: 48px; }
.features-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px;
}
.feature-card { text-align: center; padding: 36px 24px; }
.feature-icon { font-size: 48px; display: block; margin-bottom: 16px; }
.feature-card h3 { font-size: 20px; font-weight: 700; margin-bottom: 12px; }
.feature-card p { color: var(--text-secondary); font-size: 14px; line-height: 1.7; }

.disease-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.disease-card { cursor: pointer; padding: 24px; }
.disease-card h4 { font-size: 18px; font-weight: 700; margin-bottom: 8px; }
.disease-symptoms { color: var(--text-secondary); font-size: 13px; margin-bottom: 12px; }
.disease-tag { display: inline-block; background: #e0e7ff; color: var(--primary); padding: 4px 12px; border-radius: 50px; font-size: 12px; font-weight: 500; }

.cta { text-align: center; }
.cta-card {
  background: linear-gradient(135deg, #1e293b, #0f172a); border-radius: 24px; padding: 64px 40px; color: white;
}
.cta-card h2 { font-size: 36px; font-weight: 800; margin-bottom: 16px; }
.cta-card p { color: #94a3b8; font-size: 16px; margin-bottom: 32px; }
.cta-disclaimer { font-size: 12px; color: #64748b; margin-top: 24px; }

.app-footer { text-align: center; padding: 32px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); }

@media (max-width: 768px) {
  .hero-content { grid-template-columns: 1fr; }
  .hero-text h1 { font-size: 36px; }
  .features-grid { grid-template-columns: 1fr; }
  .disease-grid { grid-template-columns: 1fr; }
}
</style>
