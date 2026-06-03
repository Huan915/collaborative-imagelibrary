<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="120px">
        <div class="title-bar">
          <img class="logo" src="../assets/logo.png" alt="logo" />
          <div class="title">云图库</div>
        </div>
      </a-col>
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>
      <!-- 用户信息展示 -->
      <a-col flex="120px">
        <div class="login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar
                  :size="40"
                  :src="loginUserStore.loginUser.userAvatar"
                  @click.prevent
                ></a-avatar
                >&nbsp;&nbsp;
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item> <UserOutlined /> 个人资料 </a-menu-item>
                  <a-menu-item @click="logout"> <LogoutOutlined /> 退出登录 </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" @click="handleLogin">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
import { h, ref, computed } from 'vue'
import { HomeOutlined, LogoutOutlined, UserOutlined } from '@ant-design/icons-vue'
import { message, type MenuProps } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useUserLoginStore } from '@/stores/useUserLoginStore'
import { userLogoutUsingPost } from '@/api/userController'

const loginUserStore = useUserLoginStore()

console.log(loginUserStore.loginUser.id)

//未经过滤菜单项
const menus = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/add_picture',
    label: '创建图片',
    title: '创建图片',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: '/admin/spaceManage',
    label: '空间管理',
    title: '空间管理',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://github.com/Huan915', target: '_blank' }, 'Go to My GitHub'),
    title: '编程导航',
  },
]

const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    if ((menu?.key as string).startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

const items = computed(() => filterMenus(menus))

const router = useRouter()
// 当前要高亮的菜单项
const current = ref<string[]>([])
// 监听路由变化，更新高亮菜单项
router.afterEach((to) => {
  current.value = [to.path]
})
//路由跳转
const doMenuClick = ({ key }) => {
  router.push({
    path: key,
  })
}
const handleLogin = () => {
  router.push({
    path: '/user/login',
  })
}

const logout = async () => {
  const res = await userLogoutUsingPost()
  if (res.data.code === 0) {
    const userAccount = loginUserStore.loginUser.userAccount
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('账号-' + userAccount + '  已退出')
    router.push('/')
  }
}
</script>

<style scoped>
#globalHeader .title-bar {
  display: flex;
  align-items: center;
}
.title {
  color: black;
  font-size: 18px;
  margin-left: 16px;
}
.logo {
  height: 48px;
}
</style>
