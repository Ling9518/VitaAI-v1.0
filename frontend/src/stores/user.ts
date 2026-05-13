import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/index'

export interface UserInfo {
  id: number
  username: string
  email: string
  role: string
  avatarUrl: string | null
  realName: string | null
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const user = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isDoctor = computed(() => user.value?.role === 'DOCTOR' || user.value?.role === 'ADMIN')

  async function login(account: string, password: string) {
    const res = await api.post('/auth/login', { account, password })
    const data = res.data.data
    token.value = data.accessToken
    refreshToken.value = data.refreshToken
    user.value = data.user
    localStorage.setItem('token', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
  }

  async function fetchProfile() {
    if (!token.value) return
    try {
      const res = await api.get('/users/profile')
      user.value = res.data.data
    } catch { /* ignore fetch error, keep current user */ }
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  return { token, refreshToken, user, isLoggedIn, isAdmin, isDoctor, login, fetchProfile, logout }
})
