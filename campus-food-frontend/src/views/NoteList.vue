<!-- 探店笔记列表页，支持关键词搜索和发布入口 -->
<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { note as noteApi } from '../api/index'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const notes = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ keyword: '', page: 1, size: 12 })

async function fetchNotes() {
  loading.value = true
  try {
    const res = await noteApi.list(query.value)
    notes.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(fetchNotes)
watch(() => query.value.page, fetchNotes)

function search() {
  query.value.page = 1
  fetchNotes()
}
</script>

<template>
  <div class="note-list-page">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索笔记" clearable style="width:240px" @keyup.enter="search" />
      <el-button type="primary" @click="search">搜索</el-button>
      <el-button v-if="userStore.isLoggedIn()" type="success" @click="router.push('/notes/create')">
        <el-icon><Plus /></el-icon> 发布笔记
      </el-button>
    </div>

    <div v-loading="loading" class="note-grid">
      <el-card
        v-for="n in notes" :key="n.id"
        class="note-card" @click="router.push(`/notes/${n.id}`)"
      >
        <div class="note-type-badge">
          <el-tag size="small" :type="n.type === 'VIDEO' ? 'danger' : 'success'">{{ n.type }}</el-tag>
        </div>
        <div class="note-title">{{ n.title }}</div>
        <div class="note-content">{{ n.content?.slice(0, 80) }}...</div>
        <div class="note-tags" v-if="n.tags">
          <el-tag v-for="tag in n.tags.split(',')" :key="tag" size="small" type="info" style="margin-right:4px">{{ tag }}</el-tag>
        </div>
        <div class="note-footer">
          <span><el-icon><View /></el-icon> {{ n.viewCount }}</span>
          <span><el-icon><Star /></el-icon> {{ n.likeCount }}</span>
          <span><el-icon><ChatDotRound /></el-icon> {{ n.commentCount }}</span>
        </div>
      </el-card>
      <el-empty v-if="!loading && !notes.length" description="暂无笔记" style="grid-column:1/-1" />
    </div>

    <el-pagination
      v-model:current-page="query.page"
      :page-size="query.size"
      :total="total"
      layout="prev, pager, next"
      style="margin-top:24px;justify-content:center;display:flex"
    />
  </div>
</template>

<style scoped>
.note-list-page { max-width: 1200px; margin: 0 auto; padding: 24px; }
.toolbar { display: flex; gap: 12px; align-items: center; margin-bottom: 24px; background: #fff; padding: 16px; border-radius: 8px; }
.note-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
.note-card { cursor: pointer; transition: transform .2s; position: relative; }
.note-card:hover { transform: translateY(-4px); box-shadow: 0 4px 12px rgba(0,0,0,.1); }
.note-type-badge { margin-bottom: 8px; }
.note-title { font-size: 16px; font-weight: bold; margin-bottom: 8px; }
.note-content { font-size: 13px; color: #666; margin-bottom: 8px; line-height: 1.5; }
.note-tags { margin-bottom: 8px; }
.note-footer { display: flex; gap: 16px; font-size: 12px; color: #999; align-items: center; }
.note-footer span { display: flex; align-items: center; gap: 4px; }
</style>
