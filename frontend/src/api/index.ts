import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 300_000,
  headers: { 'Content-Type': 'application/json' }
})

let isRefreshing = false
let refreshSubscribers: Array<(token: string) => void> = []

function subscribeTokenRefresh(cb: (token: string) => void) {
  refreshSubscribers.push(cb)
}

function onTokenRefreshed(token: string) {
  refreshSubscribers.forEach(cb => cb(token))
  refreshSubscribers = []
}

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
    return response
  },
  async error => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      const storedRefreshToken = localStorage.getItem('refreshToken')

      if (storedRefreshToken) {
        if (isRefreshing) {
          return new Promise(resolve => {
            subscribeTokenRefresh((token: string) => {
              originalRequest.headers.Authorization = `Bearer ${token}`
              resolve(api(originalRequest))
            })
          })
        }

        originalRequest._retry = true
        isRefreshing = true

        try {
          const res = await axios.post('/api/auth/refresh', { refreshToken: storedRefreshToken })
          const { accessToken, refreshToken: newRefreshToken } = res.data.data

          localStorage.setItem('token', accessToken)
          if (newRefreshToken) localStorage.setItem('refreshToken', newRefreshToken)

          onTokenRefreshed(accessToken)
          isRefreshing = false

          originalRequest.headers.Authorization = `Bearer ${accessToken}`
          return api(originalRequest)
        } catch {
          isRefreshing = false
          refreshSubscribers = []
          localStorage.removeItem('token')
          localStorage.removeItem('refreshToken')
          if (window.location.pathname !== '/login') {
            window.location.href = '/login'
          }
          return Promise.reject(error)
        }
      }

      // No refresh token, redirect to login
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(error)
    }

    if (error.response?.status === 403) {
      ElMessage.error('您没有权限执行此操作')
      return Promise.reject(error)
    }

    ElMessage.error(error.response?.data?.message || '网络错误')
    return Promise.reject(error)
  }
)

export default api
