<template>
  <div class="navbar">
    <!-- 左侧：折叠按钮 + 面包屑 -->
    <div class="navbar-left">
      <el-icon class="collapse-btn" @click="appStore.toggleSidebar">
        <Fold v-if="!appStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
          {{ item.meta?.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 右侧：功能区 -->
    <div class="navbar-right">
      <el-tooltip content="刷新" placement="bottom">
        <el-icon class="action-icon" @click="handleRefresh">
          <Refresh />
        </el-icon>
      </el-tooltip>
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="30" :src="userStore.userInfo?.avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>个人中心
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const breadcrumbs = computed(() => {
  return route.matched.filter(item => item.meta?.title)
})

function handleRefresh() {
  router.go(0)
}

async function handleCommand(command) {
  if (command === 'logout') {
    await userStore.logout()
  } else if (command === 'profile') {
    router.push('/user/profile')
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  width: 100%;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-btn {
  font-size: 22px;
  cursor: pointer;
  color: #5a5e66;

  &:hover {
    color: #409eff;
  }
}

.breadcrumb {
  font-size: 14px;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-icon {
  font-size: 20px;
  cursor: pointer;
  color: #5a5e66;

  &:hover {
    color: #409eff;
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 0 8px;
  height: 50px;

  &:hover {
    background: rgba(0, 0, 0, 0.025);
  }

  .username {
    font-size: 14px;
    color: #333;
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
