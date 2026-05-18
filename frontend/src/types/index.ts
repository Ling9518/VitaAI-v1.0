// ========== 用户 ==========
export interface UserInfo {
  id: number
  username: string
  email: string
  role: 'ADMIN' | 'DOCTOR' | 'USER'
  realName: string | null
  phone: string | null
  avatarUrl: string | null
  gender: 'MALE' | 'FEMALE' | 'OTHER' | null
  birthday: string | null
  doctorLicense: string | null
  doctorTitle: string | null
  doctorDept: string | null
  isVerified: boolean
  lastLoginAt: string | null
  createdAt: string
}

// ========== API 通用响应 ==========
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  timestamp: string
}

export interface Pagination {
  page: number
  pageSize: number
  total: number
  totalPages: number
}

export interface PageResponse<T> {
  list: T[]
  pagination: Pagination
}

// ========== 认证 ==========
export interface LoginData {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: UserInfo
}

// ========== 疾病/药品 ==========
export interface Disease {
  id: number
  name: string
  alias?: string
  icdCode?: string
  classification?: string
  bodySystem?: string
  severity?: 'MILD' | 'MODERATE' | 'SEVERE'
  isInfectious?: boolean
  isChronic?: boolean
  cause?: string
  symptoms?: string
  diagnosis?: string
  treatment?: string
  prevention?: string
  complications?: string
  viewsCount?: number
  favoritesCount?: number
  status?: 'PENDING' | 'APPROVED' | 'REJECTED'
  createdAt?: string
}

export interface Drug {
  id: number
  name: string
  genericName?: string
  brandName?: string
  drugType: 'PRESCRIPTION' | 'OTC' | 'HERBAL' | 'BIOLOGIC'
  form?: string
  specification?: string
  manufacturer?: string
  approvalNo?: string
  price?: number
  efficacy?: string
  usage2?: string
  dosage?: string
  sideEffect?: string
  contraindication?: string
  storage?: string
  viewsCount?: number
  favoritesCount?: number
  status?: 'PENDING' | 'APPROVED' | 'REJECTED'
  createdAt?: string
}

export interface FavoriteItem {
  id: number
  targetId: number
  targetType: 'DISEASE' | 'DRUG'
  name: string
  classification?: string
  drugType?: string
}

// ========== AI 诊断 ==========
export interface DiagnosisRecord {
  id: number
  conversationId: string
  symptomSummary: string | null
  conversationSummary?: string | null
  symptomsDetail?: string | null
  severityLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | null
  needsHospital: boolean | null
  advice: string | null
  feedbackAccuracy: 'ACCURATE' | 'MOSTLY_ACCURATE' | 'INACCURATE' | 'PENDING'
  feedbackDetail: string | null
  messageCount: number
  createdAt: string
  updatedAt: string | null
}

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  streaming?: boolean
}

export interface DiagnosisDetail extends DiagnosisRecord {
  messages: Array<{
    id: number
    senderType: 'USER' | 'AI'
    content: string
    createdAt: string
  }>
  aiAnalysis?: string | null
  suggestedDiseases?: string | null
  suggestedDrugs?: string | null
  warningText?: string | null
}

export interface DictItem {
  code: string
  name: string
}

export interface ChatResponse {
  conversationId: string
  isNewConversation: boolean
  messageId: number
  senderType: string
  content: string
  aiModel: string
  tokensUsed: number
  createdAt: string
}

// ========== 健康档案 ==========
export interface HealthRecord {
  id: number
  userId: number
  bloodType: 'A' | 'B' | 'AB' | 'O' | 'UNKNOWN'
  height: number
  weight: number
  medicalHistory: string | null
  allergyHistory: string | null
  medicationRecords: string | null
  familyHistory: string | null
  surgeryHistory: string | null
  lifestyle: string | null
  isComplete: boolean
  completenessRate: number
  lastCheckupDate: string | null
  createdAt: string
  updatedAt: string | null
}

// ========== 技能 ==========
export interface Skill {
  id: number
  name: string
  description: string
  content: string
  category: string
  keywords: string | null
  isActive: boolean
  priority: number
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  createdAt: string
  publishedAt: string | null
}
