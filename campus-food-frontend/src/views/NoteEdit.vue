<!-- 笔记编辑/发布页，支持图文和视频上传、草稿保存 -->
<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { note as noteApi, shop as shopApi, upload as uploadApi } from '../api/index'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const noteId = route.params.id ? Number(route.params.id) : null
const isEdit = !!noteId

const form = ref({ title: '', content: '', shopId: null, type: 'IMAGE', mediaUrls: '', tags: '', publish: true })
const shops = ref([])
const submitting = ref(false)
const uploading = ref(false)

// 已上传的媒体列表 [{ url, isVideo }]
const mediaList = ref([])

onMounted(async () => {
  try {
    const res = await shopApi.list({ page: 1, size: 100 })
    shops.value = res.records || []
  } catch (e) {
    ElMessage.error('加载店铺列表失败')
  }

  if (isEdit) {
    try {
      const n = await noteApi.detail(noteId)
      Object.assign(form.value, { title: n.title, content: n.content, shopId: n.shopId, type: n.type, mediaUrls: n.mediaUrls, tags: n.tags })
      if (n.mediaUrls) {
        mediaList.value = n.mediaUrls.split(',').filter(Boolean).map(url => ({
          url,
          isVideo: url.includes('/videos/')
        }))
      }
    } catch (e) {
      ElMessage.error('加载笔记失败')
    }
  } else {
    try {
      const draft = await noteApi.getDraft()
      if (draft) {
        Object.assign(form.value, draft)
        if (draft.mediaUrls) {
          mediaList.value = draft.mediaUrls.split(',').filter(Boolean).map(url => ({
            url,
            isVideo: url.includes('/videos/')
          }))
        }
      }
    } catch {}
  }
})

// 切换类型时清空媒体
watch(() => form.value.type, () => {
  mediaList.value = []
  form.value.mediaUrls = ''
})

function syncMediaUrls() {
  form.value.mediaUrls = mediaList.value.map(m => m.url).join(',')
}

async function handleFileChange(uploadFile) {
  const file = uploadFile.raw
  const isVideo = file.type.startsWith('video/')
  const isImage = file.type.startsWith('image/')

  if (form.value.type === 'IMAGE' && !isImage) {
    return ElMessage.warning('图文模式只能上传图片')
  }
  if (form.value.type === 'VIDEO' && !isVideo) {
    return ElMessage.warning('视频模式只能上传视频')
  }
  if (form.value.type === 'IMAGE' && mediaList.value.length >= 9) {
    return ElMessage.warning('最多上传9张图片')
  }
  if (form.value.type === 'VIDEO' && mediaList.value.length >= 1) {
    return ElMessage.warning('只能上传1个视频')
  }

  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('files', file)
    const urls = await uploadApi.media(fd)
    mediaList.value.push({ url: urls[0], isVideo })
    syncMediaUrls()
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败：' + (e.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

function removeMedia(index) {
  mediaList.value.splice(index, 1)
  syncMediaUrls()
}

async function saveDraft() {
  try {
    await noteApi.saveDraft({ ...form.value, publish: false })
    ElMessage.success('草稿已保存')
  } catch (e) {
    ElMessage.error('保存草稿失败')
  }
}

async function submit() {
  if (!form.value.title || !form.value.content || !form.value.shopId) {
    return ElMessage.warning('请填写完整信息')
  }
  submitting.value = true
  try {
    if (isEdit) {
      await noteApi.update(noteId, form.value)
    } else {
      await noteApi.publish(form.value)
    }
    ElMessage.success(isEdit ? '更新成功' : '发布成功')
    router.push('/notes')
  } catch (e) {
    ElMessage.error(e?.message || '操作失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="note-edit-page">
    <el-card>
      <template #header>{{ isEdit ? '编辑笔记' : '发布笔记' }}</template>
      <el-form :model="form" label-width="80px">

        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="给笔记起个标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="关联店铺">
          <el-select v-model="form.shopId" placeholder="选择店铺" filterable style="width:100%">
            <el-option v-for="s in shops" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio :value="'IMAGE'">图文</el-radio>
            <el-radio :value="'VIDEO'">短视频</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 媒体上传区 -->
        <el-form-item :label="form.type === 'IMAGE' ? '上传图片' : '上传视频'">
          <div class="media-upload-area">
            <!-- 已上传预览 -->
            <div
              v-for="(media, idx) in mediaList"
              :key="media.url"
              class="media-preview-item"
            >
              <video v-if="media.isVideo" :src="media.url" class="preview-video" controls />
              <img v-else :src="media.url" class="preview-img" />
              <el-button
                class="remove-btn"
                type="danger"
                :icon="Delete"
                circle
                size="small"
                @click="removeMedia(idx)"
              />
            </div>

            <!-- 上传按钮 -->
            <el-upload
              v-if="(form.type === 'IMAGE' && mediaList.length < 9) || (form.type === 'VIDEO' && mediaList.length < 1)"
              class="upload-trigger"
              action="#"
              :show-file-list="false"
              :accept="form.type === 'IMAGE' ? 'image/*' : 'video/*'"
              :on-change="handleFileChange"
              :auto-upload="false"
            >
              <div class="upload-placeholder" v-loading="uploading">
                <el-icon :size="28"><Plus /></el-icon>
                <span>{{ form.type === 'IMAGE' ? '添加图片' : '添加视频' }}</span>
                <span class="upload-hint">
                  {{ form.type === 'IMAGE' ? '最多9张，每张≤10MB' : '单个视频≤200MB' }}
                </span>
              </div>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="分享你的美食体验..." />
        </el-form-item>

        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔，如：好吃,性价比高" />
        </el-form-item>

        <el-form-item>
          <el-button @click="saveDraft">保存草稿</el-button>
          <el-button type="primary" :loading="submitting" @click="submit">
            {{ isEdit ? '保存修改' : '发布笔记' }}
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>

      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.note-edit-page { max-width: 800px; margin: 0 auto; padding: 24px; }

.media-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: flex-start;
}

.media-preview-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  opacity: 0;
  transition: opacity .2s;
}

.media-preview-item:hover .remove-btn {
  opacity: 1;
}

.upload-trigger {
  width: 120px;
  height: 120px;
}

.upload-placeholder {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #8c8c8c;
  font-size: 12px;
  gap: 4px;
  transition: border-color .2s;
}

.upload-placeholder:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-hint {
  font-size: 11px;
  color: #bbb;
  text-align: center;
  padding: 0 4px;
}
</style>
