<template>
  <div class="profile-page">
    <el-row :gutter="16">
      <!-- 左侧：用户信息卡片 -->
      <el-col :span="8">
        <el-card shadow="hover" class="user-card">
          <div class="user-avatar">
            <el-avatar :size="100" :src="userStore.userInfo?.avatar">
              <el-icon :size="50"><User /></el-icon>
            </el-avatar>
          </div>
          <div class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</div>
          <div class="user-role" v-if="userStore.userInfo?.roles?.length">
            <el-tag v-for="role in userStore.userInfo.roles" :key="role.roleCode" size="small" class="role-tag">
              {{ role.roleCode }}
            </el-tag>
          </div>
          <el-descriptions :column="1" border class="user-desc">
            <el-descriptions-item label="用户名">{{ userStore.userInfo?.username }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ userStore.userInfo?.nickname }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ userStore.userInfo?.phone || '--' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userStore.userInfo?.email || '--' }}</el-descriptions-item>
            <el-descriptions-item label="部门ID">{{ userStore.userInfo?.deptId || '--' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 右侧：修改密码 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">修改密码</span>
          </template>
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width: 480px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitLoading" @click="handleSubmitPwd">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { changePassword } from '@/api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const submitLoading = ref(false)
const pwdFormRef = ref()

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleSubmitPwd() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } finally {
    submitLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.user-card {
  text-align: center;

  .user-avatar {
    margin-bottom: 16px;
  }

  .user-name {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }

  .user-role {
    margin-bottom: 16px;
    display: flex;
    justify-content: center;
    gap: 6px;
    flex-wrap: wrap;
  }

  .user-desc {
    margin-top: 16px;
    text-align: left;
  }
}

.card-title {
  font-weight: 600;
  font-size: 16px;
}
</style>
