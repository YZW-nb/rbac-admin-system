import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const tagsList = ref([])
  const cachedViews = ref([])

  // 全局加载状态
  const loading = ref(false)
  const loadingText = ref('加载中...')
  const requestCount = ref(0)

  // 是否有请求在进行中
  const isLoading = computed(() => requestCount.value > 0)

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function addTag(tag) {
    if (tagsList.value.some(t => t.path === tag.path)) return
    tagsList.value.push(tag)
    if (!cachedViews.value.includes(tag.name)) {
      cachedViews.value.push(tag.name)
    }
  }

  function removeTag(path) {
    const index = tagsList.value.findIndex(t => t.path === path)
    if (index > -1) {
      const removed = tagsList.value.splice(index, 1)[0]
      if (cachedViews.value.includes(removed.name)) {
        cachedViews.value.splice(cachedViews.value.indexOf(removed.name), 1)
      }
    }
  }

  function clearTags() {
    tagsList.value = []
    cachedViews.value = []
  }

  // 加载状态控制
  function startLoading(text = '加载中...') {
    loadingText.value = text
    requestCount.value++
    loading.value = true
  }

  function stopLoading() {
    requestCount.value = Math.max(0, requestCount.value - 1)
    if (requestCount.value === 0) {
      loading.value = false
      loadingText.value = '加载中...'
    }
  }

  // 强制停止所有加载
  function forceStopLoading() {
    requestCount.value = 0
    loading.value = false
    loadingText.value = '加载中...'
  }

  return {
    sidebarCollapsed,
    tagsList,
    cachedViews,
    loading,
    loadingText,
    isLoading,
    requestCount,
    toggleSidebar,
    addTag,
    removeTag,
    clearTags,
    startLoading,
    stopLoading,
    forceStopLoading
  }
})
