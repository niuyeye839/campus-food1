<!-- 店铺详情页，展示店铺信息、地图导航、优惠和用户评价 -->
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { shop as shopApi, review as reviewApi, interact } from '../api/index'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import ShopMap from '../components/ShopMap.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const shopId = Number(route.params.id)

const shopData = ref(null)
const discounts = ref([])
const reviews = ref([])
const reviewTotal = ref(0)
const reviewPage = ref(1)
const showReviewDialog = ref(false)
const reviewForm = ref({ shopId, rating: 5, content: '', images: '' })
const submitting = ref(false)
const isFollowed = ref(false)

onMounted(async () => {
  shopData.value = await shopApi.detail(shopId)
  try { discounts.value = await shopApi.discounts(shopId) } catch {}
  await loadReviews()
})

async function loadReviews() {
  const res = await reviewApi.listByShop(shopId, reviewPage.value)
  reviews.value = res.records || []
  reviewTotal.value = res.total || 0
}

async function submitReview() {
  if (!userStore.isLoggedIn()) return router.push('/login')
  submitting.value = true
  try {
    await reviewApi.create(reviewForm.value)
    ElMessage.success('评价发布成功，待审核后显示')
    showReviewDialog.value = false
    reviewForm.value = { shopId, rating: 5, content: '', images: '' }
    await loadReviews()
  } finally {
    submitting.value = false
  }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn()) return router.push('/login')
  await interact.favorite('SHOP', shopId)
  ElMessage.success('操作成功')
}

async function toggleFollow() {
  if (!userStore.isLoggedIn()) return router.push('/login')
  const followed = await interact.follow('SHOP', shopId)
  isFollowed.value = followed
  ElMessage.success('操作成功')
}

async function checkFollowStatus() {
  if (userStore.isLoggedIn()) {
    try {
      // 这里需要添加一个API来检查关注状态
      // 暂时注释掉，后续实现
    } catch {}
  }
}
</script>

<template>
  <div class="shop-detail" v-if="shopData">
    <div class="shop-header">
      <div class="shop-header-info">
        <h1>{{ shopData.name }}</h1>
        <div class="shop-meta">
          <el-rate :model-value="Number(shopData.score)" disabled />
          <span class="score-text">{{ shopData.score }}</span>
          <span class="review-count">{{ shopData.reviewCount }}条评价</span>
          <el-tag v-if="shopData.studentDiscount" type="warning">学生折扣</el-tag>
          <el-tag type="info">{{ shopData.category }}</el-tag>
        </div>
        <div class="shop-info-row"><el-icon><Location /></el-icon> {{ shopData.address }}</div>
        <div class="shop-info-row" v-if="shopData.phone"><el-icon><Phone /></el-icon> {{ shopData.phone }}</div>
        <div class="shop-info-row" v-if="shopData.businessHours"><el-icon><Clock /></el-icon> {{ shopData.businessHours }}</div>
        <p v-if="shopData.description" style="color:#666;margin-top:8px">{{ shopData.description }}</p>
      </div>
      <div class="shop-actions">
        <el-button @click="toggleFavorite"><el-icon><Star /></el-icon> 收藏</el-button>
        <el-button :type="isFollowed ? 'primary' : 'default'" @click="toggleFollow">
          <el-icon v-if="!isFollowed"><Plus /></el-icon>
          <el-icon v-else><Check /></el-icon>
          {{ isFollowed ? '已关注' : '关注' }}
        </el-button>
        <el-button type="primary" @click="showReviewDialog = true">写评价</el-button>
      </div>
    </div>

    <!-- 地图 & 导航 -->
    <el-card style="margin-bottom:20px">
      <template #header><span>店铺位置</span></template>
      <ShopMap
        :longitude="shopData.longitude ? Number(shopData.longitude) : null"
        :latitude="shopData.latitude ? Number(shopData.latitude) : null"
        :name="shopData.name"
        :address="shopData.address"
      />
    </el-card>

    <!-- 折扣信息 -->
    <el-card v-if="discounts.length" style="margin-bottom:20px">
      <template #header><span>优惠活动</span></template>
      <div v-for="d in discounts" :key="d.id" class="discount-item">
        <el-tag type="danger">{{ d.title }}</el-tag>
        <span style="margin-left:8px;color:#666">{{ d.description }}</span>
      </div>
    </el-card>

    <!-- 评价列表 -->
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>用户评价（{{ reviewTotal }}）</span>
          <el-button type="primary" size="small" @click="showReviewDialog = true">写评价</el-button>
        </div>
      </template>
      <div v-for="r in reviews" :key="r.id" class="review-item">
        <div class="review-header">
          <el-rate :model-value="r.rating" disabled size="small" />
          <span class="review-time">{{ r.createdAt?.slice(0, 10) }}</span>
        </div>
        <p class="review-content">{{ r.content }}</p>
        
        <!-- 商家回复 -->
        <div v-if="r.merchantReply" class="merchant-reply">
          <div class="reply-header">
            <el-tag type="success" size="small">商家回复</el-tag>
            <span class="reply-time">{{ r.merchantReplyTime?.slice(0, 10) }}</span>
          </div>
          <p class="reply-content">{{ r.merchantReply }}</p>
        </div>
        
        <el-divider />
      </div>
      <el-empty v-if="!reviews.length" description="暂无评价" />
      <el-pagination
        v-model:current-page="reviewPage"
        :page-size="10"
        :total="reviewTotal"
        layout="prev, pager, next"
        @current-change="loadReviews"
        style="margin-top:16px;justify-content:center;display:flex"
      />
    </el-card>

    <!-- 写评价弹窗 -->
    <el-dialog v-model="showReviewDialog" title="写评价" width="500px">
      <el-form :model="reviewForm">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="分享你的用餐体验..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
  <div v-else style="text-align:center;padding:60px"><el-icon class="is-loading" :size="40"><Loading /></el-icon></div>
</template>

<style scoped>
.shop-detail { max-width: 900px; margin: 0 auto; padding: 24px; }
.shop-header { display: flex; justify-content: space-between; align-items: flex-start; background: #fff; padding: 24px; border-radius: 8px; margin-bottom: 20px; }
.shop-header-info h1 { margin: 0 0 12px; }
.shop-meta { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; margin-bottom: 12px; }
.score-text { font-size: 20px; font-weight: bold; color: #e6a23c; }
.review-count { color: #999; font-size: 13px; }
.shop-info-row { display: flex; align-items: center; gap: 6px; color: #555; margin-bottom: 6px; }
.shop-actions { display: flex; flex-direction: column; gap: 8px; }
.discount-item { padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.review-item { padding: 12px 0; }
.review-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.review-time { color: #999; font-size: 12px; }
.review-content { color: #333; margin: 0; }

/* 商家回复样式 */
.merchant-reply { margin-top: 12px; padding: 12px; background: #f5f7fa; border-radius: 8px; border-left: 3px solid #67c23a; }
.reply-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.reply-time { color: #999; font-size: 11px; }
.reply-content { color: #555; margin: 0; font-size: 14px; }
</style>
