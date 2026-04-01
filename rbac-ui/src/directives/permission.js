import { useUserStore } from '@/stores/user'

/**
 * v-permission 按钮级权限指令
 * 用法：v-permission="'sys:user:add'" 或 v-permission="['sys:user:add', 'sys:user:edit']"
 */
export default {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()
    const permissions = userStore.permissions

    if (value) {
      const requiredPerms = Array.isArray(value) ? value : [value]
      const hasPermission = requiredPerms.some(perm => permissions.includes(perm))

      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    }
  }
}
