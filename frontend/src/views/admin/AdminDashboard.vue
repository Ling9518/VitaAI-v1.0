<template>
  <div class="admin-page">
    <AppHeader />
    <div class="page-container section-padding">
      <div class="page-hero">
        <h1>管理<span class="gradient-text">后台</span></h1>
        <p>系统管理、用户管理和内容审核</p>
      </div>

      <!-- Stats cards -->
      <div class="stats-grid">
        <div class="stat-card card">
          <span class="stat-icon">👥</span>
          <div>
            <div class="stat-num">{{ stats.totalUsers }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </div>
        <div class="stat-card card">
          <span class="stat-icon">🩺</span>
          <div>
            <div class="stat-num">{{ stats.totalDoctors }}</div>
            <div class="stat-label">医生数</div>
          </div>
        </div>
        <div class="stat-card card">
          <span class="stat-icon">🦠</span>
          <div>
            <div class="stat-num">{{ stats.totalDiseases }}</div>
            <div class="stat-label">疾病数</div>
          </div>
        </div>
        <div class="stat-card card">
          <span class="stat-icon">💊</span>
          <div>
            <div class="stat-num">{{ stats.totalDrugs }}</div>
            <div class="stat-label">药品数</div>
          </div>
        </div>
        <div class="stat-card card">
          <span class="stat-icon">📋</span>
          <div>
            <div class="stat-num">{{ stats.totalDiagnoses }}</div>
            <div class="stat-label">诊断数</div>
          </div>
        </div>
        <div class="stat-card card highlight">
          <span class="stat-icon">⏳</span>
          <div>
            <div class="stat-num">{{ stats.pendingReviews }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <el-tabs v-model="activeTab" type="border-card">
        <!-- User Management -->
        <el-tab-pane label="用户管理" name="users">
          <div class="tab-header">
            <el-select v-model="userRoleFilter" placeholder="角色筛选" clearable @change="fetchUsers" style="width: 160px;">
              <el-option label="全部用户" value="" />
              <el-option label="管理员" value="ADMIN" />
              <el-option label="医生" value="DOCTOR" />
              <el-option label="用户" value="USER" />
            </el-select>
          </div>
          <el-table :data="users" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="email" label="邮箱" width="200" />
            <el-table-column prop="realName" label="真实姓名" width="100" />
            <el-table-column label="角色" width="90">
              <template #default="{ row }">
                <el-tag :type="roleType(row.role)" size="small">{{ roleMap[row.role] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.isDisabled ? 'danger' : 'success'" size="small">{{ row.isDisabled ? '已禁用' : '正常' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="最后登录" width="160">
              <template #default="{ row }">{{ formatDate(row.lastLoginAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" @click="toggleUser(row)">{{ row.isDisabled ? '启用' : '禁用' }}</el-button>
                <el-button size="small" v-if="row.role !== 'ADMIN'" @click="changeRole(row)">改角色</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="tab-pager" v-if="userTotal > 20">
            <el-pagination v-model:current-page="userPage" :page-size="20" :total="userTotal" layout="prev, pager, next" @current-change="fetchUsers" />
          </div>
        </el-tab-pane>

        <!-- Content Review -->
        <el-tab-pane label="内容审核" name="content">
          <div class="review-section">
            <h3>待审核疾病</h3>
            <el-table :data="pendingDiseases" stripe style="width: 100%">
              <el-table-column prop="name" label="名称" width="160" />
              <el-table-column prop="classification" label="分类" width="120" />
              <el-table-column label="症状" min-width="200">
                <template #default="{ row }">{{ truncateText(row.symptoms, 60) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button size="small" type="success" @click="reviewDisease(row, 'APPROVED')">通过</el-button>
                  <el-button size="small" type="danger" @click="reviewDisease(row, 'REJECTED')">拒绝</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!pendingDiseases.length" description="无待审核疾病" :image-size="40" />
          </div>
          <div class="review-section">
            <h3>待审核药品</h3>
            <el-table :data="pendingDrugs" stripe style="width: 100%">
              <el-table-column prop="name" label="名称" width="160" />
              <el-table-column prop="drugType" label="类型" width="100" />
              <el-table-column label="功效" min-width="200">
                <template #default="{ row }">{{ truncateText(row.efficacy, 60) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button size="small" type="success" @click="reviewDrug(row, 'APPROVED')">通过</el-button>
                  <el-button size="small" type="danger" @click="reviewDrug(row, 'REJECTED')">拒绝</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!pendingDrugs.length" description="无待审核药品" :image-size="40" />
          </div>
        </el-tab-pane>

        <!-- Audit Logs -->
        <el-tab-pane label="审计日志" name="audit">
          <el-table :data="auditLogs" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="operation" label="操作" width="140" />
            <el-table-column prop="module" label="模块" width="120" />
            <el-table-column prop="id" label="日志ID" width="80" />
            <el-table-column prop="operationDesc" label="详情" min-width="200">
              <template #default="{ row }">{{ truncateText(row.operationDesc, 80) }}</template>
            </el-table-column>
            <el-table-column label="时间" width="160">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Online Consultation Messages -->
        <el-tab-pane label="在线问诊" name="messages">
          <div class="tab-header">
            <el-radio-group v-model="adminMsgFilter" @change="fetchAdminMessages">
              <el-radio-button label="UNRESOLVED">未解决 ({{ adminMsgStats.unresolved }})</el-radio-button>
              <el-radio-button label="RESOLVED">已解决 ({{ adminMsgStats.resolved }})</el-radio-button>
            </el-radio-group>
          </div>
          <div class="message-cards" v-if="adminMsgList.length">
            <div class="msg-card card" v-for="m in adminMsgList" :key="m.id" :class="{ resolved: m.status === 'RESOLVED' }">
              <div class="msg-header">
                <div>
                  <span class="msg-user">{{ m.user?.username }}</span>
                  <span class="msg-date">{{ formatDate(m.createdAt) }}</span>
                </div>
                <div class="msg-header-right">
                  <el-button
                    v-if="m.status === 'UNRESOLVED'"
                    size="small"
                    type="warning"
                    @click="markAdminStatus(m.id, 'RESOLVED')"
                  >标记已解决</el-button>
                  <el-button
                    v-else
                    size="small"
                    type="info"
                    @click="markAdminStatus(m.id, 'UNRESOLVED')"
                  >标记未解决</el-button>
                </div>
              </div>

              <!-- User message content -->
              <div class="msg-content" v-if="adminEditingMsgId !== m.id">{{ m.content }}</div>
              <div class="admin-edit-area" v-else>
                <el-input v-model="adminEditMsgContent" type="textarea" :rows="3" resize="none" />
                <div class="admin-edit-actions">
                  <el-button size="small" @click="cancelAdminEditMsg">取消</el-button>
                  <el-button size="small" type="primary" :disabled="!adminEditMsgContent.trim()" :loading="adminSavingMsgId === m.id" @click="saveAdminEditMsg(m.id)">保存</el-button>
                </div>
              </div>
              <div class="msg-edit-btn" v-if="adminEditingMsgId !== m.id">
                <el-button size="small" text @click="startAdminEditMsg(m)">编辑用户留言</el-button>
              </div>

              <!-- Existing reply (not editing) -->
              <div class="msg-reply-section" v-if="m.reply && adminEditingReplyId !== m.id">
                <div class="reply-label">回复内容：</div>
                <div class="reply-text">{{ m.reply }}</div>
                <div class="reply-date" v-if="m.repliedAt">回复于 {{ formatDate(m.repliedAt) }}</div>
                <div class="reply-actions">
                  <el-button size="small" text type="primary" @click="startAdminEditReply(m)">编辑回复</el-button>
                  <el-button size="small" text type="danger" @click="handleAdminWithdrawReply(m.id)">撤回回复</el-button>
                </div>
              </div>

              <!-- Editing reply mode -->
              <div class="edit-reply-area" v-if="adminEditingReplyId === m.id">
                <el-input v-model="adminEditReplyContent" type="textarea" :rows="3" placeholder="编辑回复内容..." resize="none" />
                <div class="edit-reply-actions">
                  <el-button size="small" @click="cancelAdminEditReply">取消</el-button>
                  <el-button size="small" type="primary" :disabled="!adminEditReplyContent.trim()" :loading="adminReplyingId === m.id" @click="saveAdminEditReply(m.id)">保存修改</el-button>
                </div>
              </div>

              <!-- No reply yet: show reply input -->
              <div class="msg-action-bar" v-if="!m.reply">
                <div class="reply-input-wrap">
                  <el-input v-model="adminReplyTexts[m.id]" type="textarea" :rows="2" placeholder="输入回复内容..." resize="none" />
                  <el-button type="primary" :disabled="!adminReplyTexts[m.id]?.trim()" :loading="adminReplyingId === m.id" @click="handleAdminReply(m.id)">保存回复</el-button>
                </div>
                <el-button type="success" :icon="ChatDotRound" @click="goAdminDiagnose(m)">AI辅助诊断</el-button>
              </div>

              <!-- Has reply: still show AI button -->
              <div class="msg-action-bar" v-if="m.reply && adminEditingReplyId !== m.id">
                <el-button type="success" :icon="ChatDotRound" @click="goAdminDiagnose(m)">AI辅助诊断</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无留言" :image-size="60" />
        </el-tab-pane>

        <!-- Disease Management -->
        <el-tab-pane label="疾病管理" name="diseaseMgmt">
          <div class="tab-header">
            <el-input v-model="diseaseSearch" placeholder="搜索疾病名称..." clearable @clear="fetchAdminDiseases" @keyup.enter="fetchAdminDiseases" style="width: 240px;" />
            <el-select v-model="diseaseStatusFilter" placeholder="状态筛选" clearable @change="fetchAdminDiseases" style="width: 140px;">
              <el-option label="全部" value="" />
              <el-option label="已审核" value="APPROVED" />
              <el-option label="待审核" value="PENDING" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
            <el-button type="primary" @click="openDiseaseCreate">新增疾病</el-button>
          </div>
          <el-table :data="adminDiseaseList" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="name" label="名称" width="160" />
            <el-table-column prop="classification" label="分类" width="120" />
            <el-table-column label="严重程度" width="100">
              <template #default="{ row }">
                <span class="severity-tag" :class="(row.severity || 'MODERATE').toLowerCase()">{{ sevMap[row.severity] || '中等' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'PENDING' ? 'warning' : 'danger'" size="small">{{ statusMap[row.status] || row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="viewsCount" label="浏览" width="70" />
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" @click="openDiseaseEdit(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDeleteDisease(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="tab-pager" v-if="adminDiseaseTotal > 20">
            <el-pagination v-model:current-page="adminDiseasePage" :page-size="20" :total="adminDiseaseTotal" layout="prev, pager, next" @current-change="fetchAdminDiseases" />
          </div>
        </el-tab-pane>

        <!-- Drug Management -->
        <el-tab-pane label="药品管理" name="drugMgmt">
          <div class="tab-header">
            <el-input v-model="drugSearch" placeholder="搜索药品名称..." clearable @clear="fetchAdminDrugs" @keyup.enter="fetchAdminDrugs" style="width: 240px;" />
            <el-select v-model="drugStatusFilter" placeholder="状态筛选" clearable @change="fetchAdminDrugs" style="width: 140px;">
              <el-option label="全部" value="" />
              <el-option label="已审核" value="APPROVED" />
              <el-option label="待审核" value="PENDING" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
            <el-button type="primary" @click="openDrugCreate">新增药品</el-button>
          </div>
          <el-table :data="adminDrugList" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="name" label="名称" width="160" />
            <el-table-column prop="genericName" label="通用名" width="140" />
            <el-table-column label="类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ drugTypeMap[row.drugType] || row.drugType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'PENDING' ? 'warning' : 'danger'" size="small">{{ statusMap[row.status] || row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="viewsCount" label="浏览" width="70" />
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" @click="openDrugEdit(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDeleteDrug(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="tab-pager" v-if="adminDrugTotal > 20">
            <el-pagination v-model:current-page="adminDrugPage" :page-size="20" :total="adminDrugTotal" layout="prev, pager, next" @current-change="fetchAdminDrugs" />
          </div>
        </el-tab-pane>

        <!-- Disease Edit Dialog -->
        <el-dialog v-model="diseaseDialogVisible" :title="diseaseDialogTitle" width="700px" top="3vh">
          <el-form :model="diseaseForm" label-width="90px" label-position="top">
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="疾病名称"><el-input v-model="diseaseForm.name" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="别名"><el-input v-model="diseaseForm.alias" /></el-form-item></el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8"><el-form-item label="ICD编码"><el-input v-model="diseaseForm.icdCode" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="分类"><el-input v-model="diseaseForm.classification" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="身体系统"><el-input v-model="diseaseForm.bodySystem" /></el-form-item></el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="严重程度"><el-select v-model="diseaseForm.severity" style="width:100%"><el-option label="轻度" value="MILD" /><el-option label="中度" value="MODERATE" /><el-option label="重度" value="SEVERE" /></el-select></el-form-item>
              </el-col>
              <el-col :span="8"><el-form-item label="传染性"><el-switch v-model="diseaseForm.isInfectious" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="慢性病"><el-switch v-model="diseaseForm.isChronic" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="病因"><el-input v-model="diseaseForm.cause" type="textarea" :rows="2" /></el-form-item>
            <el-form-item label="症状"><el-input v-model="diseaseForm.symptoms" type="textarea" :rows="3" /></el-form-item>
            <el-form-item label="诊断方法"><el-input v-model="diseaseForm.diagnosis" type="textarea" :rows="2" /></el-form-item>
            <el-form-item label="治疗方案"><el-input v-model="diseaseForm.treatment" type="textarea" :rows="3" /></el-form-item>
            <el-form-item label="预防措施"><el-input v-model="diseaseForm.prevention" type="textarea" :rows="2" /></el-form-item>
            <el-form-item label="并发症"><el-input v-model="diseaseForm.complications" type="textarea" :rows="2" /></el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="diseaseDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="diseaseSaving" @click="saveDisease">{{ diseaseDialogMode === 'create' ? '创建' : '保存' }}</el-button>
          </template>
        </el-dialog>

        <!-- Skills Management -->
        <el-tab-pane label="技能管理" name="skills">
          <div class="tab-header">
            <el-button type="primary" @click="openSkillCreate">新增技能</el-button>
            <el-button type="success" @click="openSkillUpload">上传SKILL.md</el-button>
            <el-button type="warning" :loading="skillSyncing" @click="handleSyncSkills">同步Vita-skills</el-button>
            <span v-if="syncResult" class="sync-msg">{{ syncResult }}</span>
          </div>
          <el-table :data="skillList" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="name" label="名称" width="160" />
            <el-table-column prop="category" label="分类" width="140" />
            <el-table-column label="描述" min-width="200">
              <template #default="{ row }">{{ truncateText(row.description, 80) }}</template>
            </el-table-column>
            <el-table-column label="激活" width="80">
              <template #default="{ row }">
                <el-switch v-model="row.isActive" @change="toggleSkillActive(row)" />
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'APPROVED' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="240">
              <template #default="{ row }">
                <el-button size="small" @click="openSkillEdit(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDeleteSkill(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="tab-pager" v-if="skillTotal > 20">
            <el-pagination v-model:current-page="skillPage" :page-size="20" :total="skillTotal" layout="prev, pager, next" @current-change="fetchSkills" />
          </div>
        </el-tab-pane>

        <!-- Skill Edit Dialog -->
        <el-dialog v-model="skillDialogVisible" :title="skillDialogTitle" width="700px" top="3vh">
          <el-form :model="skillForm" label-width="90px" label-position="top">
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="技能名称"><el-input v-model="skillForm.name" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="分类"><el-input v-model="skillForm.category" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="描述"><el-input v-model="skillForm.description" type="textarea" :rows="2" /></el-form-item>
            <el-form-item label="技能内容（Markdown）"><el-input v-model="skillForm.content" type="textarea" :rows="12" /></el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="skillDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="skillSaving" @click="saveSkill">{{ skillDialogMode === 'create' ? '创建' : '保存' }}</el-button>
          </template>
        </el-dialog>

        <!-- Skill Upload Dialog -->
        <el-dialog v-model="skillUploadVisible" title="上传SKILL.md文件" width="600px" top="3vh">
          <el-form label-width="90px" label-position="top">
            <el-form-item label="技能名称（目录名）"><el-input v-model="skillUploadName" placeholder="如: Medical-Diagnosis" /></el-form-item>
            <el-form-item label="SKILL.md内容"><el-input v-model="skillUploadContent" type="textarea" :rows="16" placeholder="粘贴SKILL.md完整内容..." /></el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="skillUploadVisible = false">取消</el-button>
            <el-button type="primary" :loading="skillUploading" @click="handleSkillUpload">上传到Vita-skills</el-button>
          </template>
        </el-dialog>

        <!-- Drug Edit Dialog -->
        <el-dialog v-model="drugDialogVisible" :title="drugDialogTitle" width="700px" top="3vh">
          <el-form :model="drugForm" label-width="90px" label-position="top">
            <el-row :gutter="16">
              <el-col :span="8"><el-form-item label="药品名称"><el-input v-model="drugForm.name" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="通用名"><el-input v-model="drugForm.genericName" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="品牌名"><el-input v-model="drugForm.brandName" /></el-form-item></el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="类型"><el-select v-model="drugForm.drugType" style="width:100%"><el-option label="处方药" value="PRESCRIPTION" /><el-option label="非处方药" value="OTC" /><el-option label="草药" value="HERBAL" /><el-option label="生物制品" value="BIOLOGIC" /></el-select></el-form-item>
              </el-col>
              <el-col :span="8"><el-form-item label="剂型"><el-input v-model="drugForm.form" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="规格"><el-input v-model="drugForm.specification" /></el-form-item></el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="生产厂家"><el-input v-model="drugForm.manufacturer" /></el-form-item></el-col>
              <el-col :span="6"><el-form-item label="批准文号"><el-input v-model="drugForm.approvalNo" /></el-form-item></el-col>
              <el-col :span="6"><el-form-item label="价格"><el-input-number v-model="drugForm.price" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="功效"><el-input v-model="drugForm.efficacy" type="textarea" :rows="3" /></el-form-item>
            <el-form-item label="用法用量"><el-input v-model="drugForm.usage2" type="textarea" :rows="2" /></el-form-item>
            <el-form-item label="剂量"><el-input v-model="drugForm.dosage" type="textarea" :rows="2" /></el-form-item>
            <el-form-item label="副作用"><el-input v-model="drugForm.sideEffect" type="textarea" :rows="2" /></el-form-item>
            <el-form-item label="禁忌"><el-input v-model="drugForm.contraindication" type="textarea" :rows="2" /></el-form-item>
            <el-form-item label="储存条件"><el-input v-model="drugForm.storage" /></el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="drugDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="drugSaving" @click="saveDrug">{{ drugDialogMode === 'create' ? '创建' : '保存' }}</el-button>
          </template>
        </el-dialog>

        <!-- Contact Messages -->
        <el-tab-pane label="联系留言" name="contacts">
          <el-table :data="contactList" stripe style="width: 100%">
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="phone" label="电话" width="140" />
            <el-table-column prop="email" label="邮箱" width="200" />
            <el-table-column prop="content" label="留言内容" min-width="200" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.isRead ? 'success' : 'warning'" size="small">{{ row.isRead ? '已读' : '未读' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="时间" width="160">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button v-if="!row.isRead" size="small" @click="markContactRead(row.id)">标记已读</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
    <footer class="app-footer"><p>&copy; 2026 VitaAI 智慧医院系统. All rights reserved.</p></footer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import api from '@/api/index'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import { truncateText, formatDate } from '@/utils'

const activeTab = ref('users')

const stats = reactive({ totalUsers: 0, totalDoctors: 0, totalDiseases: 0, totalDrugs: 0, totalDiagnoses: 0, pendingReviews: 0 })
const roleMap: Record<string, string> = { ADMIN: '管理员', DOCTOR: '医生', USER: '用户', VISITOR: '访客' }

// Users
const users = ref<any[]>([])
const userPage = ref(1)
const userTotal = ref(0)
const userRoleFilter = ref('')

// Content
const pendingDiseases = ref<any[]>([])
const pendingDrugs = ref<any[]>([])

// Audit
const auditLogs = ref<any[]>([])

function roleType(role: string) {
  return role === 'ADMIN' ? 'danger' : role === 'DOCTOR' ? 'warning' : 'info'
}

async function fetchStats() {
  try { const res = await api.get('/admin/stats'); Object.assign(stats, res.data.data) } catch { /* */ }
}
async function fetchUsers() {
  try {
    const res = await api.get('/admin/users', { params: { page: userPage.value, role: userRoleFilter.value || undefined } })
    users.value = res.data.data.list || []
    userTotal.value = res.data.data.pagination?.total || 0
  } catch { ElMessage.error('加载失败') }
}
async function fetchPending() {
  try {
    const res = await api.get('/admin/content/pending')
    pendingDiseases.value = res.data.data.diseases?.list || []
    pendingDrugs.value = res.data.data.drugs?.list || []
  } catch { ElMessage.error('加载待审核内容失败') }
}
async function fetchAuditLogs() {
  try {
    const res = await api.get('/admin/audit-logs')
    auditLogs.value = res.data.data.list || []
  } catch { ElMessage.error('加载审计日志失败') }
}

async function toggleUser(row: any) {
  try {
    await api.put(`/admin/users/${row.id}`, { isDisabled: !row.isDisabled })
    ElMessage.success(row.isDisabled ? '已启用' : '已禁用')
    fetchUsers()
  } catch { ElMessage.error('操作失败') }
}
async function changeRole(row: any) {
  try {
    const { value } = await ElMessageBox.prompt('输入新角色 (USER/DOCTOR)', '修改角色', { inputValue: row.role })
    if (value) {
      await api.put(`/admin/users/${row.id}`, { role: value.toUpperCase() })
      ElMessage.success('角色已更新')
      fetchUsers()
    }
  } catch { /* cancelled */ }
}
async function reviewDisease(row: any, action: string) {
  try {
    await api.put(`/admin/content/diseases/${row.id}/review`, { action })
    ElMessage.success(action === 'APPROVED' ? '已通过' : '已拒绝')
    fetchPending()
    fetchStats()
  } catch { ElMessage.error('加载失败') }
}
async function reviewDrug(row: any, action: string) {
  try {
    await api.put(`/admin/content/drugs/${row.id}/review`, { action })
    ElMessage.success(action === 'APPROVED' ? '已通过' : '已拒绝')
    fetchPending()
    fetchStats()
  } catch { ElMessage.error('加载失败') }
}

// Messages
const adminMsgFilter = ref('UNRESOLVED')
const adminMsgList = ref<any[]>([])
const adminMsgStats = reactive({ unresolved: 0, resolved: 0 })
const router = useRouter()
const adminReplyingId = ref<number | null>(null)
const adminSavingMsgId = ref<number | null>(null)
const adminReplyTexts = ref<Record<number, string>>({})
// Admin edit message content
const adminEditingMsgId = ref<number | null>(null)
const adminEditMsgContent = ref('')
// Admin edit reply
const adminEditingReplyId = ref<number | null>(null)
const adminEditReplyContent = ref('')
// Contact messages
const contactList = ref<any[]>([])

// Disease Management
const adminDiseaseList = ref<any[]>([])
const adminDiseasePage = ref(1)
const adminDiseaseTotal = ref(0)
const diseaseSearch = ref('')
const diseaseStatusFilter = ref('')
const diseaseDialogVisible = ref(false)
const diseaseDialogMode = ref<'create' | 'edit'>('create')
const diseaseDialogTitle = ref('')
const diseaseSaving = ref(false)
const diseaseForm = reactive<any>({
  name: '', alias: '', icdCode: '', classification: '', bodySystem: '',
  severity: 'MODERATE', isInfectious: false, isChronic: false,
  cause: '', symptoms: '', diagnosis: '', treatment: '', prevention: '', complications: ''
})
const sevMap: Record<string, string> = { MILD: '轻度', MODERATE: '中度', SEVERE: '重度' }
const statusMap: Record<string, string> = { APPROVED: '已审核', PENDING: '待审核', REJECTED: '已拒绝' }

// Drug Management
const adminDrugList = ref<any[]>([])
const adminDrugPage = ref(1)
const adminDrugTotal = ref(0)
const drugSearch = ref('')
const drugStatusFilter = ref('')
const drugDialogVisible = ref(false)
const drugDialogMode = ref<'create' | 'edit'>('create')
const drugDialogTitle = ref('')
const drugSaving = ref(false)
const drugForm = reactive<any>({
  name: '', genericName: '', brandName: '', drugType: 'OTC', form: '',
  specification: '', manufacturer: '', approvalNo: '', price: 0,
  efficacy: '', usage2: '', dosage: '', sideEffect: '', contraindication: '', storage: ''
})
const drugTypeMap: Record<string, string> = { PRESCRIPTION: '处方药', OTC: '非处方药', HERBAL: '草药', BIOLOGIC: '生物制品' }

// Skills Management
const skillList = ref<any[]>([])
const skillPage = ref(1)
const skillTotal = ref(0)
const skillDialogVisible = ref(false)
const skillDialogMode = ref<'create' | 'edit'>('create')
const skillDialogTitle = ref('')
const skillSaving = ref(false)
const skillSyncing = ref(false)
const syncResult = ref('')
const skillForm = reactive<any>({ name: '', category: '', description: '', content: '' })

// Skill Upload
const skillUploadVisible = ref(false)
const skillUploadName = ref('')
const skillUploadContent = ref('')
const skillUploading = ref(false)

async function fetchAdminMsgStats() {
  try { const res = await api.get('/messages/stats'); Object.assign(adminMsgStats, res.data.data) } catch { /* */ }
}
async function fetchAdminMessages() {
  try {
    const res = await api.get('/messages', { params: { status: adminMsgFilter.value } })
    adminMsgList.value = res.data.data.list || []
  } catch { ElMessage.error('加载失败') }
}
async function handleAdminReply(id: number) {
  const reply = adminReplyTexts.value[id]?.trim()
  if (!reply) return
  adminReplyingId.value = id
  try {
    await api.put(`/messages/${id}/reply`, { reply })
    ElMessage.success('已回复')
    adminReplyTexts.value[id] = ''
    fetchAdminMessages()
    fetchAdminMsgStats()
  } catch { /* */ }
  finally { adminReplyingId.value = null }
}
// Admin edit user message content
function startAdminEditMsg(m: any) {
  adminEditingMsgId.value = m.id
  adminEditMsgContent.value = m.content
}
function cancelAdminEditMsg() {
  adminEditingMsgId.value = null
  adminEditMsgContent.value = ''
}
async function saveAdminEditMsg(id: number) {
  const content = adminEditMsgContent.value.trim()
  if (!content) return
  adminSavingMsgId.value = id
  try {
    await api.put(`/messages/admin/${id}`, { content })
    ElMessage.success('留言已更新')
    cancelAdminEditMsg()
    fetchAdminMessages()
  } catch { /* */ }
  finally { adminSavingMsgId.value = null }
}
// Admin edit reply
function startAdminEditReply(m: any) {
  adminEditingReplyId.value = m.id
  adminEditReplyContent.value = m.reply
}
function cancelAdminEditReply() {
  adminEditingReplyId.value = null
  adminEditReplyContent.value = ''
}
async function saveAdminEditReply(id: number) {
  const reply = adminEditReplyContent.value.trim()
  if (!reply) return
  adminReplyingId.value = id
  try {
    await api.put(`/messages/admin/${id}`, { reply })
    ElMessage.success('回复已更新')
    cancelAdminEditReply()
    fetchAdminMessages()
    fetchAdminMsgStats()
  } catch { /* */ }
  finally { adminReplyingId.value = null }
}
async function handleAdminWithdrawReply(id: number) {
  try {
    await api.delete(`/messages/${id}/reply`)
    ElMessage.success('回复已撤回')
    fetchAdminMessages()
    fetchAdminMsgStats()
  } catch { ElMessage.error('加载失败') }
}
async function markAdminStatus(id: number, status: string) {
  try {
    await api.put(`/messages/${id}/status`)
    ElMessage.success(status === 'RESOLVED' ? '已标记为已解决' : '已标记为未解决')
    fetchAdminMessages()
    fetchAdminMsgStats()
  } catch { ElMessage.error('加载失败') }
}
function goAdminDiagnose(m: any) {
  router.push({ path: '/ai-chat', query: { symptom: m.content } })
}
async function fetchContacts() {
  try {
    const res = await api.get('/contact-messages')
    contactList.value = res.data.data.list || []
  } catch { ElMessage.error('加载失败') }
}
async function markContactRead(id: number) {
  try {
    await api.put(`/contact-messages/${id}/read`)
    ElMessage.success('已标记为已读')
    fetchContacts()
  } catch { ElMessage.error('加载失败') }
}

// Disease Management functions
async function fetchAdminDiseases() {
  try {
    const params: any = { page: adminDiseasePage.value }
    if (diseaseSearch.value) params.keyword = diseaseSearch.value
    if (diseaseStatusFilter.value) params.status = diseaseStatusFilter.value
    const res = await api.get('/admin/diseases', { params })
    adminDiseaseList.value = res.data.data.list || []
    adminDiseaseTotal.value = res.data.data.pagination?.total || 0
  } catch { ElMessage.error('加载失败') }
}
function openDiseaseCreate() {
  diseaseDialogMode.value = 'create'
  diseaseDialogTitle.value = '新增疾病'
  Object.keys(diseaseForm).forEach(k => (diseaseForm as any)[k] = '')
  diseaseForm.severity = 'MODERATE'
  diseaseForm.isInfectious = false
  diseaseForm.isChronic = false
  diseaseDialogVisible.value = true
}
function openDiseaseEdit(row: any) {
  diseaseDialogMode.value = 'edit'
  diseaseDialogTitle.value = '编辑疾病 - ' + row.name
  Object.assign(diseaseForm, {
    id: row.id, name: row.name || '', alias: row.alias || '', icdCode: row.icdCode || '',
    classification: row.classification || '', bodySystem: row.bodySystem || '',
    severity: row.severity || 'MODERATE', isInfectious: row.isInfectious || false,
    isChronic: row.isChronic || false, cause: row.cause || '', symptoms: row.symptoms || '',
    diagnosis: row.diagnosis || '', treatment: row.treatment || '',
    prevention: row.prevention || '', complications: row.complications || ''
  })
  diseaseDialogVisible.value = true
}
async function saveDisease() {
  diseaseSaving.value = true
  try {
    const payload = { ...diseaseForm }
    if (diseaseDialogMode.value === 'create') {
      await api.post('/admin/diseases', payload)
      ElMessage.success('疾病已创建')
    } else {
      await api.put(`/admin/diseases/${(diseaseForm as any).id}`, payload)
      ElMessage.success('疾病已更新')
    }
    diseaseDialogVisible.value = false
    fetchAdminDiseases()
    fetchStats()
  } catch { /* */ }
  finally { diseaseSaving.value = false }
}
async function handleDeleteDisease(id: number) {
  try { await ElMessageBox.confirm('确定删除此疾病？', '确认删除', { type: 'warning' }) } catch { return }
  try {
    await api.delete(`/admin/diseases/${id}`)
    ElMessage.success('已删除')
    fetchAdminDiseases()
    fetchStats()
  } catch { ElMessage.error('加载失败') }
}

// Skills Management functions
async function fetchSkills() {
  try {
    const res = await api.get('/ai/skills/all', { params: { page: skillPage.value } })
    skillList.value = res.data.data.list || []
    skillTotal.value = res.data.data.pagination?.total || 0
  } catch { ElMessage.error('加载失败') }
}
function openSkillCreate() {
  skillDialogMode.value = 'create'
  skillDialogTitle.value = '新增技能'
  Object.keys(skillForm).forEach(k => (skillForm as any)[k] = '')
  skillDialogVisible.value = true
}
function openSkillEdit(row: any) {
  skillDialogMode.value = 'edit'
  skillDialogTitle.value = '编辑技能 - ' + row.name
  Object.assign(skillForm, {
    id: row.id, name: row.name || '', category: row.category || '',
    description: row.description || '', content: row.content || ''
  })
  skillDialogVisible.value = true
}
async function saveSkill() {
  skillSaving.value = true
  try {
    const payload = { ...skillForm }
    if (skillDialogMode.value === 'create') {
      await api.post('/ai/skills', payload)
      ElMessage.success('技能已创建')
    } else {
      await api.put(`/ai/skills/${(skillForm as any).id}`, payload)
      ElMessage.success('技能已更新')
    }
    skillDialogVisible.value = false
    fetchSkills()
  } catch { /* */ }
  finally { skillSaving.value = false }
}
async function toggleSkillActive(row: any) {
  try {
    await api.put(`/ai/skills/${row.id}`, { isActive: row.isActive })
  } catch { ElMessage.error('加载失败') }
}
async function handleDeleteSkill(id: number) {
  try { await ElMessageBox.confirm('确定删除此技能？', '确认删除', { type: 'warning' }) } catch { return }
  try {
    await api.delete(`/ai/skills/${id}`)
    ElMessage.success('已删除')
    fetchSkills()
    fetchStats()
  } catch { ElMessage.error('加载失败') }
}
function openSkillUpload() {
  skillUploadName.value = ''
  skillUploadContent.value = ''
  skillUploadVisible.value = true
}
async function handleSkillUpload() {
  if (!skillUploadName.value.trim()) { ElMessage.warning('请输入技能名称'); return }
  if (!skillUploadContent.value.trim()) { ElMessage.warning('请输入SKILL.md内容'); return }
  skillUploading.value = true
  try {
    await api.post('/ai/skills/upload', { name: skillUploadName.value, content: skillUploadContent.value })
    ElMessage.success('已上传到Vita-skills目录')
    skillUploadVisible.value = false
  } catch { /* */ }
  finally { skillUploading.value = false }
}
async function handleSyncSkills() {
  skillSyncing.value = true
  syncResult.value = ''
  try {
    const res = await api.post('/ai/skills/sync')
    const data = res.data.data
    syncResult.value = `同步完成：${data.synced} 个技能已更新`
    ElMessage.success(syncResult.value)
    fetchSkills()
  } catch { /* */ }
  finally { skillSyncing.value = false }
}

// Drug Management functions
async function fetchAdminDrugs() {
  try {
    const params: any = { page: adminDrugPage.value }
    if (drugSearch.value) params.keyword = drugSearch.value
    if (drugStatusFilter.value) params.status = drugStatusFilter.value
    const res = await api.get('/admin/drugs', { params })
    adminDrugList.value = res.data.data.list || []
    adminDrugTotal.value = res.data.data.pagination?.total || 0
  } catch { ElMessage.error('加载失败') }
}
function openDrugCreate() {
  drugDialogMode.value = 'create'
  drugDialogTitle.value = '新增药品'
  Object.keys(drugForm).forEach(k => (drugForm as any)[k] = k === 'price' ? 0 : '')
  drugForm.drugType = 'OTC'
  drugDialogVisible.value = true
}
function openDrugEdit(row: any) {
  drugDialogMode.value = 'edit'
  drugDialogTitle.value = '编辑药品 - ' + row.name
  Object.assign(drugForm, {
    id: row.id,
    name: row.name || '', genericName: row.genericName || '',
    brandName: row.brandName || '', drugType: row.drugType || 'OTC',
    form: row.form || '', specification: row.specification || '',
    manufacturer: row.manufacturer || '', approvalNo: row.approvalNo || '',
    price: row.price || 0, efficacy: row.efficacy || '', usage2: row.usage2 || '',
    dosage: row.dosage || '', sideEffect: row.sideEffect || '',
    contraindication: row.contraindication || '', storage: row.storage || ''
  })
  drugDialogVisible.value = true
}
async function saveDrug() {
  drugSaving.value = true
  try {
    const payload = { ...drugForm }
    if (drugDialogMode.value === 'create') {
      await api.post('/admin/drugs', payload)
      ElMessage.success('药品已创建')
    } else {
      await api.put(`/admin/drugs/${(drugForm as any).id}`, payload)
      ElMessage.success('药品已更新')
    }
    drugDialogVisible.value = false
    fetchAdminDrugs()
    fetchStats()
  } catch { /* */ }
  finally { drugSaving.value = false }
}
async function handleDeleteDrug(id: number) {
  try { await ElMessageBox.confirm('确定删除此药品？', '确认删除', { type: 'warning' }) } catch { return }
  try {
    await api.delete(`/admin/drugs/${id}`)
    ElMessage.success('已删除')
    fetchAdminDrugs()
    fetchStats()
  } catch { ElMessage.error('加载失败') }
}

onMounted(() => { fetchStats(); fetchUsers(); fetchPending(); fetchAuditLogs(); fetchAdminMsgStats(); fetchAdminMessages(); fetchContacts(); fetchAdminDiseases(); fetchAdminDrugs(); fetchSkills() })
</script>

<style scoped>
.admin-page { min-height: 100vh; display: flex; flex-direction: column; }
.page-hero { text-align: center; margin-bottom: 44px; }
.page-hero h1 { font-size: 38px; font-weight: 800; margin-bottom: 12px; letter-spacing: -0.5px; }
.page-hero p { color: var(--text-secondary); font-size: 16px; }

.stats-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; margin-bottom: 36px; }
.stat-card { display: flex; align-items: center; gap: 18px; padding: 22px 24px; border: 1px solid var(--border); transition: all .25s ease; }
.stat-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.stat-card.highlight { border: 2px solid var(--warning); background: linear-gradient(135deg, #fffbeb, #fef3c7); }
.stat-icon { font-size: 36px; }
.stat-num { font-size: 28px; font-weight: 800; }
.stat-label { font-size: 13px; color: var(--text-light); }

.tab-header { margin-bottom: 18px; display: flex; gap: 12px; }
.tab-pager { display: flex; justify-content: center; margin-top: 24px; }
.review-section { margin-bottom: 36px; }
.review-section h3 { font-size: 18px; font-weight: 700; margin-bottom: 18px; display: flex; align-items: center; gap: 8px; }
.message-cards { display: flex; flex-direction: column; gap: 16px; }
.msg-card { padding: 24px 28px; border: 1px solid var(--border); }
.msg-card.resolved { background: #f9fafb; opacity: 0.85; }
.msg-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.msg-header > div { display: flex; align-items: center; gap: 12px; }
.msg-user { font-weight: 700; font-size: 15px; }
.msg-date { font-size: 13px; color: var(--text-light); }
.msg-status-tag { padding: 3px 12px; border-radius: 50px; font-size: 12px; font-weight: 600; }
.msg-status-tag.unresolved { background: #fef3c7; color: #92400e; }
.msg-status-tag.resolved { background: #d1fae5; color: #065f46; }
.msg-content { font-size: 15px; line-height: 1.7; color: var(--text); white-space: pre-wrap; margin-bottom: 16px; }
.msg-reply-section { padding: 16px 20px; background: #f0fdf4; border-radius: 12px; border: 1px solid #bbf7d0; margin-bottom: 16px; }
.reply-label { font-size: 13px; font-weight: 700; color: #065f46; margin-bottom: 6px; }
.reply-text { font-size: 15px; line-height: 1.7; color: var(--text); white-space: pre-wrap; }
.msg-header-right { display: flex; align-items: center; gap: 8px; }
.msg-action-bar { display: flex; gap: 12px; align-items: flex-start; margin-top: 8px; }
.reply-input-wrap { display: flex; gap: 12px; align-items: flex-start; flex: 1; }
.reply-input-wrap :deep(.el-textarea) { flex: 1; }
.reply-date { font-size: 12px; color: var(--text-light); margin-top: 4px; }
.reply-actions { display: flex; gap: 8px; margin-top: 10px; padding-top: 10px; border-top: 1px solid #d1fae5; }
.msg-edit-btn { margin-bottom: 12px; }
.admin-edit-area { margin-bottom: 12px; }
.admin-edit-area :deep(.el-textarea__inner) { border-radius: 12px; font-size: 14px; }
.admin-edit-actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 10px; }
.edit-reply-area { margin-top: 12px; }
.edit-reply-area :deep(.el-textarea__inner) { border-radius: 12px; font-size: 14px; }
.edit-reply-actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 10px; }

.app-footer { text-align: center; padding: 36px; color: var(--text-light); font-size: 14px; border-top: 1px solid var(--border); margin-top: auto; }

.severity-tag { padding: 3px 12px; border-radius: 50px; font-size: 12px; font-weight: 600; }
.severity-tag.mild { background: #d1fae5; color: #065f46; }
.severity-tag.moderate { background: #fef3c7; color: #92400e; }
.severity-tag.severe { background: #fee2e2; color: #991b1b; }

.sync-msg { font-size: 13px; color: var(--success, #10b981); font-weight: 600; }

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
