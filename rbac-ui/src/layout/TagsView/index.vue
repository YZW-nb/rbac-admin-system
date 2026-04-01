<template>
  <div class="tags-view">
    <el-scrollbar class="tags-scroll">
      <div class="tags-wrapper">
        <router-link
          v-for="tag in appStore.tagsList"
          :key="tag.path"
          :to="tag.path"
          class="tag-item"
          :class="{ active: isActive(tag.path) }"
          @contextmenu.prevent="openContextMenu($event, tag)"
        >
          <span>{{ tag.meta?.title }}</span>
          <el-icon
            v-if="!isAffix(tag)"
            class="tag-close"
            @click.prevent.stop="closeTag(tag)"
          >
            <Close />
          </el-icon>
        </router-link>
      </div>
    </el-scrollbar>

    <!-- 右键菜单 -->
    <ul v-show="contextMenuVisible" class="context-menu" :style="{ left: contextMenuLeft + 'px', top: contextMenuTop + 'px' }">
      <li @click="refreshPage">
        <el-icon><Refresh /></el-icon>刷新页面
      </li>
      <li v-if="!selectedTag?.meta?.affix" @click="closeSelectedTag">
        <el-icon><Close /></el-icon>关闭当前
      </li>
      <li @click="closeOtherTags">
        <el-icon><Operation /></el-icon>关闭其他
      </li>
      <li @click="closeAllTags">
        <el-icon><CircleClose /></el-icon>关闭所有
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

// 右键菜单状态
const contextMenuVisible = ref(false)
const contextMenuLeft = ref(0)
const contextMenuTop = ref(0)
const selectedTag = ref(null)

function isActive(path) {
  return route.path === path
}

function isAffix(tag) {
  return tag.meta?.affix
}

function closeTag(tag) {
  const lastTag = appStore.tagsList[appStore.tagsList.length - 1]
  appStore.removeTag(tag.path)
  if (isActive(tag.path) && lastTag) {
    router.push(lastTag.path)
  }
}

function openContextMenu(e, tag) {
  selectedTag.value = tag
  contextMenuLeft.value = e.clientX
  contextMenuTop.value = e.clientY
  contextMenuVisible.value = true
}

function closeContextMenu() {
  contextMenuVisible.value = false
}

function refreshPage() {
  closeContextMenu()
  if (selectedTag.value && isActive(selectedTag.value.path)) {
    router.go(0)
  }
}

function closeSelectedTag() {
  closeContextMenu()
  if (selectedTag.value && !selectedTag.value.meta?.affix) {
    closeTag(selectedTag.value)
  }
}

function closeOtherTags() {
  closeContextMenu()
  appStore.tagsList.forEach(tag => {
    if (!isAffix(tag) && !isActive(tag.path)) {
      appStore.removeTag(tag.path)
    }
  })
}

function closeAllTags() {
  closeContextMenu()
  appStore.tagsList.forEach(tag => {
    if (!isAffix(tag)) {
      appStore.removeTag(tag.path)
    }
  })
  // 跳转到最后一个 affix 标签
  const affixTag = appStore.tagsList[appStore.tagsList.length - 1]
  if (affixTag && !isActive(affixTag.path)) {
    router.push(affixTag.path)
  }
}

// 监听路由变化，自动添加标签
watch(
  () => route.path,
  () => {
    if (route.meta?.title && !route.meta?.hidden) {
      appStore.addTag({
        path: route.path,
        name: route.name,
        meta: route.meta
      })
    }
  },
  { immediate: true }
)

// 点击其他区域关闭右键菜单
onMounted(() => {
  document.addEventListener('click', closeContextMenu)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', closeContextMenu)
})
</script>

<style lang="scss" scoped>
.tags-view {
  height: 34px;
  background: #fff;
  border-bottom: 1px solid #eee;
  padding: 0 8px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.04);
}

.tags-scroll {
  height: 100%;
}

.tags-wrapper {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 4px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 26px;
  padding: 0 8px;
  font-size: 12px;
  color: #495060;
  background: #fff;
  border: 1px solid #d8dce5;
  border-radius: 3px;
  text-decoration: none;
  cursor: pointer;
  white-space: nowrap;

  &.active {
    background-color: #409eff;
    color: #fff;
    border-color: #409eff;
  }

  &:hover:not(.active) {
    color: #409eff;
  }

  .tag-close {
    font-size: 12px;
    border-radius: 50%;
    transition: all 0.2s;

    &:hover {
      background-color: rgba(0, 0, 0, 0.15);
      color: #fff;
    }
  }
}

.context-menu {
  position: fixed;
  z-index: 3000;
  list-style: none;
  margin: 0;
  padding: 4px 0;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  font-size: 12px;

  li {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 16px;
    cursor: pointer;
    color: #606266;
    transition: background 0.15s;

    &:hover {
      background: #ecf5ff;
      color: #409eff;
    }
  }
}
</style>
