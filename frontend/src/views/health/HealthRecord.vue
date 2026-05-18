<template>
  <div class="health-page">
    <AppHeader />
    <div class="page-container section-padding">
      <div class="page-hero">
        <h1>健康<span class="gradient-text">档案</span></h1>
        <p>建立个人健康档案，让AI诊断更精准</p>
      </div>

      <div v-if="loading" class="loading-state"><el-icon class="is-loading"><Loading /></el-icon> 加载中...</div>
      <div v-else class="record-grid">
        <div class="card form-card">
          <h3>基础信息</h3>
          <el-form :model="form" label-width="100px" size="large">
            <el-form-item label="血型">
              <el-select v-model="form.bloodType" placeholder="请选择" clearable>
                <el-option v-for="b in bloodTypes" :key="b.value" :label="b.label" :value="b.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="身高 (cm)" :rules="heightRules" prop="height">
              <el-input-number v-model="form.height" :min="50" :max="250" :step="0.1" controls-position="right" />
            </el-form-item>
            <el-form-item label="体重 (kg)" :rules="weightRules" prop="weight">
              <el-input-number v-model="form.weight" :min="10" :max="300" :step="0.1" controls-position="right" />
            </el-form-item>
            <el-form-item label="最后体检">
              <el-date-picker v-model="form.lastCheckupDate" type="date" placeholder="选择日期" />
            </el-form-item>
          </el-form>
        </div>

        <div class="card form-card">
          <h3>病史信息</h3>
          <el-form :model="form" label-width="100px" size="large">
            <el-form-item label="既往病史">
              <el-input v-model="form.medicalHistory" type="textarea" :rows="3" placeholder="如：高血压、糖尿病..." />
            </el-form-item>
            <el-form-item label="过敏史">
              <el-input v-model="form.allergyHistory" type="textarea" :rows="3" placeholder="如：青霉素过敏、花粉过敏..." />
            </el-form-item>
            <el-form-item label="用药记录">
              <el-input v-model="form.medicationRecords" type="textarea" :rows="3" placeholder="目前正在服用的药物..." />
            </el-form-item>
          </el-form>
        </div>

        <div class="card form-card">
          <h3>其他信息</h3>
          <el-form :model="form" label-width="100px" size="large">
            <el-form-item label="家族病史">
              <el-input v-model="form.familyHistory" type="textarea" :rows="3" placeholder="直系亲属的重大疾病史..." />
            </el-form-item>
            <el-form-item label="手术史">
              <el-input v-model="form.surgeryHistory" type="textarea" :rows="3" placeholder="曾做过的手术..." />
            </el-form-item>
            <el-form-item label="生活习惯">
              <el-input v-model="form.lifestyle" type="textarea" :rows="3" placeholder="吸烟、饮酒、运动习惯等..." />
            </el-form-item>
          </el-form>
        </div>
      </div>

      <div class="completion-bar" v-if="record">
        <div class="completion-header">
          <span>档案完整度</span>
          <span>{{ Math.round((record.completenessRate || 0)) }}%</span>
        </div>
        <el-progress :percentage="Math.round((record.completenessRate || 0))" :stroke-width="10" color="#10b981" />
      </div>

      <div class="form-actions">
        <button class="btn-primary" @click="handleSave" :disabled="saving">{{ saving ? '保存中...' : '保存健康档案' }}</button>
      </div>

      <div class="detail-disclaimer">
        <p>您的健康档案将用于AI诊断参考，所有数据均加密存储，保障隐私安全。</p>
      </div>
    </div>
    <footer class="app-footer"><p>&copy; 2026 VitaAI 智慧医院系统. All rights reserved.</p></footer>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import api from '@/api/index'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import type { HealthRecord } from '@/types'
import { bloodTypes } from '@/utils'

const record = ref<HealthRecord | null>(null)
const loading = ref(false)
const saving = ref(false)

const heightRules = [
  { required: true, message: '请输入身高', trigger: 'blur' },
  { type: 'number' as const, min: 50, max: 250, message: '身高范围 50-250cm', trigger: 'blur' },
]
const weightRules = [
  { required: true, message: '请输入体重', trigger: 'blur' },
  { type: 'number' as const, min: 10, max: 300, message: '体重范围 10-300kg', trigger: 'blur' },
]

const form = reactive({
  bloodType: '',
  height: null as number | null,
  weight: null as number | null,
  lastCheckupDate: null as string | null,
  medicalHistory: '',
  allergyHistory: '',
  medicationRecords: '',
  familyHistory: '',
  surgeryHistory: '',
  lifestyle: '',
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await api.get('/health-records')
    const data = res.data.data
    if (data) {
      record.value = data
      Object.assign(form, {
        bloodType: data.bloodType || '',
        height: data.height,
        weight: data.weight,
        lastCheckupDate: data.lastCheckupDate || null,
        medicalHistory: data.medicalHistory || '',
        allergyHistory: data.allergyHistory || '',
        medicationRecords: data.medicationRecords || '',
        familyHistory: data.familyHistory || '',
        surgeryHistory: data.surgeryHistory || '',
        lifestyle: data.lifestyle || '',
      })
    }
  } catch {
    ElMessage.error('加载健康档案失败')
  } finally {
    loading.value = false
  }
})

async function handleSave() {
  saving.value = true
  try {
    if (record.value?.id) {
      await api.put('/health-records', { ...form })
    } else {
      await api.post('/health-records', { ...form })
    }
    ElMessage.success('健康档案保存成功')
    // Reload to get updated completeness
    const res = await api.get('/health-records')
    record.value = res.data.data
  } catch {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.health-page { min-height: 100vh; display: flex; flex-direction: column; }
.loading-state { text-align: center; padding: 80px; color: var(--text-light); font-size: 15px; }
.page-hero { text-align: center; margin-bottom: 44px; }
.page-hero h1 { font-size: 38px; font-weight: 800; margin-bottom: 12px; letter-spacing: -0.5px; }
.page-hero p { color: var(--text-secondary); font-size: 16px; }

.record-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; margin-bottom: 36px; }
.form-card { padding: 32px 36px; border: 1px solid var(--border); transition: all .25s ease; }
.form-card:hover { border-color: rgba(37,99,235,.1); box-shadow: var(--shadow-md); }
.form-card h3 { font-size: 18px; font-weight: 700; margin-bottom: 24px; padding-bottom: 12px; border-bottom: 2px solid var(--primary); display: inline-block; }

.completion-bar { background: var(--bg-card); padding: 24px 36px; border-radius: var(--radius); margin-bottom: 36px; box-shadow: var(--shadow); border: 1px solid var(--border); }
.completion-header { display: flex; justify-content: space-between; margin-bottom: 14px; font-weight: 600; font-size: 15px; }

.form-actions { display: flex; justify-content: center; margin-bottom: 36px; }
.detail-disclaimer { background: linear-gradient(135deg, #eff6ff, #f0f9ff); padding: 14px 24px; border-radius: var(--radius-sm); text-align: center; font-size: 13px; color: var(--primary); margin-bottom: 48px; border: 1px solid #dbeafe; }
.app-footer { text-align: center; padding: 36px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }

@media (max-width: 768px) {
  .record-grid { grid-template-columns: 1fr; }
}
</style>
