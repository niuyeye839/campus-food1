<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { rank as rankApi } from '../api/index'

const router = useRouter()
const activeTab = ref('total')
const rankData = ref([])
const loading = ref(false)
const category = ref('快餐')
const categories = ['快餐', '中餐', '西餐', '小吃', '饮品', '甜品', '火锅', '烧烤']

async function fetchRank() {
  loading.value = true
  try {
    if (activeTab.value === 'total') rankData.value = await rankApi.total()
    else if (activeTab.value === 'monthly') rankData.value = await rankApi.monthly()
    else rankData.value = await rankApi.category(category.value)
  } finally {
    loading.value = false
  }
}

onMounted(fetchRank)
watch(activeTab, fetchRank)
watch(category, () => { if (activeTab.value === 'category') fetchRank() })
</script>

<template>
  <div class="rank-page">
    <el-card>
      <template #header>店铺排行榜</template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="总榜" name="total" />
        <el-tab-pane label="月榜" name="monthly" />
        <el-tab-pane label="分类榜" name="category" />
      </el-tabs>

      <div v-if="activeTab === 'category'" style="margin-bottom:16px">
        <el-select v-model="category" @change="fetchRank">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
      </div>

      <div v-loading="loading">
        <div
          v-for="(item, index) in rankData" :key="item.shopId"
          class="rank-item"
          @click="router.push(`/shops/${item.shopId}`)"
        >
          <div class="rank-num" :class="['gold','silver','bronze'][index] || ''">{{ index + 1 }}</div>
          <div class="rank-info">
            <span class="rank-name">{{ item.shopName }}</span>
            <span class="rank-category">{{ item.category }}</span>
          </div>
          <div class="rank-score">
            <el-rate :model-value="Number(item.score)" disabled size="small" />
            <span>{{ item.score }}</span>
          </div>
        </div>
        <el-empty v-if="!loading && !rankData.length" description="暂无数据" />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.rank-page { max-width: 800px; margin: 0 auto; padding: 24px; }
.rank-item { display: flex; align-items: center; gap: 16px; padding: 14px; border-bottom: 1px solid #f0f0f0; cursor: pointer; border-radius: 4px; }
.rank-item:hover { background: #f9f9f9; }
.rank-num { width: 32px; height: 32px; border-radius: 50%; background: #e0e0e0; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 14px; flex-shrink: 0; }
.rank-num.gold { background: #ffd700; color: #fff; }
.rank-num.silver { background: #c0c0c0; color: #fff; }
.rank-num.bronze { background: #cd7f32; color: #fff; }
.rank-info { flex: 1; }
.rank-name { font-weight: bold; margin-right: 8px; }
.rank-category { color: #999; font-size: 12px; }
.rank-score { display: flex; align-items: center; gap: 6px; }
</style>
