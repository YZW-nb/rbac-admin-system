import { defineStore } from 'pinia'
import { ref } from 'vue'
import Layout from '@/layout/index.vue'

// 静态路由
export const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { hidden: true, title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'House', affix: true }
      }
    ]
  },
  {
    path: '/user/profile',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Profile',
        component: () => import('@/views/user/profile/index.vue'),
        meta: { hidden: true, title: '个人中心' }
      }
    ]
  },
  // AI 智能助手
  {
    path: '/ai-chat',
    component: Layout,
    children: [
      {
        path: '',
        name: 'AiChat',
        component: () => import('@/views/ai-chat/index.vue'),
        meta: { title: 'AI 助手', icon: 'MagicStick' }
      }
    ]
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { hidden: true, title: '404' }
  }
]

// 动态路由组件映射
const viewModules = import.meta.glob('@/views/**/*.vue')

export const usePermissionStore = defineStore('permission', () => {
  const dynamicRoutes = ref([])

  /**
   * 根据后端返回的路由数据生成 Vue Router 路由
   */
  function generateRoutes(routes) {
    // 后端返回的已经是树形结构，直接过滤转换
    const accessedRoutes = filterAsyncRoutes(routes)
    dynamicRoutes.value = accessedRoutes
    return accessedRoutes
  }

  function filterAsyncRoutes(routes) {
    const result = []
    routes.forEach(route => {
      const tmp = { ...route }
      // 递归处理子路由
      if (tmp.children && tmp.children.length) {
        tmp.children = filterAsyncRoutes(tmp.children)
        // 有子路由的目录，自动 redirect 到第一个子路由
        if (!tmp.redirect && tmp.children[0].path) {
          tmp.redirect = tmp.children[0].path
        }
      }
      // 解析组件
      if (tmp.component) {
        if (tmp.component === 'Layout') {
          tmp.component = Layout
        } else {
          tmp.component = resolveComponent(tmp.component)
        }
      } else if (tmp.children && tmp.children.length) {
        tmp.component = Layout
      }
      result.push(tmp)
    })
    return result
  }

  function resolveComponent(componentPath) {
    // componentPath 形如 "system/user/index" 或 "system/user"
    let path = componentPath
    if (!path.endsWith('.vue')) {
      if (!path.includes('/index')) {
        path = path + '/index'
      }
      path = path + '.vue'
    }
    // 确保以 / 开头
    if (!path.startsWith('/')) {
      path = '/' + path
    }
    const resolved = viewModules['/src/views' + path]
    if (!resolved) {
      console.warn(`[Router] 组件未找到: ${path}`)
      return () => import('@/views/error/404.vue')
    }
    return resolved
  }

  function resetRoutes() {
    dynamicRoutes.value = []
  }

  return {
    dynamicRoutes,
    generateRoutes,
    resetRoutes
  }
})
