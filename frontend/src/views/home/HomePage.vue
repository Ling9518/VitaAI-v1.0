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
    <section id="features" class="features section-padding">
      <div class="page-container">
        <div class="section-intro" v-if="!userStore.isLoggedIn">
          <p class="intro-text">VitaAI智慧医院系统，整合全球医学知识与DeepSeek大模型，为学校、工厂、监狱等不方便前往医院的人群提供便捷、安全的AI医疗服务。系统将医生诊断案例转化为"skills"并植入大模型，实现精准病情判断。</p>
        </div>
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
            <p class="disease-symptoms">{{ truncateText(d.symptoms ?? null, 80) }}</p>
            <span class="disease-tag">{{ d.classification }}</span>
          </div>
        </div>
      </div>
    </section>
    <!-- Contact -->
    <section id="contact" class="contact section-padding">
      <div class="page-container">
        <h2 class="section-title"><span class="gradient-text">联系</span>我们</h2>
        <p class="section-subtitle">如有任何问题或合作意向，请随时与我们联系</p>
        <div class="contact-grid">
          <div class="info-card card">
            <h3>联系方式</h3>
            <div class="info-list">
              <div class="info-item">
                <span class="info-icon">📞</span>
                <div>
                  <div class="info-label">电话</div>
                  <div class="info-value">18943587503</div>
                </div>
              </div>
              <div class="info-item">
                <span class="info-icon">📧</span>
                <div>
                  <div class="info-label">邮箱</div>
                  <div class="info-value">ROTATED_EMAIL</div>
                </div>
              </div>
              <div class="info-item">
                <span class="info-icon">📍</span>
                <div>
                  <div class="info-label">地址</div>
                  <div class="info-value">安徽省芜湖市</div>
                </div>
              </div>
            </div>
          </div>
          <div class="form-card card">
            <h3>在线留言</h3>
            <el-form ref="contactFormRef" :model="contactForm" :rules="contactRules" size="large" @submit.prevent="handleContactSubmit">
              <el-form-item prop="name">
                <el-input v-model="contactForm.name" placeholder="您的姓名" />
              </el-form-item>
              <el-form-item prop="phone">
                <el-input v-model="contactForm.phone" placeholder="联系电话" />
              </el-form-item>
              <el-form-item prop="email">
                <el-input v-model="contactForm.email" placeholder="电子邮箱" />
              </el-form-item>
              <el-form-item prop="content">
                <el-input v-model="contactForm.content" type="textarea" :rows="4" placeholder="请输入您的留言内容..." />
              </el-form-item>
              <el-form-item>
                <button class="btn-primary btn-full" :disabled="contactSubmitting">
                  {{ contactSubmitting ? '提交中...' : '提交留言' }}
                </button>
              </el-form-item>
            </el-form>
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
import { ref, reactive, onMounted } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import api from '@/api/index'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { Disease } from '@/types'
import { truncateText } from '@/utils'
const userStore = useUserStore()

const features = [
  { icon: '🤖', title: 'AI智能诊断', desc: '基于DeepSeek大模型，整合医学知识库，提供专业的多轮对话诊断服务' },
  { icon: '📋', title: '健康档案', desc: '建立个人健康档案，记录病史、过敏史、用药记录，AI诊断更精准' },
  { icon: '🔍', title: '症状自测', desc: '结构化症状问卷，智能引导准确描述病情，初步评估健康风险' },
  { icon: '💊', title: '疾病药品库', desc: '涵盖90+疾病和120+药品分类，专业的疾病介绍和用药指导' },
  { icon: '📊', title: '诊断报告', desc: '生成结构化诊断报告，包含诊断建议、用药方案和注意事项' },
  { icon: '🔒', title: '数据安全', desc: '医疗数据加密存储，操作审计全覆盖，保障您的隐私安全' },
]

const topDiseases = ref<Disease[]>([])

// Contact form
const contactFormRef = ref()
const contactSubmitting = ref(false)
const contactForm = reactive({ name: '', phone: '', email: '', content: '' })
const contactRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  content: [{ required: true, message: '请输入留言内容', trigger: 'blur' }, { min: 5, message: '留言内容至少5个字', trigger: 'blur' }],
}
async function handleContactSubmit() {
  const valid = await contactFormRef.value.validate().catch(() => false)
  if (!valid) return
  contactSubmitting.value = true
  try {
    await api.post('/contact-messages', { ...contactForm })
    ElMessage.success('留言已提交，感谢您的反馈！')
    contactForm.name = ''; contactForm.phone = ''; contactForm.email = ''; contactForm.content = ''
  } catch {
    ElMessage.error('留言提交失败，请稍后重试')
  }
  finally { contactSubmitting.value = false }
}

