<template>
  <a-modal
    class="image-cropper"
    v-model:visible="visible"
    title="编辑图片"
    :footer="false"
    @cancel="closeModal"
  >
    <vue-cropper
      ref="cropperRef"
      :img="proxyImageUrl"
      output-type="png"
      :info="true"
      :can-move-box="true"
      :fixed-box="false"
      :auto-crop="true"
      :center-box="true"
      crossorigin="anonymous"
    />

    <div style="margin-bottom: 12px" />
    <div class="image-edit-actions" v-if="isTeamSpace">
      <a-space>
        <a-button v-if="editingUser" disabled>{{ editingUser.userName }}正在编辑</a-button>
        <a-button v-if="canEnterEdit" type="primary" ghost @click="enterEdit">进入编辑</a-button>
        <a-button v-if="canExitEdit" danger ghost @click="exitEdit">退出编辑</a-button>
      </a-space>
    </div>
    <div style="margin-bottom: 12px" />
    <!-- 图片操作 -->
    <div class="image-cropper-actions">
      <a-space>
        <a-button @click="rotateLeft" :disabled="!canEdit">向左旋转</a-button>
        <a-button @click="rotateRight" :disabled="!canEdit">向右旋转</a-button>
        <a-button @click="changeScale(2)" :disabled="!canEdit">放大</a-button>
        <a-button @click="changeScale(-2)" :disabled="!canEdit">缩小</a-button>
        <a-button type="primary" :loading="loading" :disabled="!canEdit" @click="handleConfirm"
          >确认
        </a-button>
      </a-space>
    </div>
    <div style="margin-bottom: 12px" />
  </a-modal>
</template>
<script setup lang="ts">
import { computed, onUnmounted, ref, watchEffect } from 'vue'
import { uploadPictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { useUserLoginStore } from '@/stores/useUserLoginStore.ts'
import PictureEditWebSocket from '@/utils/pictureEditWebSocket.ts'
import { PICTURE_EDIT_ACTION_ENUM, PICTURE_EDIT_MESSAGE_TYPE_ENUM } from '@/constants/picture.ts'
import { SPACE_TYPE_ENUM } from '@/constants/space.ts'

interface Props {
  imageUrl?: string
  picture?: API.PictureVO
  onSuccess?: (newPicture: API.PictureVO) => void
  spaceId?: number
  space?: API.SpaceVO
}

// 是否为团队空间
const isTeamSpace = computed(() => {
  console.log('current space')
  console.log(props.space?.spaceType)
  return props.space?.spaceType === SPACE_TYPE_ENUM.TEAM
})

// 添加默认值处理
const props = withDefaults(defineProps<Props>(), {
  imageUrl: '',
})

const cropperRef = ref()
const loading = ref(false)
// 弹窗是否显示
const visible = ref(false)

// 打开弹窗
const openModal = () => {
  visible.value = true
}
// 关闭弹窗
const closeModal = () => {
  visible.value = false
  webSocket?.disconnect()
  editingUser.value = undefined
}
//暴露方法
defineExpose({
  openModal,
})

/**
 *  上传图片
 */
const handleUpload = async ({ file }) => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = props.picture ? { id: props.picture.id } : {}
    params.spaceId = props.spaceId
    const res = await uploadPictureUsingPost(params, {}, file)

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
  loading.value = false
}

// 图片操作（左旋）
const rotateLeft = () => {
  cropperRef.value.rotateLeft()
  editAction(PICTURE_EDIT_ACTION_ENUM.ROTATE_LEFT)
}
// 图片操作（右旋）
const rotateRight = () => {
  cropperRef.value.rotateRight()
  editAction(PICTURE_EDIT_ACTION_ENUM.ROTATE_RIGHT)
}
// 图片操作（放大/缩小）
const changeScale = (number: number) => {
  cropperRef.value.changeScale(number)
  if (number < 0){
    editAction(PICTURE_EDIT_ACTION_ENUM.ZOOM_OUT)
  }else {
    editAction(PICTURE_EDIT_ACTION_ENUM.ZOOM_IN)
  }
}
// 完成编辑
const handleConfirm = () => {
  console.log(props.imageUrl)
  cropperRef.value.getCropBlob(async (blob: Blob) => {
    // 从 props.picture 获取原始文件名，如果没有则使用默认名称
    const fileName = (props.picture?.name || 'image') + '.png'
    // 将Blob转换为File
    const file = new File([blob], fileName, {
      type: blob.type,
    })
    //上传 图片
    await handleUpload({ file })
  })
}

