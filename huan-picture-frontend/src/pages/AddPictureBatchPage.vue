<template>
  <div id="addPictureBatchPage">
    <h2 style="margin-bottom: 16px">批量创建</h2>
    <!-- 图片信息编辑 -->
    <a-form layout="vertical" :model="formData" @finish="handleSubmit">
      <a-form-item name="serchText" label="关键字">
        <a-input v-model:value="formData.searchText" placeholder="请输入关键字" allow-clear />
      </a-form-item>
      <a-form-item name="count" label="简介">
        <a-input v-model:value="formData.count" placeholder="抓取数量" />
      </a-form-item>
      <a-form-item name="namePrefix" label="分类">
        <a-input v-model:value="formData.namePrefix" placeholder="名称前缀" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading"
          >执行任务</a-button
        >
      </a-form-item>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
import {
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
  uploadPictureByBatchUsingPost,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const picture = ref<API.PictureVO>()

const formData = reactive<API.PictureByBatchRequest>({
  count: 10,
})

const router = useRouter()

//提交任务状态
const loading = ref(false)

const handleSubmit = async () => {
  loading.value = true
  const res = await uploadPictureByBatchUsingPost({
    ...formData,
  })
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success(`创建成功，共 ${res.data.data} 条`)
    // 跳转到主页
  } else {
    message.error('创建失败，' + res.data.message)
  }
  loading.value = false
}

const categoryOptions = ref<{ value: string; label: string }[]>([])
const tagOptions = ref<{ value: string; label: string }[]>([])

/**
 * 获取标签和分类选项
 * @param values
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('获取标签分类列表失败，' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

const route = useRoute()

const getOldPicture = async () => {
  //获取到id
  const id = route.query?.id
  if (id) {
    const res = await getPictureVoByIdUsingGet({ id })
    if (res.data.code === 0 && res.data.data) {
      const oldPicture = res.data.data
      console.log(oldPicture)
      picture.value = oldPicture
      pictureForm.name = oldPicture.name
      pictureForm.introduction = oldPicture.introduction
      pictureForm.tags = oldPicture.tags
      pictureForm.category = oldPicture.category
    }
  }
}

onMounted(() => {
  getOldPicture()
})
</script>

<style scoped>
#addPictureBatchPage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
