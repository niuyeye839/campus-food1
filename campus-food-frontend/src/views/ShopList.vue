<!-- 店铺列表页，支持关键词、分类、排序等多维度筛选 -->
<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { shop as shopApi } from '../api/index'

const router = useRouter()
const shops = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({
  keyword: '', category: '', minScore: null,
  sortBy: 'score', studentDiscount: null,
  page: 1, size: 12
})

const categories = ['快餐', '中餐', '西餐', '小吃', '饮品', '甜品', '火锅', '烧烤']

async function fetchShops() {
  loading.value = true
  try {
    const res = await shopApi.list(query.value)
    shops.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(fetchShops)
watch(() => query.value.page, fetchShops)

function search() {
  query.value.page = 1
  fetchShops()
}
</script>

<template>
  <div class="shop-list-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索店铺名称" clearable style="width:220px" @keyup.enter="search" />
      <el-select v-model="query.category" placeholder="分类" clearable style="width:120px">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-select v-model="query.sortBy" style="width:130px">
        <el-option label="按评分排序" value="score" />
        <el-option label="按热度排序" value="hot" />
        <el-option label="按距离排序" value="distance" />
      </el-select>
      <el-checkbox v-model="query.studentDiscount" :true-value="1" :false-value="null">学生折扣</el-checkbox>
      <el-button type="primary" @click="search">搜索</el-button>
    </div>

    <!-- 店铺列表 -->
    <div v-loading="loading" class="shop-grid">
      <el-card v-for="s in shops" :key="s.id" class="shop-card" @click="router.push(`/shops/${s.id}`)">
        <div class="shop-img-placeholder">{{ s.category }}</div>
        <div class="shop-body">
          <div class="shop-name">{{ s.name }}</div>
          <div class="shop-score">
            <el-rate :model-value="Number(s.score)" disabled size="small" />
            <span>{{ s.score }}</span>
          </div>
          <div class="shop-addr">
            <el-icon>
              <Location />
            </el-icon> {{ s.address }}
          </div>
          <div class="shop-tags">
            <el-tag v-if="s.studentDiscount" size="small" type="warning">学生折扣</el-tag>
            <el-tag size="small" type="info">{{ s.category }}</el-tag>
            <el-tag size="small">{{ s.reviewCount }}条评价</el-tag>
          </div>
        </div>
      </el-card>
      <el-empty v-if="!loading && !shops.length" description="暂无店铺" style="grid-column:1/-1" />
    </div>

    <el-pagination v-model:current-page="query.page" :page-size="query.size" :total="total" layout="prev, pager, next"
      style="margin-top:24px;justify-content:center;display:flex" />
  </div>
</template>

<style scoped>
.shop-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 24px;
  background: #fff;
  padding: 16px;
  border-radius: 8px;
}

.shop-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.shop-card {
  cursor: pointer;
  transition: transform .2s;
}

.shop-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, .1);
}

.shop-img-placeholder {
  height: 120px;
  background: linear-gradient(135deg, #f5f5f5, #e0e0e0);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  border-radius: 4px;
  margin-bottom: 12px;
}

.shop-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 6px;
}

.shop-score {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.shop-addr {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.shop-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
</style>
