<!-- 店铺详情页，展示店铺信息、地图导航、优惠和用户评价 -->
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { shop as shopApi, review as reviewApi, interact, upload as uploadApi, favoriteFolder as folderApi } from '../api/index'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
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
const isFavorited = ref(false)
const uploading = ref(false)
const imageList = ref([])
const favoriteFolders = ref([])
const showFolderDialog = ref(false)
const selectedFolderId = ref(null)

onMounted(async () => {
  shopData.value = await shopApi.detail(shopId)
  try { discounts.value = await shopApi.discounts(shopId) } catch {}
  await loadReviews()
  await checkFavoriteStatus()
  if (userStore.isLoggedIn()) {
    await loadFolders()
  }
})

async function loadReviews() {
  // 如果用户已登录，使用包含自己评价的接口；否则只显示已发布的
  const token = localStorage.getItem('token')
  const res = token 
    ? await reviewApi.listWithOwnReviews(shopId, reviewPage.value)
    : await reviewApi.listPublishedByShop(shopId, reviewPage.value)
  reviews.value = res.records || []
  reviewTotal.value = res.total || 0
}

async function submitReview() {
  if (!userStore.isLoggedIn()) return router.push('/login')
  if (!reviewForm.value.content.trim()) {
    return ElMessage.warning('请填写评价内容')
  }
  submitting.value = true
  try {
    reviewForm.value.images = imageList.value.join(',')
    await reviewApi.create(reviewForm.value)
    ElMessage.success('评价发布成功，待审核后显示')
    showReviewDialog.value = false
    reviewForm.value = { shopId, rating: 5, content: '', images: '' }
    imageList.value = []
    await loadReviews()
  } finally {
    submitting.value = false
  }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn()) return router.push('/login')
  
  if (!isFavorited.value) {
    // 收藏：显示收藏夹选择对话框
    await loadFolders()
    selectedFolderId.value = favoriteFolders.value.find(f => f.isDefault)?.id || null
    showFolderDialog.value = true
  } else {
    // 取消收藏
    const favorited = await interact.favorite('SHOP', shopId)
    isFavorited.value = favorited
    ElMessage.success('已取消收藏')
  }
}

async function loadFolders() {
  try {
    favoriteFolders.value = await folderApi.list()
  } catch (e) {
    ElMessage.error('加载收藏夹失败')
  }
}

async function confirmFavorite() {
  try {
    const favorited = await interact.favorite('SHOP', shopId, selectedFolderId.value)
    isFavorited.value = favorited
    showFolderDialog.value = false
    ElMessage.success('收藏成功')
  } catch (e) {
    ElMessage.error('收藏失败')
  }
}

async function checkFavoriteStatus() {
  if (userStore.isLoggedIn()) {
    try {
      isFavorited.value = await interact.isFavorited('SHOP', shopId)
    } catch {}
  }
}

async function handleImageUpload(uploadFile) {
  const file = uploadFile.raw
  if (!file.type.startsWith('image/')) {
    return ElMessage.warning('只能上传图片')
  }
  if (imageList.value.length >= 6) {
    return ElMessage.warning('最多上传6张图片')
  }
  
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('files', file)
    const urls = await uploadApi.media(fd)
    imageList.value.push(urls[0])
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败：' + (e.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

function removeImage(index) {
  imageList.value.splice(index, 1)
}

function previewImage(url) {
  window.open(url, '_blank')
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
        <el-button 
          :type="isFavorited ? 'warning' : 'default'" 
          @click="toggleFavorite"
        >
          <el-icon :style="{ color: isFavorited ? '#f7ba2a' : '' }"><Star /></el-icon> 
          {{ isFavorited ? '已收藏' : '收藏' }}
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
        <div class="review-user">
          <el-avatar :src="r.avatar || '/default-avatar.png'" :size="40" />
          <div class="user-info">
            <div style="display: flex; align-items: center; gap: 8px;">
              <span class="username">{{ r.username || '匿名用户' }}</span>
              <!-- 显示审核状态（只对评价作者显示） -->
              <el-tag v-if="r.userId === userStore.profile?.id && r.status === 0" type="warning" size="small">待审核</el-tag>
              <el-tag v-if="r.userId === userStore.profile?.id && r.status === 2" type="danger" size="small">审核未通过</el-tag>
            </div>
            <div class="review-meta">
              <el-rate :model-value="r.rating" disabled size="small" />
              <span class="review-time">{{ r.createdAt?.slice(0, 10) }}</span>
            </div>
          </div>
        </div>
        <p class="review-content">{{ r.content }}</p>
        
        <!-- 评价图片 -->
        <div v-if="r.images" class="review-images">
          <img 
            v-for="(img, idx) in r.images.split(',')" 
            :key="idx" 
            :src="img" 
            class="review-img"
            @click="previewImage(img)"
          />
        </div>
        
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
        <el-form-item label="上传图片">
          <div class="image-upload-area">
            <!-- 已上传图片预览 -->
            <div
              v-for="(url, idx) in imageList"
              :key="url"
              class="image-preview-item"
            >
              <img :src="url" class="preview-img" />
              <el-button
                class="remove-btn"
                type="danger"
                :icon="Delete"
                circle
                size="small"
                @click="removeImage(idx)"
              />
            </div>

            <!-- 上传按钮 -->
            <el-upload
              v-if="imageList.length < 6"
              class="upload-trigger"
              action="#"
              :show-file-list="false"
              accept="image/*"
              :on-change="handleImageUpload"
              :auto-upload="false"
            >
              <div class="upload-placeholder" v-loading="uploading">
                <el-icon :size="24"><Plus /></el-icon>
                <span style="font-size:12px">添加图片</span>
                <span style="font-size:11px;color:#bbb">最多6张</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReview">提交</el-button>
      </template>
    </el-dialog>

    <!-- 选择收藏夹弹窗 -->
    <el-dialog v-model="showFolderDialog" title="选择收藏夹" width="400px">
      <el-radio-group v-model="selectedFolderId" style="width:100%">
        <el-radio 
          v-for="folder in favoriteFolders" 
          :key="folder.id" 
          :value="folder.id"
          style="display:block;margin-bottom:12px"
        >
          {{ folder.name }} 
          <span style="color:#999;font-size:12px">({{ folder.count || 0 }})</span>
        </el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="showFolderDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmFavorite">确定</el-button>
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

.review-user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.user-info {
  flex: 1;
}

.username {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

.review-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.review-time { color: #999; font-size: 12px; }
.review-content { color: #333; margin: 0; }

/* 商家回复样式 */
.merchant-reply { margin-top: 12px; padding: 12px; background: #f5f7fa; border-radius: 8px; border-left: 3px solid #67c23a; }
.reply-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.reply-time { color: #999; font-size: 11px; }
.reply-content { color: #555; margin: 0; font-size: 14px; }

/* 图片上传样式 */
.image-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: flex-start;
}

.image-preview-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

.preview-img {
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

.image-preview-item:hover .remove-btn {
  opacity: 1;
}

.upload-trigger {
  width: 100px;
  height: 100px;
}

.upload-placeholder {
  width: 100px;
  height: 100px;
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

/* 评价图片展示 */
.review-images {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.review-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  transition: transform .2s;
}

.review-img:hover {
  transform: scale(1.05);
}

</style>
