<template>
  <div class="contact-page">
    <AppHeader />
    <div class="page-container section-padding">
      <div class="page-hero">
        <h1><span class="gradient-text">联系</span>我们</h1>
        <p>如有任何问题或合作意向，请随时与我们联系</p>
      </div>

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
          <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleSubmit">
            <el-form-item prop="name">
              <el-input v-model="form.name" placeholder="您的姓名" />
            </el-form-item>
            <el-form-item prop="phone">
              <el-input v-model="form.phone" placeholder="联系电话" />
            </el-form-item>
            <el-form-item prop="email">
              <el-input v-model="form.email" placeholder="电子邮箱" />
            </el-form-item>
            <el-form-item prop="content">
              <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入您的留言内容..." />
            </el-form-item>
            <el-form-item>
              <button class="btn-primary btn-full" :disabled="submitting">
                {{ submitting ? '提交中...' : '提交留言' }}
              </button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
    <footer class="app-footer"><p>&copy; 2026 VitaAI 智慧医院系统. All rights reserved.</p></footer>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import api from '@/api/index'
import { ElMessage } from 'element-plus'

const formRef = ref()
const submitting = ref(false)

const form = reactive({ name: '', phone: '', email: '', content: '' })
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  content: [{ required: true, message: '请输入留言内容', trigger: 'blur' }, { min: 5, message: '留言内容至少5个字', trigger: 'blur' }],
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await api.post('/contact-messages', { ...form })
    ElMessage.success('留言已提交，感谢您的反馈！')
    form.name = ''; form.phone = ''; form.email = ''; form.content = ''
  } catch {
    ElMessage.error('留言提交失败，请稍后重试')
  }
  finally { submitting.value = false }
}
</script>

<style scoped>
.contact-page { min-height: 100vh; display: flex; flex-direction: column; }
.page-hero { text-align: center; margin-bottom: 48px; }
.page-hero h1 { font-size: 38px; font-weight: 800; margin-bottom: 12px; letter-spacing: -0.5px; }
.page-hero p { color: var(--text-secondary); font-size: 16px; }

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
.form-card :deep(.el-input__wrapper) { border-radius: 12px !important; }
.form-card :deep(.el-textarea__inner) { border-radius: 12px !important; }

.app-footer { text-align: center; padding: 36px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }

@media (max-width: 768px) {
  .contact-grid { grid-template-columns: 1fr; }
}
</style>