onMounted(async () => {
  try { const res = await api.get('/diseases/top', { params: { limit: 6 } }); topDiseases.value = res.data.data } catch { ElMessage.error('加载热门疾病失败') }
  // Handle hash anchor on page load
  const hash = window.location.hash
  if (hash) {
    setTimeout(() => {
      const el = document.getElementById(hash.replace('#', ''))
      if (el) el.scrollIntoView({ behavior: 'smooth' })
    }, 300)
  }
})
function homeTruncate(text: string, len: number) {
  return text && text.length > len ? text.substring(0, len) + '...' : text || ''
}
</script>

<style scoped>
.hero { min-height: 88vh; display: flex; align-items: center; position: relative; overflow: hidden; }
.hero::before {
  content: ''; position: absolute; top: -50%; left: -50%; width: 200%; height: 200%;
  background: radial-gradient(ellipse at 30% 20%, rgba(37,99,235,.04) 0%, transparent 50%),
              radial-gradient(ellipse at 70% 80%, rgba(6,182,212,.04) 0%, transparent 50%);
  animation: gradient-shift 12s ease infinite;
  background-size: 200% 200%;
}
.hero-content {
  display: grid; grid-template-columns: 1fr 1fr; gap: 80px; align-items: center;
  position: relative; z-index: 1;
}
.hero-badge {
  display: inline-flex; align-items: center; gap: 6px;
  background: linear-gradient(135deg, rgba(37,99,235,.08), rgba(6,182,212,.08));
  border: 1px solid rgba(37,99,235,.12);
  color: var(--primary); padding: 8px 20px; border-radius: 50px; font-size: 13px; font-weight: 600; margin-bottom: 28px;
  animation: fadeInUp .6s ease-out;
}
.hero-text h1 { font-size: 54px; font-weight: 800; line-height: 1.15; margin-bottom: 24px; letter-spacing: -0.5px; animation: fadeInUp .6s ease-out .1s both; }
.hero-text p { font-size: 18px; color: var(--text-secondary); margin-bottom: 40px; max-width: 540px; line-height: 1.8; animation: fadeInUp .6s ease-out .2s both; }
.hero-buttons { display: flex; gap: 16px; margin-bottom: 56px; animation: fadeInUp .6s ease-out .3s both; }
.btn-lg { padding: 16px 36px; font-size: 17px; display: inline-flex; align-items: center; gap: 8px; border-radius: 50px; }
.btn-outline { font-weight: 600; border-radius: 50px; padding: 14px 32px; }
.hero-stats { display: flex; gap: 56px; animation: fadeInUp .6s ease-out .4s both; }
.stat-item { display: flex; flex-direction: column; position: relative; }
.stat-item + .stat-item::before {
  content: ''; position: absolute; left: -28px; top: 8px; bottom: 8px;
  width: 1px; background: var(--border);
}
.stat-num { font-size: 34px; font-weight: 800; color: var(--primary); letter-spacing: -0.5px; }
.stat-label { font-size: 14px; color: var(--text-light); margin-top: 2px; }
.hero-visual { position: relative; display: flex; align-items: center; justify-content: center; }
.hero-orb {
  width: 360px; height: 360px; border-radius: 50%;
  background: radial-gradient(circle, rgba(37,99,235,.1), transparent 70%);
  position: absolute; animation: float 6s ease-in-out infinite;
}
.hero-orb-2 {
  width: 240px; height: 240px; border-radius: 50%;
  background: radial-gradient(circle, rgba(6,182,212,.15), transparent 70%);
  position: absolute; transform: translate(80px, -50px);
  animation: float-slow 8s ease-in-out infinite;
}
.hero-icon-wrap { position: relative; z-index: 1; }
.hero-icon { font-size: 180px; filter: drop-shadow(0 24px 48px rgba(37,99,235,.15)); animation: float 5s ease-in-out infinite; }