const proxyImageUrl = computed(() => {
  // 添加对 props.imageUrl 的存在性检查
  if (props.imageUrl && props.imageUrl.includes('cos.ap-guangzhou.myqcloud.com')) {
    // 提取路径部分并加上代理前缀
    const urlObj = new URL(props.imageUrl)
    return `/cos-proxy${urlObj.pathname}`
  }
  return props.imageUrl || '' // 返回默认空字符串而不是 undefined
})
// ------------实时编辑------------
const loginUserStore = useUserLoginStore()
const loginUser = loginUserStore.loginUser

const editingUser = ref<API.UserVO>()
// 当前用户是否可进入编辑
const canEnterEdit = computed(() => {
  return !editingUser.value
})
// 正在编辑的用户是本人，可退出编辑
const canExitEdit = computed(() => {
  return editingUser.value?.id === loginUser.id
})
// 可以点击图片的编辑操作按钮
const canEdit = computed(() => {
  // 不是团队空间，默认就可以编辑
  if (!isTeamSpace.value) {
    return true
  }
  return editingUser.value?.id === loginUser.id
})
// 编写 WebSocket 逻辑
let webSocket: PictureEditWebSocket | null

const initWebSocket = () => {
  const pictureId = props.picture?.id
  if (!pictureId || !visible.value) {
    return
  }
  webSocket?.disconnect()
  webSocket = new PictureEditWebSocket(pictureId)
  // 建立连接
  webSocket.connect()

  webSocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.INFO, (msg) => {
    message.info('收到通知消息', msg)
  })
  webSocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.EDIT_ACTION, (msg) => {
    message.info('收到编辑操作', msg.editAction)
    // 根据收到的编辑操作进行相应的操作
    switch (msg.editAction) {
      case PICTURE_EDIT_ACTION_ENUM.ZOOM_IN:
        changeScale(2)
        break
      case PICTURE_EDIT_ACTION_ENUM.ZOOM_OUT:
        changeScale(-2)
        break
      case PICTURE_EDIT_ACTION_ENUM.ROTATE_LEFT:
        rotateLeft()
        break
      case PICTURE_EDIT_ACTION_ENUM.ROTATE_RIGHT:
        rotateRight()
    }
  })
  webSocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.ENTER_EDIT, (msg) => {
    console.log('收到进入编辑状态的消息：', msg)
    message.info(msg.message)
    editingUser.value = msg.user
  })
  webSocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.EXIT_EDIT, (msg) => {
    message.info(msg.user.userName + '退出编辑')
    editingUser.value = undefined
  })
  webSocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.ERROR, (msg) => {
    message.error('发生错误', msg)
  })
}

watchEffect(() => {
  if (isTeamSpace.value) {
    initWebSocket()
  }
})
// 断开websocket连接
onUnmounted(() => {
  if (webSocket) {
    webSocket.disconnect()
  }
  editingUser.value = undefined
})
// 进入编辑图片
const enterEdit = () => {
  if (webSocket) {
    // 发送进入编辑状态的请求
    webSocket.sendMessage({
      type: PICTURE_EDIT_MESSAGE_TYPE_ENUM.ENTER_EDIT,
    })
  }
}
// 退出编辑图片
const exitEdit = () => {
  if (webSocket) {
    // 发送进入编辑状态的请求
    webSocket.sendMessage({
      type: PICTURE_EDIT_MESSAGE_TYPE_ENUM.EXIT_EDIT,
    })
  }
}
// 编辑图片操作
const editAction = (action: string) => {
  if (webSocket) {
    // 发送进入编辑状态的请求
    webSocket.sendMessage({
      type: PICTURE_EDIT_MESSAGE_TYPE_ENUM.EDIT_ACTION,
      editAction: action,
    })
  }
}
</script>
<style scoped>
.image-cropper .image-cropper-actions {
  text-align: center;
}
.image-cropper .image-edit-actions {
  text-align: center;
}
.image-cropper .vue-cropper {
  height: 400px !important;
}
</style>
