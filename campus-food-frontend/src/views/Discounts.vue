<!-- 优惠活动页，展示校园周边店铺的优惠信息列表 -->
<script setup>
import { ref, onMounted } from 'vue'
import { discount as discountApi } from '../api/index'

const discounts = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

async function fetchDiscounts() {
  loading.value = true
  try {
    const res = await discountApi.list(page.value)
    discounts.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(fetchDiscounts)
</script>

<template>
  <div class="discounts-page">
    <el-card>
      <template #header>优惠活动</template>
      <div v-loading="loading" class="discount-grid">
        <el-card v-for="d in discounts" :key="d.id" class="discount-card">
          <div class="discount-title">
            <el-tag type="danger">优惠</el-tag>
            <span style="margin-left:8px;font-weight:bold">{{ d.title }}</span>
          </div>
          <p class="discount-desc">{{ d.description }}</p>
          <div class="discount-meta">
            <span>来源：{{ d.source }}</span>
            <span>{{ d.startTime?.slice(0, 10) }} ~ {{ d.endTime?.slice(0, 10) }}</span>
          </div>
          <el-button
            v-if="d.originalUrl"
            type="primary" size="small" style="margin-top:8px"
            @click="window.open(d.originalUrl)"
          >查看原文</el-button>
        </el-card>
        <el-empty v-if="!loading && !discounts.length" description="暂无优惠活动" style="grid-column:1/-1" />
      </div>
      <el-pagination
        v-model:current-page="page"
        :page-size="10"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchDiscounts"
        style="margin-top:24px;justify-content:center;display:flex"
      />
    </el-card>
  </div>
</template>

<style scoped>
.discounts-page { max-width: 1000px; margin: 0 auto; padding: 24px; }
.discount-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.discount-card { border-left: 4px solid #f56c6c; }
.discount-title { display: flex; align-items: center; margin-bottom: 8px; }
.discount-desc { color: #555; font-size: 13px; margin: 0 0 8px; }
.discount-meta { display: flex; justify-content: space-between; font-size: 12px; color: #999; }
</style>
