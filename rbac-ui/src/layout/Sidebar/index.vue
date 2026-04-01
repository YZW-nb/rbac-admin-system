<template>
  <div class="sidebar-container">
    <!-- Logo -->
    <div class="sidebar-logo" :class="{ collapse: appStore.sidebarCollapsed }">
      <img src="@/assets/logo.svg" alt="Logo" class="logo-img" />
      <span v-show="!appStore.sidebarCollapsed" class="logo-title">RBAC 管理系统</span>
    </div>

    <!-- 菜单 -->
    <el-scrollbar>
      <el-menu
        :default-active="activeMenu"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        :unique-opened="true"
        @select="handleSelect"
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <!-- 单个菜单（无子菜单） -->
          <el-menu-item
            v-if="!route.children || route.children.length === 0"
            :index="route.path"
          >
            <el-icon v-if="route.meta?.icon">
              <component :is="route.meta.icon" />
            </el-icon>
            <template #title>{{ route.meta?.title }}</template>
          </el-menu-item>

          <!-- 子菜单 -->
          <el-sub-menu
            v-else
            :index="route.path"
          >
            <template #title>
              <el-icon v-if="route.meta?.icon">
                <component :is="route.meta.icon" />
              </el-icon>
              <span>{{ route.meta?.title }}</span>
            </template>
            <el-menu-item
              v-for="child in route.children"
              :key="child.path"
              :index="resolvePath(route.path, child.path)"
            >
              <el-icon v-if="child.meta?.icon">
                <component :is="child.meta.icon" />
              </el-icon>
              <template #title>{{ child.meta?.title }}</template>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { constantRoutes } from '@/stores/permission'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

// el-menu @select 事件处理：只有真正的页面菜单才跳转，目录菜单忽略
function handleSelect(index) {
  if (index && !index.endsWith('-submenu')) {
    router.push(index)
  }
}

// 将后端返回的路由数据转换为菜单数据
const menuRoutes = computed(() => {
  // 合并静态路由（dashboard 等）和动态路由
  const allRoutes = [
    ...constantRoutes.filter(r => !r.meta?.hidden),
    ...userStore.routes
  ]
  if (allRoutes.length === 0) return []
  return transformMenuRoutes(allRoutes)
})

function transformMenuRoutes(routes) {
  return routes
    .filter(r => !r.meta?.hidden && r.meta?.title)
    .map(r => ({
      ...r,
      children: r.children ? transformMenuRoutes(r.children) : []
    }))
}

function resolvePath(parentPath, childPath) {
  if (childPath.startsWith('/')) return childPath
  if (parentPath.endsWith('/')) return parentPath + childPath
  return parentPath + '/' + childPath
}
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  height: 50px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  background-color: #263445;
  overflow: hidden;

  &.collapse {
    justify-content: center;
    padding: 0;
  }

  .logo-img {
    width: 32px;
    height: 32px;
    flex-shrink: 0;
  }

  .logo-title {
    margin-left: 12px;
    color: #fff;
    font-size: 16px;
    font-weight: 600;
    white-space: nowrap;
  }
}

.el-menu {
  border-right: none;
}
</style>
