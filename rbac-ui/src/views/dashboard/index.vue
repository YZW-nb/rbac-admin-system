<template>
  <div class="dashboard">
    <el-row :gutter="16">
      <el-col :span="6" v-for="item in statCards" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">{{ item.title }}</div>
              <div class="stat-value">{{ item.value }}</div>
            </div>
            <el-icon :size="48" :style="{ color: item.color }" class="stat-icon">
              <component :is="item.icon" />
            </el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt-16">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">系统信息</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="系统名称">RBAC 权限管理系统</el-descriptions-item>
            <el-descriptions-item label="技术栈">Spring Boot 3.2 + Vue 3 + Element Plus</el-descriptions-item>
            <el-descriptions-item label="后端框架">Spring Boot 3.2.x</el-descriptions-item>
            <el-descriptions-item label="前端框架">Vue 3.4 + Vite 5</el-descriptions-item>
            <el-descriptions-item label="数据库">PostgreSQL 15+</el-descriptions-item>
            <el-descriptions-item label="缓存">Redis 7.x</el-descriptions-item>
            <el-descriptions-item label="当前用户">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</el-descriptions-item>
            <el-descriptions-item label="当前时间">{{ currentTime }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">快捷入口</span>
          </template>
          <div class="shortcut-grid">
            <div class="shortcut-item" @click="$router.push('/system/user')">
              <el-icon :size="24" color="#409eff"><User /></el-icon>
              <span>用户管理</span>
            </div>
            <div class="shortcut-item" @click="$router.push('/system/role')">
              <el-icon :size="24" color="#67c23a"><UserFilled /></el-icon>
              <span>角色管理</span>
            </div>
            <div class="shortcut-item" @click="$router.push('/system/menu')">
              <el-icon :size="24" color="#e6a23c"><Menu /></el-icon>
              <span>菜单管理</span>
            </div>
            <div class="shortcut-item" @click="$router.push('/system/dept')">
              <el-icon :size="24" color="#f56c6c"><OfficeBuilding /></el-icon>
              <span>部门管理</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getDashboardStats } from '@/api/dashboard'

const userStore = useUserStore()
const currentTime = ref('')
let timer = null

const statCards = reactive([
  { title: '用户总数', value: '--', icon: 'User', color: '#409eff', key: 'userCount' },
  { title: '角色总数', value: '--', icon: 'UserFilled', color: '#67c23a', key: 'roleCount' },
  { title: '菜单总数', value: '--', icon: 'Menu', color: '#e6a23c', key: 'menuCount' },
  { title: '在线用户', value: '--', icon: 'Monitor', color: '#f56c6c', key: 'onlineCount' }
])

async function fetchStats() {
  try {
    const res = await getDashboardStats()
    const data = res.data || {}
    statCards.forEach(card => {
      card.value = data[card.key] ?? '--'
    })
  } catch {
    // 接口调用失败保持 '--'
  }
}

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  })
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  fetchStats()
})

onUnmounted(() => {
  timer && clearInterval(timer)
})
</script>

<style lang="scss" scoped>
.stat-card {
  .stat-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .stat-title {
    font-size: 14px;
    color: #909399;
    margin-bottom: 8px;
  }

  .stat-value {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
  }

  .stat-icon {
    opacity: 0.8;
  }
}

.card-title {
  font-weight: 600;
  font-size: 16px;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #f5f7fa;
  }

  span {
    font-size: 13px;
    color: #606266;
  }
}
</style>
