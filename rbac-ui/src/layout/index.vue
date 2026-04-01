<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="layout-aside">
      <Sidebar />
    </el-aside>

    <el-container class="layout-main-container">
      <!-- 顶部栏 -->
      <el-header class="layout-header" height="50px">
        <Navbar />
      </el-header>

      <!-- 标签页 -->
      <TagsView v-if="showTags" />

      <!-- 内容区 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <keep-alive :include="appStore.cachedViews">
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'
import Sidebar from './Sidebar/index.vue'
import Navbar from './Navbar/index.vue'
import TagsView from './TagsView/index.vue'

const appStore = useAppStore()
const showTags = computed(() => true)
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
}

.layout-aside {
  background-color: #304156;
  transition: width 0.28s;
  overflow: hidden;
}

.layout-main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-header {
  background: #fff;
  border-bottom: 1px solid #eee;
  padding: 0 16px;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 1;
}

.layout-main {
  background: #f0f2f5;
  padding: 16px;
  overflow-y: auto;
}
</style>
