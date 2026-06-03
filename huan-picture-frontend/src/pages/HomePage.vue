<template>
  <div id="homePage">
    <!-- 搜索框 -->
    <div class="search-box">
      <a-input-search
        v-model:value="searchParams.searchText"
        placeholder="请输入关键词"
        enter-button="搜索"
        size="large"
        @search="doSearch"
      />
    </div>
    <!-- 分类和标签筛选 -->
    <a-tabs v-model:active-key="selectedCategory" @change="doSearch">
      <a-tab-pane key="all" tab="全部"></a-tab-pane>
      <a-tab-pane v-for="tag in categoryOptions" :key="tag" :tab="tag"></a-tab-pane>
    </a-tabs>
    <div class="tag-list">
      <span style="margin-right: 8px">标签：</span>
      <a-space :size="[0, 8]" wrap>
        <a-checkable-tag
          v-for="(tag, index) in tagOptions"
          :key="tag"
          v-model:checked="selectTags[index]"
          @change="doSearch"
        >
          {{ tag }}
        </a-checkable-tag>
      </a-space>
    </div>
    <!--    图片列表-->
    <PictureList :dataList="dataList" :loading="loading"></PictureList>
    <!--    分页组件-->
    <a-pagination
      v-model:current="searchParams.current"
      v-model:page-size="searchParams.pageSize"
      :total="total"
      @change="onPageChange"
      style="float: right; margin-right: 16px"
    />
  </div>
</template>

<script lang="ts" setup>
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { onMounted, reactive, ref } from 'vue'
import PictureList from '@/components/PictureList.vue'

const dataList = ref<API.PictureVO[]>()
const total = ref(0)
const loading = ref(true)

const selectTags = ref<boolean[]>([])
const selectedCategory = ref<string>('all')

const doSearch = () => {
  searchParams.current = 1
  fetchData()
  console.log(selectTags.value)
}

//搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  const params = {
    ...searchParams,
    tags: [] as string[],
  }
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  }
  selectTags.value.forEach((useTag, index) => {
    if (useTag) {
      params.tags.push(tagOptions.value[index])
    }
  })

  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
    loading.value = false
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

onMounted(() => {
  fetchData()
})

const onPageChange = (page: number, pageSize: number) => {
  searchParams.current = page
  searchParams.pageSize = pageSize
  fetchData()
}

const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])

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

onMounted(async () => {
  await getTagCategoryOptions()
  console.log(categoryOptions)
  console.log(tagOptions)
})
</script>

<style scoped>
#homePage .search-box {
  margin: 0 auto 16px;
  max-width: 480px;
}

#homePage .tag-list {
  margin-bottom: 20px;
}
</style>
