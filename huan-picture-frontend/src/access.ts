import { message } from 'ant-design-vue'
import router from './router'
import { useUserLoginStore } from './stores/useUserLoginStore'

let firstLogin = true

/**
 *  全局权限校验，每次切换页面时执行
 */
router.beforeEach(async (to, from, next) => {
  const loginUserStore = useUserLoginStore()
  let loginUser = loginUserStore.loginUser

  if (firstLogin) {
    await loginUserStore.getLoginUser()
    loginUser = loginUserStore.loginUser
    firstLogin = false
  }
  console.log(loginUser)
  if (to.path.startsWith('/admin')) {
    if (!loginUser || loginUser.userRole != 'admin') {
      message.error('无权限')
      next(`/user/login?redirect=${to.fullPath}`)
      return
    }
  }
  next()
})
