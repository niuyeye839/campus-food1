<!-- 消息中心页，展示互动、优惠和系统消息并支持标记已读 -->
<script setup>
import { ref, onMounted } from 'vue'
import { message as msgApi } from '../api/index'
import { ElMessage } from 'element-plus'

const activeType = ref('INTERACTION')
const messages = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

const types = [
  { label: '互动消息', value: 'INTERACTION' },
  { label: '优惠消息', value: 'DISCOUNT' },
  { label: '系统消息', value: 'SYSTEM' }
]

async function fetchMessages() {
  loading.value = true
  try {
    const res = await msgApi.list(activeType.value, page.value)
    messages.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function markRead() {
  await msgApi.markRead(activeType.value)
  messages.value.forEach(m => m.isRead = 1)
  ElMessage.success('已全部标记为已读')
}

onMounted(fetchMessages)
watch([activeType, page], fetchMessages)
</script>

<template>
  <div class="messages-page">
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>消息中心</span>
          <el-button size="small" @click="markRead">全部已读</el-button>
        </div>
      </template>

      <el-tabs v-model="activeType" @tab-change="() => { page = 1; fetchMessages() }">
        <el-tab-pane v-for="t in types" :key="t.value" :label="t.label" :name="t.value" />
      </el-tabs>

      <div v-loading="loading">
        <div v-for="m in messages" :key="m.id" class="msg-item" :class="{ unread: !m.isRead }">
          <div class="msg-header">
            <span class="msg-title">{{ m.title }}</span>
            <span class="msg-time">{{ m.createdAt?.slice(0, 16) }}</span>
          </div>
          <p class="msg-content">{{ m.content }}</p>
        </div>
        <el-empty v-if="!loading && !messages.length" description="暂无消息" />
      </div>

      <el-pagination
        v-model:current-page="page"
        :page-size="20"
        :total="total"
        layout="prev, pager, next"
        style="margin-top:16px;justify-content:center;display:flex"
      />
    </el-card>
  </div>
</template>

<style scoped>
.messages-page { max-width: 800px; margin: 0 auto; padding: 24px; }
.msg-item { padding: 14px; border-bottom: 1px solid #f0f0f0; border-radius: 4px; margin-bottom: 4px; }
.msg-item.unread { background: #fef9f0; border-left: 3px solid #e6a23c; }
.msg-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.msg-title { font-weight: bold; font-size: 14px; }
.msg-time { color: #999; font-size: 12px; }
.msg-content { margin: 0; color: #555; font-size: 13px; }
</style>
