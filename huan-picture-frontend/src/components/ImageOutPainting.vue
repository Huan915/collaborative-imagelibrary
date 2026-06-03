<template>
  <a-modal
    class="image-out-painting"
    v-model:visible="visible"
    title="AI扩图"
    :footer="false"
    @cancel="closeModal"
  >
    <a-row gutter="16">
      <a-col span="12">
        <h4>原始图片</h4>
        <img :src="picture?.url" :alt="picture?.name" style="max-width: 100%" />
      </a-col>
      <a-col span="12">
        <h4>扩图结果</h4>
        <img v-if="resultUrl" :src="resultUrl" :alt="picture?.name" style="max-width: 100%" />
      </a-col>
    </a-row>
    <div style="margin-bottom: 16px" />
    <a-flex justify="center" gap="16">
      <a-button type="primary" ghost @click="createTask" :loading="!!taskId"> AI 扩图</a-button>
      <a-button v-if="resultUrl" type="primary" :loading="uploading" @click="handleUpload">保存结果</a-button>
    </a-flex>
  </a-modal>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import {
  createTaskUsingPost,
  getPictureOutPaintingTaskUsingGet,
  uploadByUrlUsingPost,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

interface Props {
  picture?: API.PictureVO
  onSuccess?: (newPicture: API.PictureVO) => void
  spaceId?: number
}

const props = defineProps<Props>()
const resultUrl = ref<string>()
const loading = ref(false)
// 弹窗是否显示
const visible = ref(false)
// 任务ID
const taskId = ref<string>()

// 打开弹窗
const openModal = () => {
  visible.value = true
}
// 关闭弹窗
const closeModal = () => {
  visible.value = false
  taskId.value = undefined
}

const uploading = ref(false)

//暴露方法
defineExpose({
  openModal,
})

/**
 *  上传图片
 */
const handleUpload = async () => {
  uploading.value = true
  console.log('resultUrl', resultUrl.value)
  try {
    const params: API.PictureUploadRequest = { url: resultUrl.value, spaceId: props.spaceId }

    if (props.picture) {
      params.id = props.picture.id
    }
    const res = await uploadByUrlUsingPost(params)

    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 上传成功的图片信息返回给父组件
      props.onSuccess?.(res.data.data)
      closeModal()
    } else {
      message.error('图片上传失败' + res.data.message)
    }
  } catch (error) {
    console.log(error)
    message.error('图片上传失败' + error.message)
  }
  uploading.value = false
}
/**
 *  创建扩图任务
 */
const createTask = async () => {
  if (!props.picture?.id) return
  loading.value = true
  const res = await createTaskUsingPost({
    pictureId: props.picture.id,
    // 根据需要设置扩图参数
    parameters: {
      xScale: 1.5,
      yScale: 1.5,
    },
  })

  if (res.data.code == 0 && res.data.data) {
    message.success('任务创建成功， 生成中...')
    console.log(res.data.data.output?.taskId)
    // 拿到taskId 开启轮询
    taskId.value = res.data.data.output?.taskId
    startPolling()
  } else {
    message.error('任务创建失败' + res.data.message)
  }
}

let pollingTimer: NodeJS.Timeout = null
// 轮询
const startPolling = () => {
  if (!taskId.value) {
    return
  }

  pollingTimer = setInterval(async () => {
    try {
      const res = await getPictureOutPaintingTaskUsingGet({
        taskId: taskId.value,
      })
      if (res.data.code == 0 && res.data.data) {
        const taskResult = res.data.data.output
        console.log(taskResult?.taskStatus)
        if (taskResult?.taskStatus === 'SUCCEEDED') {
          message.success('扩图任务执行完成')
          resultUrl.value = taskResult?.outputImageUrl
          //清理轮询
          clearPolling()
        } else if (taskResult?.taskStatus === 'FAILED') {
          message.error('扩图任务执行失败')
          //清理轮询
          clearPolling()
        }
      }
    } catch (error) {
      console.log('轮询失败', error)
      message.error('轮询失败，' + error.message)
      clearPolling()
    }
  }, 3000) // 每3秒轮询一次
}

const clearPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
    taskId.value = null
  }
}
</script>
<style scoped>
.image-out-painting {
  min-width: 720px;
}
</style>
