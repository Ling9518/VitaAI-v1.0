import { ref } from 'vue'

/**
 * 截断文本，超出长度添加省略号
 */
export function truncateText(text: string | null, len: number): string {
  if (!text) return ''
  return text.length > len ? text.substring(0, len) + '...' : text
}

/**
 * 格式化日期为中文格式
 */
export function formatDate(date: string | null): string {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'long', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

/**
 * 格式化短日期（月/日 时:分）
 */
export function formatShortDate(date: string | null): string {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', {
    month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

/** 严重程度映射 */
export const severityMap: Record<string, string> = {
  LOW: '低风险', MEDIUM: '中等风险', HIGH: '高风险', CRITICAL: '危险',
}

/** 反馈准确度映射 */
export const feedbackMap: Record<string, string> = {
  ACCURATE: '准确', MOSTLY_ACCURATE: '大致准确', INACCURATE: '不准确', PENDING: '待评价',
}

/** 血型选项 */
export const bloodTypes = [
  { label: 'A型', value: 'A' }, { label: 'B型', value: 'B' },
  { label: 'AB型', value: 'AB' }, { label: 'O型', value: 'O' },
  { label: '未知', value: 'UNKNOWN' },
]

/** 通用加载状态管理 */
export function useLoading() {
  const loading = ref(false)
  const error = ref('')
  return { loading, error }
}
