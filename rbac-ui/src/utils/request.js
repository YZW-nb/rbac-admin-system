import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken, getRefreshToken, setToken, setRefreshToken, removeRefreshToken } from './auth'
import router from '@/router'
import { useAppStore } from '@/stores/app'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000 // 增加到 30 秒，给 AI 流式接口更多时间
})

// Token 自动刷新相关变量
let isRefreshing = false
let pendingRequests = []

// 需要显示 loading 的请求（排除某些不需要的请求）
const NO_LOADING_PATTERNS = [
  /\/dict\/type\/[^\/]+$/, // 字典类型获取
  /\/menu\/routes/,         // 路由获取
]

// 是否需要 loading
function shouldShowLoading(config) {
  // 如果明确指定了 loading 参数
  if (config.loading !== undefined) {
    return config.loading
  }
  // 检查是否匹配不需要 loading 的模式
  for (const pattern of NO_LOADING_PATTERNS) {
    if (pattern.test(config.url)) {
      return false
    }
  }
  return true
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }

    // 添加请求 ID，方便追踪
    config.headers['X-Request-Id'] = generateUUID()

    // 自动 loading
    if (shouldShowLoading(config) && !config.hideLoading) {
      const appStore = useAppStore()
      appStore.startLoading(config.loadingText || '加载中...')
    }

    return config
  },
  error => {
    hideLoading()
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    hideLoading(response.config)
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || res.msg || '请求失败')
      // Token 过期或无效
      if (res.code === 401) {
        handleUnauthorized()
      }
      return Promise.reject(new Error(res.message || res.msg || '请求失败'))
    }
    return res
  },
  error => {
    hideLoading(error.config || {})
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        const refreshToken = getRefreshToken()
        if (refreshToken && error.config.url !== '/auth/refresh') {
          // 有 refreshToken 且不是刷新请求本身，尝试自动刷新
          return handleTokenRefresh(error.config)
        } else {
          // 无 refreshToken 或刷新接口也返回 401，跳转登录
          handleUnauthorized()
        }
      } else if (status === 403) {
        ElMessage.error('没有操作权限')
      } else if (status === 423) {
        ElMessage.error(error.response.data?.message || '账户已锁定，请稍后再试')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status === 500) {
        ElMessage.error('服务器内部错误，请稍后重试')
      } else {
        ElMessage.error(error.response.data?.message || error.response.data?.msg || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else if (error.code === 'ERR_NETWORK') {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error(error.message || '未知错误')
    }
    return Promise.reject(error)
  }
)

// 隐藏 loading
function hideLoading(config = {}) {
  if (!config.hideLoading && !config.noloading) {
    try {
      const appStore = useAppStore()
      appStore.stopLoading()
    } catch (e) {
      // 如果在组件外调用，忽略错误
    }
  }
}

// 生成 UUID
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

/**
 * 处理 Token 自动刷新
 */
function handleTokenRefresh(config) {
  if (isRefreshing) {
    // 正在刷新中，将请求加入等待队列
    return new Promise((resolve) => {
      pendingRequests.push((newToken) => {
        config.headers['Authorization'] = 'Bearer ' + newToken
        resolve(request(config))
      })
    })
  }
  isRefreshing = true
  const refreshToken = getRefreshToken()
  return axios.post('/api/auth/refresh', null, {
    headers: { 'Refresh-Token': refreshToken }
  }).then(res => {
    if (res.data?.code === 200 && res.data?.data) {
      const { accessToken, refreshToken: newRefreshToken } = res.data.data
      setToken(accessToken)
      if (newRefreshToken) {
        setRefreshToken(newRefreshToken)
      }
      // 重新发送等待队列中的请求
      pendingRequests.forEach(cb => cb(accessToken))
      pendingRequests = []
      // 重新发送原始请求
      config.headers['Authorization'] = 'Bearer ' + accessToken
      return request(config)
    } else {
      handleUnauthorized()
      return Promise.reject(new Error('Token 刷新失败'))
    }
  }).catch(() => {
    handleUnauthorized()
    return Promise.reject(new Error('Token 刷新失败'))
  }).finally(() => {
    isRefreshing = false
  })
}

/**
 * 处理未授权（跳转登录页）
 */
function handleUnauthorized() {
  removeToken()
  removeRefreshToken()
  if (window.location.hash !== '#/login' && window.location.pathname !== '/login') {
    ElMessage.error('登录已过期，请重新登录')
    router.push('/login')
  }
}

export default request
