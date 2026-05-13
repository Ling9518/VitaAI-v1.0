<template>
  <div class="auth-page">
    <div class="auth-card">
      <router-link to="/" class="auth-logo">
        <span class="logo-icon">⚕️</span>
        <span class="logo-text gradient-text">VitaAI</span>
      </router-link>
      <h2>创建账号</h2>
      <p class="auth-sub">注册VitaAI，开启AI智能健康管理</p>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（3-20位）" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱地址" prefix-icon="Message" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（6-20位）" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" placeholder="邮箱验证码" prefix-icon="Key" />
            <el-button :disabled="sendingCode" class="captcha-btn" @click="sendCode">
              {{ captchaText }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <button class="btn-primary btn-full" :disabled="loading">
            {{ loading ? '注册中...' : '注册' }}
          </button>
        </el-form-item>
      </el-form>
      <p class="auth-switch">已有账号？<router-link to="/login">立即登录</router-link></p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/index'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const formRef = ref()

const form = reactive({
  username: '', email: '', password: '', confirmPassword: '', captchaCode: '',
})

const validateConfirm = (_rule: any, value: string, cb: any) => {
  if (value !== form.password) cb(new Error('两次密码输入不一致'))
  else cb()
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 20, message: '用户名长度3-20位', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

const captchaText = ref('获取验证码')

async function sendCode() {
  const emailValid = await formRef.value.validateField('email').catch(() => false)
  if (!emailValid && !form.email) { ElMessage.warning('请先填写邮箱'); return }
  sendingCode.value = true
  try {
    await api.post('/auth/captcha/send', { email: form.email, type: 'REGISTER' })
    ElMessage.success('验证码已发送')
    countdown.value = 60
    captchaText.value = `${countdown.value}s后重发`
    const timer = setInterval(() => {
      countdown.value--
      captchaText.value = `${countdown.value}s后重发`
      if (countdown.value <= 0) {
        clearInterval(timer)
        captchaText.value = '获取验证码'
        sendingCode.value = false
      }
    }, 1000)
  } catch {
    sendingCode.value = false
  }
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await api.post('/auth/register', { ...form })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 100%);
  padding: 24px;
}
.auth-card {
  background: white; border-radius: 24px; padding: 48px 40px;
  width: 100%; max-width: 440px; box-shadow: var(--shadow-lg); text-align: center;
}
.auth-logo { display: inline-flex; align-items: center; gap: 8px; margin-bottom: 24px; }
.logo-icon { font-size: 36px; }
.logo-text { font-size: 28px; font-weight: 800; }
.auth-card h2 { font-size: 24px; font-weight: 700; margin-bottom: 8px; }
.auth-sub { color: var(--text-secondary); margin-bottom: 32px; }
.btn-full { width: 100%; padding: 14px; font-size: 16px; justify-content: center; }
.captcha-row { display: flex; gap: 12px; width: 100%; }
.captcha-btn { white-space: nowrap; font-size: 13px; }
.auth-switch { margin-top: 24px; color: var(--text-secondary); font-size: 14px; }
</style>
