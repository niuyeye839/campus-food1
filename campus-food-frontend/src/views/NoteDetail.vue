<!-- 探店笔记详情页，展示笔记内容、媒体、点赞收藏和评论区 -->
<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { note as noteApi, comment as commentApi, interact } from '../api/index'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const noteId = Number(route.params.id)

const noteData = ref(null)
const comments = ref([])
const commentTotal = ref(0)
const commentPage = ref(1)
const commentInput = ref('')
const replyTo = ref(null)
const submitting = ref(false)
const liked = ref(false)

const mediaList = computed(() => {
  if (!noteData.value?.mediaUrls) return []
  return noteData.value.mediaUrls.split(',').filter(Boolean).map(url => ({
    url,
    isVideo: url.includes('/videos/')
  }))
})

const isOwner = computed(() =>
  userStore.userInfo && noteData.value && userStore.userInfo.userId === noteData.value.userId
)

onMounted(async () => {
  try {
    noteData.value = await noteApi.detail(noteId)
    await loadComments()
    if (userStore.isLoggedIn()) {
      try { liked.value = await noteApi.isLiked(noteId) } catch {}
    }
  } catch {
    ElMessage.error('笔记不存在或已下线')
    router.push('/notes')
  }
})

async function loadComments() {
  const res = await commentApi.listByNote(noteId, commentPage.value)
  comments.value = res.records || []
  commentTotal.value = res.total || 0
}

async function submitComment() {
  if (!userStore.isLoggedIn()) return router.push('/login')
  if (!commentInput.value.trim()) return ElMessage.warning('请输入评论内容')
  submitting.value = true
  try {
    await commentApi.create({
      noteId,
      content: commentInput.value,
      parentId: replyTo.value?.id || null,
      replyUserId: replyTo.value?.userId || null
    })
    commentInput.value = ''
    replyTo.value = null
    await loadComments()
    ElMessage.success('评论成功')
  } finally {
    submitting.value = false
  }
}

async function toggleLike() {
  if (!userStore.isLoggedIn()) return router.push('/login')
  await interact.like('NOTE', noteId)
  liked.value = !liked.value
  noteData.value.likeCount += liked.value ? 1 : -1
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn()) return router.push('/login')
  await interact.favorite('NOTE', noteId)
  ElMessage.success('操作成功')
}

async function deleteNote() {
  try {
    await noteApi.delete(noteId)
    ElMessage.success('删除成功')
    router.push('/notes')
  } catch {}
}

async function deleteComment(id) {
  try {
    await commentApi.delete(id)
    await loadComments()
  } catch {}
}
</script>

