import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('@/views/home/HomePage.vue') },
    { path: '/login', component: () => import('@/views/auth/LoginPage.vue') },
    { path: '/register', component: () => import('@/views/auth/RegisterPage.vue') },
    { path: '/ai-chat', component: () => import('@/views/ai/ChatPage.vue'), meta: { requiresAuth: true } },
    { path: '/diseases', component: () => import('@/views/disease/DiseaseList.vue') },
    { path: '/diseases/:id', component: () => import('@/views/disease/DiseaseDetail.vue') },
    { path: '/drugs', component: () => import('@/views/drug/DrugList.vue') },
    { path: '/drugs/:id', component: () => import('@/views/drug/DrugDetail.vue') },
    { path: '/health', component: () => import('@/views/health/HealthRecord.vue'), meta: { requiresAuth: true } },
    { path: '/profile', component: () => import('@/views/user/ProfilePage.vue'), meta: { requiresAuth: true } },
    { path: '/history', component: () => import('@/views/ai/HistoryPage.vue'), meta: { requiresAuth: true } },
    { path: '/admin', component: () => import('@/views/admin/AdminDashboard.vue'), meta: { requiresAuth: true, role: 'ADMIN' } },
    { path: '/doctor', component: () => import('@/views/doctor/DoctorDashboard.vue'), meta: { requiresAuth: true, role: 'DOCTOR' } },
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
  if (to.meta.role && userStore.user?.role !== to.meta.role) {
    next('/')
  } else {
    next()
  }
})

export default router
