<template>
  <div id="picture-list">
    <a-list
      :grid="{ gutter: 4, xs: 1, sm: 2, md: 4, lg: 4, xl: 6, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0" @click="doClickPicture(picture)">
          <a-card hoverable>
            <template #cover>
              <img
                alt="example"
                :src="picture.thumbnailUrl ?? picture.url"
                style="height: 320px; object-fit: cover"
              />
            </template>
            <a-card-meta :title="picture.name">
              <template #description>
                <a-flex>
                  <a-tag v-for="tag in picture.tags" :key="tag" color="green">
                    {{ tag }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>
            <template v-if="showOp" #actions>
              <a-space  @click="(e) => doEdit(picture, e)">
                <EditOutlined key="edit" />编辑
              </a-space>
              <a-space  @click="(e) => doDelete(picture, e)">
                <DeleteOutlined key="delete"/>删除
              </a-space>
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'
import { deletePictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

interface Props {
  dataList?: API.PictureVO[]
  loading: boolean
  showOp: boolean
  onReload?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false
})


const router = useRouter()
//跳转至图片详情页
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`,
  })
}



const doEdit= (picture: API.PictureVO, e:Event) => {
  // 阻止冒泡
  e.stopPropagation()
  // 跳转时一定要携带 spaceId
  router.push({
    path: '/add_picture',
    query: {
      id: picture.id,
      spaceId: picture.spaceId,
    },
  })
}

const doDelete = async (picture: API.PictureVO, e:Event) => {
  e.stopPropagation()
  console.log(picture)
  // 删除数据
    if (!picture.id) {
      return
    }
    const res = await deletePictureUsingPost({ id: picture.id })
    if (res.data.code === 0) {
      message.success('删除成功')
      // 刷新数据
      props.onReload?.()
    } else {
      message.error('删除失败')
    }
}
</script>

<style scoped>
#picture-list .search-box {
  margin: 0 auto 16px;
  max-width: 480px;
}

#picture-list .tag-list {
  margin-bottom: 20px;
}
</style>
