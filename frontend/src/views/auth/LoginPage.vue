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
.auth-switch { margin-top: 24px; color: var(--text-secondary); font-size: 14px; }
</style>
