<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo">
        <span class="logo-icon">⚕️</span>
        <span class="logo-text gradient-text">VitaAI</span>
        <span class="logo-sub">智慧医院</span>
      </router-link>
      <nav class="nav-links">
        <router-link to="/ai-chat">AI诊断</router-link>
        <router-link to="/diseases">疾病库</router-link>
        <router-link to="/drugs">药品库</router-link>
        <router-link to="/health">健康档案</router-link>
      </nav>
      <div class="header-actions">
        <template v-if="userStore.isLoggedIn">
          <el-dropdown trigger="click">
            <span class="user-avatar">
              <el-avatar :size="36" icon="UserFilled" />
              <span class="username">{{ userStore.user?.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">个人资料</el-dropdown-item>
                <el-dropdown-item @click="$router.push('/history')">诊断记录</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" @click="$router.push('/admin')">管理后台</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isDoctor" @click="$router.push('/doctor')">医生后台</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button class="btn-login" @click="$router.push('/login')">登录</el-button>
          <el-button class="btn-register" type="primary" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
const userStore = useUserStore()
const router = useRouter()
function handleLogout() { userStore.logout(); router.push('/') }
</script>

<style scoped>
.app-header {
  position: sticky; top: 0; z-index: 1000;
  background: rgba(255,255,255,.85); backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border);
}
.header-inner {
  max-width: 1200px; margin: 0 auto; padding: 0 24px;
  display: flex; align-items: center; justify-content: space-between; height: 64px;
}
.logo { display: flex; align-items: center; gap: 8px; font-weight: 700; }
.logo-icon { font-size: 28px; }
.logo-text { font-size: 24px; }
.logo-sub { font-size: 13px; color: var(--text-secondary); margin-left: 4px; }
.nav-links { display: flex; gap: 32px; }
.nav-links a { color: var(--text-secondary); font-weight: 500; font-size: 15px; transition: var(--transition); }
.nav-links a:hover, .nav-links a.router-link-active { color: var(--primary); }
.header-actions { display: flex; align-items: center; gap: 12px; }
.user-avatar { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.username { font-size: 14px; font-weight: 500; }
.btn-login { border-radius: 50px; font-weight: 600; }
.btn-register { border-radius: 50px; font-weight: 600; }
@media (max-width: 768px) { .nav-links { display: none; } }
</style>
