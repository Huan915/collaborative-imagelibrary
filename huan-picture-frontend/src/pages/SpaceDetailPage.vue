<template>
  <div id="space-detail">
    <a-flex justify="space-between">
      <h2>
        {{ space.spaceName }}
        ({{ SPACE_TYPE_MAP[space.spaceType] }})
      </h2>
      <a-space size="middle">
        <a-button type="primary" :href="`/add_picture?spaceId=${id}`" target="_blank">
          + 创建图片</a-button
        >
        <a-button
          v-if="space.spaceType"
          type="primary"
          ghost
          target="_blank"
          :href="`/spaceUserManage/${id}`"
          :icon="h(TeamOutlined)"
          >成员管理</a-button
        >
        <a-tooltip
          :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`"
          placement="topLeft"
        >
          <a-progress
            type="circle"
            :percent="
              space?.totalSize != null && space?.maxSize
                ? ((space.totalSize * 100) / space.maxSize).toFixed(1)
                : 0
            "
            :size="42"
          />
        </a-tooltip>
      </a-space>
    </a-flex>
    <div style="margin-top: 16px"></div>
    <!--    搜索表单-->
    <PictureSearchForm />
    <!--    图片列表-->
    <PictureList
      :dataList="dataList"
      :loading="loading"
      :show-op="true"
      :on-reload="fetchData"
    ></PictureList>
    <!-- 分页 -->
    <a-pagination
      style="text-align: right"
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      @change="onPageChange"
    />
  </div>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController.ts'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { computed, h, onMounted, ref, watch } from 'vue'
import { SPACE_PERMISSION_ENUM, SPACE_TYPE_MAP } from '@/constants/space.ts'
import { formatSize } from '@/utils'
import PictureList from '@/components/PictureList.vue'
import PictureSearchForm from '@/components/PictureSearchForm.vue'
import { TeamOutlined } from '@ant-design/icons-vue'

const router = useRouter()
/// ----------获取空间信息---------------
interface Props {
  id: string
}

const props = defineProps<Props>()
const space = ref<API.SpaceVO>({})

const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
      console.log(space.value)
    } else {
      message.error(res.data.message ?? '获取空间详情失败')
    }
  } catch (error) {
    message.error('请求失败，请稍后重试')
    console.error('获取空间详情时发生错误:', error)
  }
}

onMounted(async () => {
  await fetchSpaceDetail()
  console.log(space.value)
})

/// ----------获取图片信息---------------
// 定义数据
const dataList = ref<API.PictureVO[]>()
const total = ref(0)
const loading = ref(false)

const searchValue = ref<string>('')
const selectTags = ref<boolean[]>([])
const selectedCategory = ref<string>('all')

// 搜索条件
const searchParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    spaceId: props.id,
    ...searchParams.value,
  }
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

onMounted(() => {
  fetchData()
})

const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])

// 分页参数
const onPageChange = (page: number, pageSize: number) => {
  searchParams.value.current = page
  searchParams.value.pageSize = pageSize
  fetchData()
}
// 搜索
const onSearch = (newSearchParams: API.PictureQueryRequest) => {
  console.log('new', newSearchParams)

  searchParams.value = {
    ...searchParams.value,
    ...newSearchParams,
    current: 1,
  }
  console.log('searchparams', searchParams.value)
  fetchData()
}

/**
 * 获取标签和分类选项
 * @param values
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    categoryOptions.value = res.data.data.categoryList ?? []
    tagOptions.value = res.data.data.tagList ?? []
  } else {
    message.error('获取标签分类列表失败，' + res.data.message)
  }
}

// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (space.value.permissionList ?? []).includes(permission)
  })
}
// 定义权限检查
const canManageSpaceUser = createPermissionChecker(SPACE_PERMISSION_ENUM.SPACE_USER_MANAGE)
const canUploadPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_UPLOAD)
const canEditPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDeletePicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

onMounted(async () => {
  await getTagCategoryOptions()
  console.log(categoryOptions)
  console.log(tagOptions)
})

// 空间 id 改变时，必须重新获取数据
watch(
  () => props.id,
  (newSpaceId) => {
    fetchSpaceDetail()
    fetchData()
  },
)
</script>

<style scoped>
#pictureDetailPage {
  max-width: 1200px;
  margin: 0 auto;
}
</style>
