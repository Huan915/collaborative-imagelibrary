<template>
  <div id="url-picture-upload">
    <a-input-group compact>
      <a-input
        v-model:value="fileUrl"
        style="width: calc(100% - 120px)"
        placeholder="请输入图片URL"
      />
      <a-button type="primary" :loading="loading" style="width: 120px" @click="handleUpload"
        >提交</a-button
      >
    </a-input-group>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { uploadByUrlUsingPost } from '@/api/pictureController'

interface Props {
  picture?: API.PictureVO
  onSuccess?: (newPicture: API.PictureVO) => void
  spaceId: number
}

const props = defineProps<Props>()
const fileUrl = ref<string>()

const loading = ref<boolean>(false)
/**
 *  上传图片
 */
const handleUpload = async () => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = { url: fileUrl.value }
    if (props.picture) {
      params.id = props.picture.id
    }
    params.spaceId = props.spaceId
    const res = await uploadByUrlUsingPost(params)

    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 上传成功的图片信息返回给父组件
      props.onSuccess?.(res.data.data)
    } else {
      message.error('图片上传失败' + res.data.message)
    }
  } catch (error) {
    console.log(error)
    message.error('图片上传失败' + error.message)
  }
  loading.value = false
}
</script>
<style scoped>
#picture-upload :deep(.ant-upload) {
  width: 100%;
  height: 100%;
  min-height: 152px;
  min-width: 152px;
}

#picture-upload img {
  max-width: 480px;
}
.ant-upload-select-picture-card i {
  font-size: 32px;
  color: #999;
}

.ant-upload-select-picture-card .ant-upload-text {
  margin-top: 8px;
  color: #666;
}
</style>
