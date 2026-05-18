<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo">
        <span class="logo-icon">⚕️</span>
        <div class="logo-text-wrap">
          <span class="logo-text gradient-text">VitaAI</span>
          <span class="logo-sub">智慧医院</span>
        </div>
      </router-link>
      <nav class="nav-links">
        <template v-if="userStore.isLoggedIn">
          <router-link to="/ai-chat">AI诊断</router-link>
          <router-link to="/diseases">疾病库</router-link>
          <router-link to="/drugs">药品库</router-link>
          <router-link to="/favorites">收藏夹</router-link>
          <router-link to="/health">健康档案</router-link>
          <router-link to="/messages">在线问诊</router-link>
        </template>
        <a href="/#features" @click="goSection('features')">服务介绍</a>
        <a href="/#contact" @click="goSection('contact')">联系我们</a>
      </nav>
      <div class="header-actions">
        <template v-if="userStore.isLoggedIn">
          <el-dropdown trigger="click" popper-class="user-dropdown">
            <span class="user-avatar">
              <el-avatar :size="34" icon="UserFilled" />
              <span class="username">{{ userStore.user?.username }}</span>
              <span class="arrow-down">▾</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">
                  <span class="dd-icon">👤</span>个人资料
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/history')">
                  <span class="dd-icon">📝</span>诊断记录
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" @click="$router.push('/admin')">
                  <span class="dd-icon">⚙️</span>管理后台
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isDoctor" @click="$router.push('/doctor')">
                  <span class="dd-icon">🩺</span>医生后台
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <span class="dd-icon">🚪</span>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <button class="btn-login" @click="$router.push('/login')">登录</button>
          <button class="btn-register" @click="$router.push('/register')">注册</button>
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
function goSection(id: string) {
  if (router.currentRoute.value.path !== '/') {
    router.push('/#' + id)
  } else {
    const el = document.getElementById(id)
    if (el) el.scrollIntoView({ behavior: 'smooth' })
  }
}
</script>

<style scoped>
.app-header {
  position: sticky; top: 0; z-index: 1000;
  background: rgba(255,255,255,.78);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  border-bottom: 1px solid rgba(0,0,0,.06);
  transition: var(--transition);
}
.header-inner {
  max-width: 1200px; margin: 0 auto; padding: 0 24px;
  display: flex; align-items: center; justify-content: space-between; height: 64px;
}
.logo { display: flex; align-items: center; gap: 10px; font-weight: 700; }
.logo-icon { font-size: 30px; transition: var(--transition); }
.logo:hover .logo-icon { transform: scale(1.1); }
.logo-text-wrap { display: flex; flex-direction: column; }
.logo-text { font-size: 22px; line-height: 1.1; }
.logo-sub { font-size: 11px; color: var(--text-light); letter-spacing: 1px; }

.nav-links { display: flex; gap: 4px; }
.nav-links a {
  position: relative; color: var(--text-secondary); font-weight: 500; font-size: 14px;
  padding: 8px 16px; border-radius: 10px; transition: var(--transition-fast);
  display: flex; align-items: center;
}
.nav-links a:hover { color: var(--primary); background: rgba(37,99,235,.06); }
.nav-links a.router-link-active { color: var(--primary); background: rgba(37,99,235,.08); font-weight: 600; }
.nav-links a::after {
  content: ''; position: absolute; bottom: 2px; left: 50%; transform: translateX(-50%) scaleX(0);
  width: 20px; height: 3px; border-radius: 2px;
  background: linear-gradient(135deg, var(--primary), var(--accent));
  transition: transform .25s ease;
}
.nav-links a.router-link-active::after { transform: translateX(-50%) scaleX(1); }

.header-actions { display: flex; align-items: center; gap: 12px; }
.user-avatar {
  display: flex; align-items: center; gap: 8px; cursor: pointer;
  padding: 4px 12px 4px 4px; border-radius: 50px; transition: var(--transition-fast);
}
.user-avatar:hover { background: rgba(0,0,0,.04); }
.username { font-size: 14px; font-weight: 500; }
.arrow-down { font-size: 10px; color: var(--text-light); margin-left: 2px; }

.btn-login {
  background: transparent; color: var(--text); border: 1px solid var(--border);
  padding: 8px 22px; border-radius: 50px; font-weight: 600; font-size: 14px;
  cursor: pointer; transition: var(--transition);
}
.btn-login:hover { border-color: var(--primary); color: var(--primary); }
.btn-register {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: white; border: none; padding: 8px 22px; border-radius: 50px;
  font-weight: 600; font-size: 14px; cursor: pointer; transition: var(--transition);
  box-shadow: 0 2px 8px rgba(37,99,235,.3);
}
.btn-register:hover { transform: translateY(-1px); box-shadow: 0 4px 14px rgba(37,99,235,.4); }

@media (max-width: 768px) { .nav-links { display: none; } }
</style>

<style>
.user-dropdown { border-radius: 14px !important; overflow: hidden; box-shadow: 0 8px 32px rgba(0,0,0,.12) !important; border: 1px solid rgba(0,0,0,.04) !important; }
.user-dropdown .el-dropdown-menu__item { padding: 10px 18px; font-size: 14px; transition: all .15s ease; }
.user-dropdown .el-dropdown-menu__item:hover { background: #f0f6ff !important; color: var(--primary) !important; }
.user-dropdown .el-dropdown-menu__item.is-divided { border-top: 1px solid var(--border); margin-top: 4px; padding-top: 12px; }
.dd-icon { margin-right: 6px; }
</style>
