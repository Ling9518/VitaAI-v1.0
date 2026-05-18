import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('@/views/home/HomePage.vue') },
    { path: '/login', component: () => import('@/views/auth/LoginPage.vue') },
    { path: '/register', component: () => import('@/views/auth/RegisterPage.vue') },
    // AI诊断
    { path: '/ai/chat', component: () => import('@/views/ai/ChatPage.vue'), alias: '/ai-chat', meta: { requiresAuth: true } },
    { path: '/ai/history', component: () => import('@/views/ai/HistoryPage.vue'), alias: '/history', meta: { requiresAuth: true } },
    // 疾病药品
    { path: '/diseases', component: () => import('@/views/disease/DiseaseList.vue') },
    { path: '/diseases/:id', component: () => import('@/views/disease/DiseaseDetail.vue') },
    { path: '/drugs', component: () => import('@/views/drug/DrugList.vue') },
    { path: '/drugs/:id', component: () => import('@/views/drug/DrugDetail.vue') },
    // 用户
    { path: '/user/profile', component: () => import('@/views/user/ProfilePage.vue'), alias: '/profile', meta: { requiresAuth: true } },
    { path: '/user/favorites', component: () => import('@/views/user/FavoritesPage.vue'), alias: '/favorites', meta: { requiresAuth: true } },
    { path: '/user/messages', component: () => import('@/views/user/MessagesPage.vue'), alias: '/messages', meta: { requiresAuth: true } },
    // 健康档案
    { path: '/health/record', component: () => import('@/views/health/HealthRecord.vue'), alias: '/health', meta: { requiresAuth: true } },
    // 医生
    { path: '/doctor/dashboard', component: () => import('@/views/doctor/DoctorDashboard.vue'), alias: '/doctor', meta: { requiresAuth: true, role: 'DOCTOR' } },
    // 管理员
    { path: '/admin/dashboard', component: () => import('@/views/admin/AdminDashboard.vue'), alias: '/admin', meta: { requiresAuth: true, role: 'ADMIN' } },
    // 404
    { path: '/:pathMatch(.*)*', component: () => import('@/views/home/NotFoundPage.vue') },
  ]
})

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
    return
  }
  // Fetch user profile if logged in but user info not loaded yet
  if (userStore.token && !userStore.user) {
    try {
      await userStore.fetchProfile()
    } catch {
      userStore.logout()
      next('/login')
      return
    }
  }
  if (to.meta.role) {
    const userRole = userStore.user?.role
    // ADMIN can access all pages
    if (userRole !== 'ADMIN' && userRole !== to.meta.role) {
      next('/')
      return
    }
  }
  next()
})

export default router
