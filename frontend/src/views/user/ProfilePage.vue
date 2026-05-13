<template>
  <div class="profile-page">
    <AppHeader />
    <div class="page-container section-padding">
      <div class="page-hero">
        <h1>个人<span class="gradient-text">资料</span></h1>
      </div>

      <div class="profile-grid">
        <div class="card profile-card">
          <div class="avatar-section">
            <el-avatar :size="80" icon="UserFilled" />
            <h3>{{ profile.username }}</h3>
            <span class="role-tag" :class="profile.role?.toLowerCase()">{{ roleMap[profile.role] }}</span>
          </div>
          <div class="info-list">
            <div class="info-item"><span class="label">邮箱</span><span>{{ profile.email }}</span></div>
            <div class="info-item"><span class="label">真实姓名</span><span>{{ profile.realName || '未填写' }}</span></div>
            <div class="info-item"><span class="label">注册时间</span><span>{{ formatDate(profile.createdAt) }}</span></div>
          </div>
          <button class="btn-delete-account" @click="handleDeleteAccount" :disabled="deleting">{{ deleting ? '注销中...' : '注销账号' }}</button>
          <p class="delete-hint">注销后账号及所有数据将被永久删除</p>
        </div>

        <div class="card form-card">
          <h3>编辑资料</h3>
          <el-form :model="editForm" label-width="90px" size="large">
            <el-form-item label="真实姓名">
              <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="editForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="性别">
              <el-select v-model="editForm.gender" placeholder="请选择" clearable>
                <el-option label="男" value="MALE" />
                <el-option label="女" value="FEMALE" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <button class="btn-primary" @click="handleUpdateProfile" :disabled="saving">{{ saving ? '保存中...' : '保存修改' }}</button>
            </el-form-item>
          </el-form>
        </div>

        <div class="card form-card">
          <h3>修改密码</h3>
          <el-form :model="pwdForm" label-width="90px" size="large">
            <el-form-item label="当前密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <button class="btn-primary" @click="handleChangePwd" :disabled="changingPwd">{{ changingPwd ? '修改中...' : '修改密码' }}</button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
    <footer class="app-footer"><p>&copy; 2026 VitaAI 智慧医院系统. All rights reserved.</p></footer>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import { useUserStore } from '@/stores/user'
import api from '@/api/index'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const profile = ref<any>({})
const saving = ref(false)
const changingPwd = ref(false)
const deleting = ref(false)

const roleMap: Record<string, string> = { ADMIN: '管理员', DOCTOR: '医生', USER: '用户', VISITOR: '访客' }

const editForm = reactive({ realName: '', email: '', gender: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

function formatDate(date: string) {
  return date ? new Date(date).toLocaleDateString('zh-CN') : ''
}

onMounted(async () => {
  try {
    const res = await api.get('/users/profile')
    profile.value = res.data.data
    editForm.realName = profile.value.realName || ''
    editForm.email = profile.value.email || ''
    editForm.gender = profile.value.gender || ''
  } catch { /* empty */ }
})

async function handleUpdateProfile() {
  saving.value = true
  try {
    await api.put('/users/profile', { ...editForm })
    ElMessage.success('资料更新成功')
    await userStore.fetchProfile()
  } catch { /* handled by interceptor */ }
  finally { saving.value = false }
}

async function handleChangePwd() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  changingPwd.value = true
  try {
    await api.put('/users/password', { ...pwdForm })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch { /* handled by interceptor */ }
  finally { changingPwd.value = false }
}

async function handleDeleteAccount() {
  try {
    await ElMessageBox.confirm(
      '注销后账号及所有关联数据将被永久删除，且不可恢复。确定继续？',
      '确认注销账号',
      { confirmButtonText: '确定注销', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  deleting.value = true
  try {
    await api.delete('/users/account')
    ElMessage.success('账号已注销')
    userStore.logout()
    router.push('/login')
  } catch { /* handled by interceptor */ }
  finally { deleting.value = false }
}
</script>

<style scoped>
.profile-page { min-height: 100vh; display: flex; flex-direction: column; }
.page-hero { text-align: center; margin-bottom: 40px; }
.page-hero h1 { font-size: 36px; font-weight: 800; }

.profile-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }

.profile-card { text-align: center; padding: 32px; }
.avatar-section { margin-bottom: 24px; }
.avatar-section h3 { font-size: 20px; font-weight: 700; margin: 12px 0 8px; }
.role-tag { display: inline-block; padding: 4px 14px; border-radius: 50px; font-size: 13px; font-weight: 600; }
.role-tag.user { background: #dbeafe; color: var(--primary); }
.role-tag.doctor { background: #d1fae5; color: #065f46; }
.role-tag.admin { background: #ede9fe; color: #6b21a8; }
.role-tag.visitor { background: #f1f5f9; color: var(--text-secondary); }

.info-list { text-align: left; display: flex; flex-direction: column; gap: 12px; }
.info-item { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--border); }
.info-item .label { color: var(--text-light); font-size: 13px; }

.form-card { padding: 28px 32px; }
.btn-delete-account { width: 100%; margin-top: 20px; padding: 10px; background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; border-radius: 8px; cursor: pointer; font-size: 14px; font-weight: 600; }
.btn-delete-account:hover { background: #fecaca; }
.btn-delete-account:disabled { opacity: 0.6; cursor: not-allowed; }
.delete-hint { margin-top: 8px; font-size: 12px; color: #999; }
.form-card h3 { font-size: 18px; font-weight: 700; margin-bottom: 20px; }
.app-footer { text-align: center; padding: 32px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }

@media (max-width: 768px) {
  .profile-grid { grid-template-columns: 1fr; }
}
</style>
