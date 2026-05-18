<template>
  <div class="auth-page">
    <div class="auth-card">
      <router-link to="/" class="auth-logo">
        <span class="logo-icon">⚕️</span>
        <span class="logo-text gradient-text">VitaAI</span>
      </router-link>
      <h2>欢迎回来</h2>
      <p class="auth-sub">登录您的账号，继续健康之旅</p>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleLogin">
        <el-form-item prop="account">
          <el-input v-model="form.account" placeholder="用户名或邮箱" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <button class="btn-primary btn-full" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </el-form-item>
      </el-form>
      <p class="auth-switch">还没有账号？<router-link to="/register">立即注册</router-link></p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref()

const form = reactive({ account: '', password: '' })
const rules = {
  account: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form.account, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    ElMessage.error('登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 50%, #ecfdf5 100%);
  padding: 24px; position: relative; overflow: hidden;
}
.auth-page::before {
  content: ''; position: absolute; top: -300px; right: -300px;
  width: 600px; height: 600px; border-radius: 50%;
  background: radial-gradient(circle, rgba(37,99,235,.04) 0%, transparent 70%);
  pointer-events: none;
}
.auth-page::after {
  content: ''; position: absolute; bottom: -200px; left: -200px;
  width: 400px; height: 400px; border-radius: 50%;
  background: radial-gradient(circle, rgba(6,182,212,.04) 0%, transparent 70%);
  pointer-events: none;
}
.auth-card {
  background: rgba(255,255,255,.9); backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 28px; padding: 52px 44px;
  width: 100%; max-width: 440px; box-shadow: var(--shadow-xl);
  text-align: center; position: relative; z-index: 1;
  border: 1px solid rgba(255,255,255,.8);
  animation: fadeInUp .5s ease-out;
}
.auth-logo { display: inline-flex; align-items: center; gap: 10px; margin-bottom: 28px; }
.logo-icon { font-size: 38px; transition: transform .3s ease; }
.auth-logo:hover .logo-icon { transform: scale(1.1); }
.logo-text { font-size: 30px; font-weight: 800; }
.auth-card h2 { font-size: 26px; font-weight: 700; margin-bottom: 8px; }
.auth-sub { color: var(--text-secondary); margin-bottom: 36px; font-size: 15px; }
.btn-full { width: 100%; padding: 14px; font-size: 16px; justify-content: center; border-radius: 50px; }
.btn-full:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(37,99,235,.35); }
.auth-switch { margin-top: 28px; color: var(--text-secondary); font-size: 14px; }
.auth-card :deep(.el-input__wrapper) { border-radius: 12px !important; padding: 4px 12px; }
.auth-card :deep(.el-form-item) { margin-bottom: 20px; }
</style>
