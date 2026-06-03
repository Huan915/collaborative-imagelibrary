<template>
  <div id="userRegisterPage">
    <h2 class="title">云图库 - 用户注册</h2>
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
        <a-form-item
          label="密码"
          name="checkPassword"
          :rules="[{ required: true, message: '密码不能为空' }]"
        >
          <a-input-password v-model:value="formState.checkPassword" />
        </a-form-item>
        <div class="tips">
          已有账号？
          <RouterLink to="/user/login">立即登录</RouterLink>
        </div>
        <a-form-item :wrapper-col="{ offset: 8, span: 16 }">
          <a-button type="primary" html-type="submit">注册</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { userRegisterUsingPost } from '@/api/userController'
import router from '@/router'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})
const handleSubmit = async (values: API.UserRegisterRequest) => {
  if (formState.userPassword !== formState.checkPassword) {
    message.warn('两次密码不一致')
    return
  }
  const res = await userRegisterUsingPost(values)
  console.log(res.data)
  if (res.data.code === 0 && res.data.data) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}

const onFinishFailed = () => {
  console.log('注册失败')
}
</script>

<style scoped>
#userRegisterPage {
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
