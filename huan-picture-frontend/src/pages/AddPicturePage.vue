<template>
  <div id="addPicturePage">
    <h2 style="text-align: center; margin-bottom: 16px">
      {{ route.query?.id ? '修改图片' : '创建图片' }}
    </h2>
    <a-typography-paragraph v-if="spaceId" type="secondary">
      保存至空间： <a :href="`/space/${spaceId}`" target="_blank">{{ spaceId }}</a>
    </a-typography-paragraph>
    <PictureUpload :picture="picture" :onSuccess="onSuccess" :spaceId="spaceId"></PictureUpload>

    <ImageCropper
      ref="imageCropperRef"
      :imageUrl="picture?.url"
      :picture="picture"
      :spaceId="spaceId"
      :onSuccess="onCropSuccess"
      :space="space"
    />

    <ImageOutPainting
      ref="imageOutPaintingRef"
      :picture="picture"
      :onSuccess="onImageOutPaintingSuccess"
    />

    <!--    编辑图片-->
    <div v-if="picture" class="edit-bar" style="text-align: center; margin-bottom: 12px">
      <a-space>
        <a-button :icon="h(EditOutlined)" @click="doEditPicture">编辑图片</a-button>
        <a-button @click="doImagePainting" type="primary" ghost>AI智能扩图</a-button>
      </a-space>
    </div>

    <UrlPictureUpload
      :picture="picture"
      :onSuccess="onSuccess"
      :spaceId="spaceId"
    ></UrlPictureUpload>
    <!-- 图片信息编辑 -->
    <a-form layout="vertical" v-if="picture" :model="pictureForm" @finish="handleSubmit">
      <a-form-item name="name" label="图片名称">
        <a-input v-model:value="pictureForm.name" placeholder="请输入图片名称" allow-clear />
      </a-form-item>
      <a-form-item name="introduction" label="简介">
        <a-textarea
          v-model:value="pictureForm.introduction"
          placeholder="请输入图片简介"
          allow-clear
          :autoSize="{ minRows: 2, maxRows: 6 }"
        ></a-textarea>
      </a-form-item>
      <a-form-item name="category" label="分类">
        <a-auto-complete
          v-model:value="pictureForm.category"
          placeholder="请输入分类"
          :options="categoryOptions"
          allow-clear
        />
      </a-form-item>
      <a-form-item name="tags" label="标签">
        <a-select
          v-model:value="pictureForm.tags"
          mode="tags"
          placeholder="请输入标签"
          :options="tagOptions"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">创建</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
import {
  editPictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController'
import PictureUpload from '@/components/PictureUpload.vue'
import UrlPictureUpload from '@/components/UrlPictureUpload.vue'
import { message } from 'ant-design-vue'
import { computed, h, onMounted, reactive, ref, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import ImageCropper from '@/components/ImageCropper.vue'
import { EditOutlined } from '@ant-design/icons-vue'
import ImageOutPainting from '@/components/ImageOutPainting.vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController.ts'

const route = useRoute()

const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditRequest>({})

const spaceId = computed(() => {
  return route.query?.spaceId
})
/**
 * 图片上传成功
 * @param newPicture 子组件返回上传的图片信息
 */
const onSuccess = (newPicture: API.PictureVO) => {
  console.log('父组件拿到的')
  console.log(newPicture)
  picture.value = newPicture
  pictureForm.name = newPicture.name
}

const handleSubmit = async (values: API.PictureEditRequest) => {
  const pictureId = picture.value?.id
  if (!pictureId) {
    message.error('请先上传图片')
    return
  }
  const res = await editPictureUsingPost({
    id: pictureId,
    ...values,
  })

  try {
    if (res.data.code === 0 && res.data.data) {
      message.success('创建成功')
    } else {
      message.error('创建失败' + res.data.message)
    }
  } catch (error) {
    message.error('创建失败' + error)
  }
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
// 图片编辑
const imageCropperRef = ref()

const doEditPicture = () => {
  imageCropperRef.value?.openModal()
}

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

const onCropSuccess = (newPicture: API.PictureVO) => {
  console.log(newPicture)
  picture.value = newPicture
}

/**
 *  Ai扩图
 */
const imageOutPaintingRef = ref()

const doImagePainting = () => {
  imageOutPaintingRef.value?.openModal()
}

const onImageOutPaintingSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
}


onMounted(() => {
  getOldPicture()
  getTagCategoryOptions()
})

// 获取空间信息
const space = ref<API.SpaceVO>()

const fetchSpace = async () => {
  if (spaceId.value) {
    const res  = await getSpaceVoByIdUsingGet(
      {
      id: spaceId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    }
  }console.log('space this is')
}

watchEffect(() => {
  fetchSpace()
})
</script>

<style scoped>
#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
