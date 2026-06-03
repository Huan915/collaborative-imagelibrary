<template>
  <div id="userLoginPage">
    <h2 class="title">云图库 - 用户登录</h2>
    <div class="desc">企业级智能协同云图库</div>
    <div class="">
      <a-form
        :model="formState"
        name="basic"
        autocomplete="off"
        @finish="handleSubmit"
        @finishFailed="onFinishFailed"
      >
        <a-form-item
          label="账号"
          name="userAccount"
          :rules="[{ required: true, message: '账号不能为空' }]"
        >
          <a-input v-model:value="formState.userAccount" />
        </a-form-item>

        <a-form-item
          label="密码"
          name="userPassword"
          :rules="[{ required: true, message: '密码不能为空' }]"
        >
          <a-input-password v-model:value="formState.userPassword" />
        </a-form-item>
        <div class="tips">
          没有账号？
          <RouterLink to="/user/register">立即注册</RouterLink>
        </div>
        <a-form-item :wrapper-col="{ offset: 8, span: 16 }">
          <a-button type="primary" html-type="submit">登录</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { userLoginUsingPost } from '@/api/userController'
import router from '@/router'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'
import { useUserLoginStore } from '@/stores/useUserLoginStore'

const loginUserStore = useUserLoginStore()

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})
const handleSubmit = async (values: API.UserLoginRequest) => {
  const res = await userLoginUsingPost(values)
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.getLoginUser()
    message.success('登录成功')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败，' + res.data.message)
  }
}

const onFinishFailed = () => {
  console.log('登录失败')
}
</script>

<style scoped>
#userLoginPage {
  max-width: 360px;
  margin: 0 auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  color: #bbb;
  text-align: right;
  font-size: 13px;
  margin-bottom: 16px;
}
</style>
