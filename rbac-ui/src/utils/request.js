import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken, getRefreshToken, setToken, setRefreshToken, removeRefreshToken } from './auth'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// Token 自动刷新相关变量
let isRefreshing = false
let pendingRequests = []

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  response => {
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
      } else {
        ElMessage.error(error.response.data?.message || error.response.data?.msg || '服务器错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

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
