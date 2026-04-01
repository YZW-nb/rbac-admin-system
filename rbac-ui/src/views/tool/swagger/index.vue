<template>
  <div class="swagger-container">
    <iframe
      v-if="swaggerUrl"
      :src="swaggerUrl"
      frameborder="0"
      class="swagger-iframe"
      @load="iframeLoading = false"
    />
    <div v-if="iframeLoading" class="loading-mask">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <span style="margin-top: 12px; color: #909399">正在加载接口文档...</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'

const swaggerUrl = ref('')
const iframeLoading = ref(true)

onMounted(() => {
  // 开发环境使用后端 Knife4j 地址
  if (import.meta.env.DEV) {
    swaggerUrl.value = '/api/doc.html'
  } else {
    swaggerUrl.value = '/api/doc.html'
  }
})
</script>

<style lang="scss" scoped>
.swagger-container {
  width: 100%;
  height: calc(100vh - 84px);
  position: relative;
}

.swagger-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fff;
}
</style>
