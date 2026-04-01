import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const tagsList = ref([])
  const cachedViews = ref([])

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

  return {
    sidebarCollapsed,
    tagsList,
    cachedViews,
    toggleSidebar,
    addTag,
    removeTag,
    clearTags
  }
})