<template>
  <div class="note-detail" v-if="noteData">
    <el-card class="note-card">
      <div class="note-header">
        <div class="note-title-row">
          <h1>{{ noteData.title }}</h1>
          <div v-if="isOwner" class="owner-actions">
            <el-button size="small" @click="router.push(`/notes/${noteId}/edit`)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteNote">删除</el-button>
          </div>
        </div>
        <div class="note-meta">
          <el-tag :type="noteData.type === 'VIDEO' ? 'danger' : 'success'">
            {{ noteData.type === 'VIDEO' ? '短视频' : '图文' }}
          </el-tag>
          <span class="meta-item"><el-icon><View /></el-icon> {{ noteData.viewCount }}</span>
          <span class="meta-item"><el-icon><Star /></el-icon> {{ noteData.likeCount }}</span>
          <span class="meta-item"><el-icon><ChatDotRound /></el-icon> {{ noteData.commentCount }}</span>
          <span class="meta-time">{{ noteData.createdAt?.slice(0, 10) }}</span>
        </div>
        <div v-if="noteData.tags" class="note-tags">
          <el-tag
            v-for="tag in noteData.tags.split(',')" :key="tag"
            size="small" type="info" style="margin-right:4px"
          >{{ tag }}</el-tag>
        </div>
      </div>

      <el-divider />

      <!-- 媒体展示 -->
      <div v-if="mediaList.length" class="media-section">
        <template v-if="noteData.type === 'IMAGE'">
          <el-image
            v-for="(m, i) in mediaList" :key="i"
            :src="m.url"
            :preview-src-list="mediaList.map(x => x.url)"
            :initial-index="i"
            class="note-image"
            fit="cover"
            lazy
          />
        </template>
        <template v-else>
          <video
            v-for="(m, i) in mediaList" :key="i"
            :src="m.url"
            class="note-video"
            controls
            preload="metadata"
          />
        </template>
      </div>

      <div class="note-content">{{ noteData.content }}</div>

      <div class="note-actions">
        <el-button :type="liked ? 'primary' : 'default'" @click="toggleLike">
          <el-icon><Star /></el-icon> {{ liked ? '已点赞' : '点赞' }} {{ noteData.likeCount }}
        </el-button>
        <el-button @click="toggleFavorite">
          <el-icon><Collection /></el-icon> 收藏
        </el-button>
      </div>
    </el-card>

    <!-- 评论区 -->
    <el-card style="margin-top:20px">
      <template #header>评论（{{ commentTotal }}）</template>

      <div v-if="replyTo" class="reply-hint">
        回复 @用户{{ replyTo.userId }}
        <el-button text size="small" @click="replyTo = null">取消</el-button>
      </div>

      <div v-if="userStore.isLoggedIn()" class="comment-input">
        <el-input v-model="commentInput" type="textarea" :rows="3" placeholder="写下你的评论..." />
        <el-button type="primary" :loading="submitting" @click="submitComment" style="margin-top:8px">
          发表评论
        </el-button>
      </div>
      <el-alert v-else type="info" :closable="false" style="margin-bottom:16px">
        <el-link @click="router.push('/login')">登录</el-link> 后才能评论
      </el-alert>

      <el-divider />

      <div v-for="c in comments" :key="c.id" class="comment-item">
        <div class="comment-header">
          <span class="comment-user">用户{{ c.userId }}</span>
          <span class="comment-time">{{ c.createdAt?.slice(0, 10) }}</span>
          <el-button
            v-if="userStore.userInfo?.userId === c.userId"
            text size="small" type="danger"
            @click="deleteComment(c.id)"
          >删除</el-button>
        </div>
        <p class="comment-content">{{ c.content }}</p>
        <el-button text size="small" @click="replyTo = c">回复</el-button>
        <el-divider />
      </div>
      <el-empty v-if="!comments.length" description="暂无评论" />
      <el-pagination
        v-model:current-page="commentPage"
        :page-size="10"
        :total="commentTotal"
        layout="prev, pager, next"
        @current-change="loadComments"
        style="margin-top:16px;justify-content:center;display:flex"
      />
    </el-card>
  </div>
  <div v-else style="text-align:center;padding:60px">
    <el-icon class="is-loading" :size="40"><Loading /></el-icon>
  </div>
</template>

<style scoped>
.note-detail { max-width: 800px; margin: 0 auto; padding: 24px; }
.note-title-row {
  display: flex; align-items: flex-start;
  justify-content: space-between; gap: 12px; margin-bottom: 12px;
}
.note-title-row h1 { margin: 0; flex: 1; }
.owner-actions { display: flex; gap: 8px; flex-shrink: 0; padding-top: 4px; }
.note-meta { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; margin-bottom: 8px; }
.meta-item { display: flex; align-items: center; gap: 4px; color: #666; font-size: 13px; }
.meta-time { color: #999; font-size: 12px; }
.note-tags { margin-top: 8px; }
.media-section { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
.note-image { width: calc(33.33% - 6px); min-width: 100px; aspect-ratio: 1; border-radius: 6px; cursor: pointer; object-fit: cover; }
.note-video { width: 100%; max-height: 480px; border-radius: 8px; background: #000; }
.note-content { line-height: 1.8; color: #333; white-space: pre-wrap; margin-bottom: 16px; }
.note-actions { display: flex; gap: 12px; margin-top: 20px; }
.reply-hint { background: #f5f5f5; padding: 8px 12px; border-radius: 4px; margin-bottom: 12px; font-size: 13px; display: flex; align-items: center; gap: 8px; }
.comment-input { margin-bottom: 16px; }
.comment-item { padding: 8px 0; }
.comment-header { display: flex; align-items: center; gap: 12px; margin-bottom: 6px; }
.comment-user { font-weight: bold; font-size: 13px; }
.comment-time { color: #999; font-size: 12px; }
.comment-content { margin: 0 0 4px; color: #333; }
</style>