.section-intro { max-width: 720px; margin: 0 auto 48px; text-align: center; }
.intro-text { color: var(--text-secondary); font-size: 16px; line-height: 1.8; }
.section-title { text-align: center; font-size: 38px; font-weight: 800; margin-bottom: 56px; letter-spacing: -0.5px; }
.features { background: linear-gradient(180deg, transparent, rgba(37,99,235,.02), transparent); }
.features-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 28px; }
.feature-card {
  text-align: center; padding: 40px 28px;
  transition: all .4s cubic-bezier(.4,0,.2,1);
}
.feature-card:hover { transform: translateY(-6px); box-shadow: var(--shadow-lg); border-color: rgba(37,99,235,.12); }
.feature-icon { font-size: 52px; display: block; margin-bottom: 20px; transition: transform .3s ease; }
.feature-card:hover .feature-icon { transform: scale(1.1); }
.feature-card h3 { font-size: 20px; font-weight: 700; margin-bottom: 12px; }
.feature-card p { color: var(--text-secondary); font-size: 14px; line-height: 1.7; }

.popular { background: linear-gradient(180deg, rgba(37,99,235,.02), transparent, rgba(37,99,235,.02)); }
.disease-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.disease-card {
  cursor: pointer; padding: 28px; transition: all .35s cubic-bezier(.4,0,.2,1);
  border: 1px solid var(--border); position: relative; overflow: hidden;
}
.disease-card::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 3px;
  background: linear-gradient(90deg, var(--primary), var(--accent));
  transform: scaleX(0); transition: transform .3s ease;
}
.disease-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-lg); }
.disease-card:hover::before { transform: scaleX(1); }
.disease-card h4 { font-size: 18px; font-weight: 700; margin-bottom: 10px; }
.disease-symptoms { color: var(--text-secondary); font-size: 13px; line-height: 1.7; margin-bottom: 14px; }
.disease-tag { display: inline-block; background: linear-gradient(135deg, #dbeafe, #e0e7ff); color: var(--primary); padding: 4px 14px; border-radius: 50px; font-size: 12px; font-weight: 600; }

.contact { background: linear-gradient(180deg, rgba(37,99,235,.02), transparent); }
.section-subtitle { text-align: center; color: var(--text-secondary); font-size: 16px; margin-top: -36px; margin-bottom: 48px; }
.contact-grid { display: grid; grid-template-columns: 1fr 1.2fr; gap: 28px; max-width: 900px; margin: 0 auto; }
.info-card { padding: 36px; border: 1px solid var(--border); }
.info-card h3 { font-size: 20px; font-weight: 700; margin-bottom: 28px; }
.info-list { display: flex; flex-direction: column; gap: 24px; }
.info-item { display: flex; gap: 16px; align-items: flex-start; }
.info-icon { font-size: 28px; width: 44px; height: 44px; display: flex; align-items: center; justify-content: center; background: var(--bg); border-radius: 12px; flex-shrink: 0; }
.info-label { font-size: 12px; color: var(--text-light); margin-bottom: 2px; }
.info-value { font-size: 15px; font-weight: 600; color: var(--text); }
.form-card { padding: 36px; border: 1px solid var(--border); }
.form-card h3 { font-size: 20px; font-weight: 700; margin-bottom: 28px; }
.btn-full { width: 100%; justify-content: center; border-radius: 50px; }

.cta { text-align: center; }
.cta-card {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 40%, #172554 100%);
  border-radius: 28px; padding: 72px 48px; color: white;
  position: relative; overflow: hidden;
}
.cta-card::before {
  content: ''; position: absolute; top: -100px; right: -100px;
  width: 300px; height: 300px; border-radius: 50%;
  background: radial-gradient(circle, rgba(37,99,235,.2), transparent 70%);
  pointer-events: none;
}
.cta-card::after {
  content: ''; position: absolute; bottom: -80px; left: -80px;
  width: 240px; height: 240px; border-radius: 50%;
  background: radial-gradient(circle, rgba(6,182,212,.15), transparent 70%);
  pointer-events: none;
}
.cta-card > * { position: relative; z-index: 1; }
.cta-card h2 { font-size: 38px; font-weight: 800; margin-bottom: 16px; }
.cta-card p { color: #94a3b8; font-size: 17px; margin-bottom: 36px; max-width: 500px; margin-left: auto; margin-right: auto; }
.cta-disclaimer { font-size: 12px; color: #475569; margin-top: 28px; }

.app-footer { text-align: center; padding: 36px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); background: rgba(0,0,0,.01); }

@media (max-width: 768px) {
  .hero-content { grid-template-columns: 1fr; gap: 40px; }
  .hero-text h1 { font-size: 36px; }
  .hero-visual { display: none; }
  .features-grid { grid-template-columns: 1fr; }
  .disease-grid { grid-template-columns: 1fr; }
  .contact-grid { grid-template-columns: 1fr; }
  .hero-stats { gap: 32px; }
  .stat-item + .stat-item::before { left: -16px; }
  .cta-card { padding: 48px 24px; }
}
</style>
