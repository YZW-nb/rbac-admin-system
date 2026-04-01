<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">RBAC 权限管理系统</h2>
      <p class="login-subtitle">企业级后台管理系统</p>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-tips" v-if="isDev">
        <span>开发环境默认账号：admin / admin123</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

// 仅开发环境自动填充默认账号
const isDev = import.meta.env.DEV
const loginForm = reactive({
  username: isDev ? 'admin' : '',
  password: isDev ? 'admin123' : ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await userStore.login(loginForm)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/'
    await router.push(redirect)
  } catch (error) {
    // 登录失败由拦截器统一处理错误提示
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-title {
  text-align: center;
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px;
}

.login-subtitle {
  text-align: center;
  font-size: 14px;
  color: #909399;
  margin: 0 0 36px;
}

.login-btn {
  width: 100%;
}

.login-tips {
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
}
</style>
