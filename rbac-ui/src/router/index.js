import { createRouter, createWebHistory } from 'vue-router'
import { constantRoutes } from '@/stores/permission'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'

NProgress.configure({ showSpinner: false })

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 白名单路由
const whiteList = ['/login']

router.beforeEach(async (to, from, next) => {
  NProgress.start()
  const token = getToken()

  if (token) {
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else {
      const userStore = useUserStore()
      if (userStore.userInfo) {
        next()
      } else {
        try {
          // 获取用户信息和权限路由
          await userStore.fetchUserInfo()
          const permissionStore = usePermissionStore()
          const dynamicRoutes = permissionStore.generateRoutes(userStore.routes)
          // 将构建好的树形路由存回 userStore，供侧边栏使用
          userStore.routes = permissionStore.dynamicRoutes
          // 动态添加路由
          dynamicRoutes.forEach(route => {
            router.addRoute(route)
          })
          // 添加 404 兜底路由（动态路由加载完后才加，保证优先级最低）
          router.addRoute({ path: '/:pathMatch(.*)*', redirect: '/404' })
          // 重新导航到目标路由
          next({ ...to, replace: true })
        } catch (error) {
          console.error('[Router] 获取用户信息失败:', error)
          userStore.resetState()
          next(`/login?redirect=${to.path}`)
          NProgress.done()
        }
      }
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
