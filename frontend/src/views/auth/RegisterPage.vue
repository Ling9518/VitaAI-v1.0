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
        <!-- 角色选择 -->
        <el-form-item>
          <div class="role-toggle">
            <button type="button" class="role-btn" :class="{ active: form.role === 'USER' }" @click="switchRole('USER')">
              <span class="role-icon">👤</span>
              <span>普通用户</span>
            </button>
            <button type="button" class="role-btn" :class="{ active: form.role === 'DOCTOR' }" @click="switchRole('DOCTOR')">
              <span class="role-icon">🩺</span>
              <span>医生</span>
            </button>
          </div>
        </el-form-item>
        <!-- 医生专属字段 -->
        <template v-if="form.role === 'DOCTOR'">
          <el-form-item prop="realName">
            <el-input v-model="form.realName" placeholder="真实姓名" prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="doctorLicense">
            <el-input v-model="form.doctorLicense" placeholder="执业证号" prefix-icon="Document" />
          </el-form-item>
          <el-form-item prop="doctorDept">
            <el-select v-model="form.doctorDept" placeholder="选择科室" style="width: 100%">
              <el-option label="内科" value="内科" />
              <el-option label="外科" value="外科" />
              <el-option label="儿科" value="儿科" />
              <el-option label="妇产科" value="妇产科" />
              <el-option label="骨科" value="骨科" />
              <el-option label="眼科" value="眼科" />
              <el-option label="耳鼻喉科" value="耳鼻喉科" />
              <el-option label="皮肤科" value="皮肤科" />
              <el-option label="神经内科" value="神经内科" />
              <el-option label="心血管内科" value="心血管内科" />
              <el-option label="消化内科" value="消化内科" />
              <el-option label="呼吸内科" value="呼吸内科" />
              <el-option label="内分泌科" value="内分泌科" />
              <el-option label="泌尿外科" value="泌尿外科" />
              <el-option label="急诊科" value="急诊科" />
              <el-option label="中医科" value="中医科" />
            </el-select>
          </el-form-item>
          <el-form-item prop="doctorTitle">
            <el-select v-model="form.doctorTitle" placeholder="选择职称" style="width: 100%">
              <el-option label="主任医师" value="主任医师" />
              <el-option label="副主任医师" value="副主任医师" />
              <el-option label="主治医师" value="主治医师" />
              <el-option label="住院医师" value="住院医师" />
              <el-option label="实习医师" value="实习医师" />
            </el-select>
          </el-form-item>
        </template>
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
  role: 'USER', realName: '', doctorLicense: '', doctorDept: '', doctorTitle: '',
})

function switchRole(role: string) {
  form.role = role
  formRef.value?.clearValidate()
}

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
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  doctorLicense: [{ required: true, message: '请输入执业证号', trigger: 'blur' }],
  doctorDept: [{ required: true, message: '请选择科室', trigger: 'change' }],
  doctorTitle: [{ required: true, message: '请选择职称', trigger: 'change' }],
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
    ElMessage.error('验证码发送失败，请稍后重试')
    sendingCode.value = false
  }
}

async function handleRegister() {
  // 只校验当前角色的必填字段
  const fields = ['username', 'email', 'password', 'confirmPassword', 'captchaCode'] as string[]
  if (form.role === 'DOCTOR') {
    fields.push('realName', 'doctorLicense', 'doctorDept', 'doctorTitle')
  }
  const valid = await formRef.value.validateField(fields).catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const payload: Record<string, string> = {
      username: form.username, email: form.email,
      password: form.password, confirmPassword: form.confirmPassword,
      captchaCode: form.captchaCode, role: form.role,
    }
    if (form.role === 'DOCTOR') {
      payload.realName = form.realName
      payload.doctorLicense = form.doctorLicense
      payload.doctorDept = form.doctorDept
      payload.doctorTitle = form.doctorTitle
    }
    await api.post('/auth/register', payload)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    ElMessage.error('注册失败，请稍后重试')
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
  border-radius: 28px; padding: 44px 40px;
  width: 100%; max-width: 460px; box-shadow: var(--shadow-xl);
  text-align: center; position: relative; z-index: 1;
  border: 1px solid rgba(255,255,255,.8);
  animation: fadeInUp .5s ease-out;
}
.auth-logo { display: inline-flex; align-items: center; gap: 10px; margin-bottom: 24px; }
.logo-icon { font-size: 38px; transition: transform .3s ease; }
.auth-logo:hover .logo-icon { transform: scale(1.1); }
.logo-text { font-size: 30px; font-weight: 800; }
.auth-card h2 { font-size: 26px; font-weight: 700; margin-bottom: 8px; }
.auth-sub { color: var(--text-secondary); margin-bottom: 32px; font-size: 15px; }
.btn-full { width: 100%; padding: 14px; font-size: 16px; justify-content: center; border-radius: 50px; }
.btn-full:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(37,99,235,.35); }
.captcha-row { display: flex; gap: 12px; width: 100%; }
.captcha-btn { white-space: nowrap; font-size: 13px; border-radius: 12px; }
.role-toggle { display: flex; gap: 12px; width: 100%; }
.role-btn {
  flex: 1; display: flex; align-items: center; justify-content: center; gap: 8px;
  padding: 14px; border: 2px solid var(--border); border-radius: 14px;
  background: var(--bg-card); cursor: pointer; font-size: 15px; font-weight: 600;
  transition: all .25s ease; color: var(--text-secondary);
}
.role-btn:hover { border-color: var(--primary); color: var(--primary); transform: translateY(-1px); }
.role-btn.active { border-color: var(--primary); background: linear-gradient(135deg, #eff6ff, #eef2ff); color: var(--primary); box-shadow: 0 4px 12px rgba(37,99,235,.1); }
.role-icon { font-size: 20px; }
.auth-switch { margin-top: 28px; color: var(--text-secondary); font-size: 14px; }
.auth-card :deep(.el-input__wrapper) { border-radius: 12px !important; padding: 4px 12px; }
.auth-card :deep(.el-form-item) { margin-bottom: 18px; }
.auth-card :deep(.el-select .el-input__wrapper) { border-radius: 12px !important; }
</style>
