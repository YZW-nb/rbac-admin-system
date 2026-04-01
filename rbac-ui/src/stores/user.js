import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/auth'
import { getToken, setToken, removeToken, getRefreshToken, setRefreshToken, removeRefreshToken } from '@/utils/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(null)
  const permissions = ref([])
  const routes = ref([])

  async function login(loginForm) {
    const res = await loginApi(loginForm)
    // 后端返回字段为 accessToken
    const accessToken = res.data.accessToken || res.data.token
    const refreshToken = res.data.refreshToken
    token.value = accessToken
    setToken(accessToken)
    if (refreshToken) {
      setRefreshToken(refreshToken)
    }
    return res
  }

  async function fetchUserInfo() {
    const res = await getUserInfo()
    // 后端直接返回 UserVO，res.data 即用户对象（含 permissions/routes）
    userInfo.value = res.data
    permissions.value = res.data.permissions || []
    routes.value = res.data.routes || []
    return res
  }

  async function logout() {
    try {
      await logoutApi()
    } catch {
      // 忽略登出接口错误
    }
    resetState()
    router.push('/login')
  }

  function resetState() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    routes.value = []
    removeToken()
    removeRefreshToken()
  }

  return {
    token,
    userInfo,
    permissions,
    routes,
    login,
    fetchUserInfo,
    logout,
    resetState
  }
})
