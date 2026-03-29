<!-- 首页，展示 AI 推荐、热门商铺和最新探店笔记 -->
<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { recommend, note as noteApi, ai, shop as shopApi } from '../api/index'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const recommendShops = ref([])
const hotShops = ref([])
const latestNotes = ref([])
const aiInput = ref('')
const aiResult = ref('')
const aiLoading = ref(false)
const aiDialogVisible = ref(false)
const studentDiscount = ref(null)

onMounted(async () => {
  if (userStore.isLoggedIn()) {
    recommend.shops(6).then(r => { recommendShops.value = r }).catch(() => { })
  }
  fetchHotShops()
  noteApi.list({ page: 1, size: 6 }).then(r => { latestNotes.value = r.records || [] }).catch(() => { })
})

async function fetchHotShops() {
  const params = studentDiscount.value ? { size: 5, studentDiscount: 1 } : { size: 5 }
  shopApi.list(params).then(r => { hotShops.value = r.records || [] }).catch(() => { })
}

watch(studentDiscount, () => {
  fetchHotShops()
})

async function askAi() {
  if (!aiInput.value.trim()) return
  aiLoading.value = true
  try {
    aiResult.value = await ai.chat({ action: 'recommend', content: aiInput.value })
    aiDialogVisible.value = true
  } catch {
    ElMessage.error('AI 服务暂不可用')
  } finally {
    aiLoading.value = false
  }
}

const COVER_COLORS = [
  'linear-gradient(135deg,#f6d365,#fda085)',
  'linear-gradient(135deg,#a1c4fd,#c2e9fb)',
  'linear-gradient(135deg,#fd7043,#ff8a65)',
  'linear-gradient(135deg,#43e97b,#38f9d7)',
  'linear-gradient(135deg,#fa709a,#fee140)',
  'linear-gradient(135deg,#4facfe,#00f2fe)',
  'linear-gradient(135deg,#f093fb,#f5576c)',
  'linear-gradient(135deg,#96fbc4,#f9f586)',
]
function noteCoverColor(id) {
  return COVER_COLORS[id % COVER_COLORS.length]
}
function shopCoverColor(id) {
  return COVER_COLORS[(id + 3) % COVER_COLORS.length]
}
</script>

<template>
  <div class="home">
    <!-- Hero -->
    <div class="hero">
      <h1>发现校园美食</h1>
      <p>探索周边好店，分享美食体验</p>
      <el-input v-model="aiInput" placeholder="问问 AI：今天吃什么？" size="large" class="hero-search" @keyup.enter="askAi">
        <template #append>
          <el-button type="primary" :loading="aiLoading" @click="askAi">AI 推荐</el-button>
        </template>
      </el-input>
    </div>

    <!-- AI 推荐弹窗 -->
    <el-dialog v-model="aiDialogVisible" title="🤖 AI 美食推荐" width="480px" align-center>
      <div class="ai-dialog-content">
        <div class="ai-question">
          <el-icon>
            <ChatDotRound />
          </el-icon>
          <span>{{ aiInput }}</span>
        </div>
        <el-divider />
        <div class="ai-answer">{{ aiResult }}</div>
      </div>
      <template #footer>
        <el-button type="primary" @click="aiDialogVisible = false">好的，去找找</el-button>
        <el-button @click="router.push('/shops')">浏览店铺</el-button>
      </template>
    </el-dialog>

    <div class="content">
      <!-- 个性化推荐 -->
      <section v-if="recommendShops.length">
        <div class="section-header">
          <h2>为你推荐</h2>
          <el-link @click="router.push('/shops')">查看更多</el-link>
        </div>
        <div class="shop-grid">
          <el-card v-for="s in recommendShops" :key="s.id" class="shop-card" @click="router.push(`/shops/${s.id}`)">
            <div class="shop-img">{{ s.category }}</div>
            <div class="shop-info">
              <div class="shop-name">{{ s.name }}</div>
              <div class="shop-meta">
                <el-rate :model-value="s.score" disabled text-color="#ff9900" size="small" />
                <span class="shop-reviews">{{ s.reviewCount }}条评价</span>
              </div>
              <div class="shop-tags">
                <el-tag v-if="s.studentDiscount" size="small" type="warning">学生折扣</el-tag>
                <el-tag size="small" type="info">{{ s.category }}</el-tag>
              </div>
            </div>
          </el-card>
        </div>
      </section>

      <!-- 热门商铺 -->
      <section>
        <div class="section-header">
          <div class="section-header-left">
            <h2>🔥 热门商铺</h2>
            <el-checkbox v-model="studentDiscount" :true-value="1" :false-value="null" style="margin-left: 16px">学生折扣</el-checkbox>
          </div>
          <el-link @click="router.push('/shops')">查看全部 &rsaquo;</el-link>
        </div>
        <div class="hot-shop-list">
          <div v-for="(s, idx) in hotShops" :key="s.id" class="hot-shop-item" @click="router.push(`/shops/${s.id}`)">
            <div class="hot-rank" :class="`rank-${idx + 1}`">{{ idx + 1 }}</div>
            <div class="hot-shop-cover" :style="{ background: shopCoverColor(s.id) }">
              <span class="hot-category">{{ s.category }}</span>
            </div>
            <div class="hot-shop-info">
              <div class="hot-shop-name">{{ s.name }}</div>
              <div class="hot-shop-meta">
                <el-rate :model-value="Number(s.score)" disabled size="small" />
                <span class="hot-score">{{ s.score }}</span>
              </div>
              <div class="hot-shop-tags">
                <el-tag v-if="s.studentDiscount" size="small" type="warning">学生折扣</el-tag>
                <el-tag size="small" type="info">{{ s.category }}</el-tag>
              </div>
            </div>
            <div class="hot-like">
              <el-icon>
                <Star />
              </el-icon>
              <span>{{ s.likeCount }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 探店笔记 -->
      <section>
        <div class="section-header">
          <h2>探店笔记</h2>
          <el-link @click="router.push('/notes')">查看更多 &rsaquo;</el-link>
        </div>
        <div class="note-waterfall">
          <div v-for="n in latestNotes" :key="n.id" class="note-card-dz" @click="router.push(`/notes/${n.id}`)">
            <!-- 封面 -->
            <div class="note-cover" :style="{ background: noteCoverColor(n.id) }">
              <el-tag v-if="n.type === 'VIDEO'" class="video-badge" type="danger" size="small">
                <el-icon>
                  <VideoPlay />
                </el-icon> 视频
              </el-tag>
              <div class="cover-category">{{ n.shopName || '美食探店' }}</div>
            </div>
            <!-- 内容 -->
            <div class="note-body">
              <div class="note-title-dz">{{ n.title }}</div>
              <div class="note-excerpt">{{ n.content?.slice(0, 50) }}{{ n.content?.length > 50 ? '...' : '' }}</div>
              <!-- 标签 -->
              <div class="note-tags" v-if="n.tags">
                <span v-for="tag in n.tags.split(',').slice(0, 2)" :key="tag" class="note-tag"># {{ tag.trim() }}</span>
              </div>
              <!-- 底部：用户 + 互动 -->
              <div class="note-footer-dz">
                <div class="note-author">
                  <div class="avatar-placeholder">{{ (n.username || '匿名')[0] }}</div>
                  <span class="author-name">{{ n.username || '匿名用户' }}</span>
                </div>
                <div class="note-stats">
                  <span><el-icon>
                      <Star />
                    </el-icon> {{ n.likeCount || 0 }}</span>
                  <span><el-icon>
                      <View />
                    </el-icon> {{ n.viewCount || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-if="!latestNotes.length" class="note-empty">
            <el-empty description="暂无笔记，快来发布第一篇吧" />
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.hero {
  background: linear-gradient(135deg, #e6a23c, #f56c6c);
  color: #fff;
  text-align: center;
  padding: 60px 24px;
}

.hero h1 {
  font-size: 36px;
  margin-bottom: 8px;
}

.hero p {
  font-size: 16px;
  margin-bottom: 24px;
  opacity: .9;
}

.hero-search {
  max-width: 500px;
  margin: 0 auto;
}

.ai-dialog-content {
  padding: 4px 0;
}

.ai-question {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: #555;
  margin-bottom: 4px;
}

.ai-answer {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
  min-height: 60px;
}

.content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header-left {
  display: flex;
  align-items: center;
}

.section-header h2 {
  margin: 0;
  font-size: 20px;
}

.shop-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.shop-card {
  cursor: pointer;
  transition: transform .2s;
}

.shop-card:hover {
  transform: translateY(-4px);
}

.shop-img {
  height: 100px;
  background: #f0f0f0;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-bottom: 8px;
}

.shop-name {
  font-weight: bold;
  margin-bottom: 4px;
}

.shop-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.shop-reviews {
  font-size: 12px;
  color: #999;
}

.shop-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

/* 热门商铺 */
.hot-shop-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-shop-item {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border-radius: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: transform .2s, box-shadow .2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .05);
}

.hot-shop-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, .1);
}

.hot-rank {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  flex-shrink: 0;
  background: #f0f0f0;
  color: #999;
}

.rank-1 {
  background: #ff4d4f;
  color: #fff;
}

.rank-2 {
  background: #ff7a45;
  color: #fff;
}

.rank-3 {
  background: #ffa940;
  color: #fff;
}

.hot-shop-cover {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hot-category {
  font-size: 11px;
  color: rgba(255, 255, 255, .9);
  background: rgba(0, 0, 0, .2);
  padding: 2px 6px;
  border-radius: 8px;
}

.hot-shop-info {
  flex: 1;
  min-width: 0;
}

.hot-shop-name {
  font-size: 15px;
  font-weight: bold;
  color: #1a1a1a;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hot-shop-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.hot-score {
  font-size: 13px;
  color: #e6a23c;
  font-weight: bold;
}

.hot-shop-tags {
  display: flex;
  gap: 4px;
}

.hot-like {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  color: #f56c6c;
  font-size: 13px;
  font-weight: bold;
  flex-shrink: 0;
}

/* 探店笔记瀑布流 */
.note-waterfall {
  columns: 3;
  column-gap: 16px;
}

@media (max-width: 900px) {
  .note-waterfall {
    columns: 2;
  }
}

@media (max-width: 560px) {
  .note-waterfall {
    columns: 1;
  }
}

.note-card-dz {
  break-inside: avoid;
  margin-bottom: 16px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .06);
  transition: transform .2s, box-shadow .2s;
}

.note-card-dz:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, .12);
}

.note-cover {
  width: 100%;
  height: 140px;
  position: relative;
  display: flex;
  align-items: flex-end;
  padding: 10px;
}

.video-badge {
  position: absolute;
  top: 8px;
  right: 8px;
}

.cover-category {
  font-size: 12px;
  color: rgba(255, 255, 255, .9);
  background: rgba(0, 0, 0, .25);
  padding: 2px 8px;
  border-radius: 10px;
}

.note-body {
  padding: 12px;
}

.note-title-dz {
  font-size: 15px;
  font-weight: bold;
  color: #1a1a1a;
  margin-bottom: 6px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.note-excerpt {
  font-size: 13px;
  color: #888;
  line-height: 1.5;
  margin-bottom: 8px;
}

.note-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.note-tag {
  font-size: 12px;
  color: #e6a23c;
}

.note-footer-dz {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.note-author {
  display: flex;
  align-items: center;
  gap: 6px;
}

.avatar-placeholder {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e6a23c, #f56c6c);
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.author-name {
  font-size: 12px;
  color: #999;
}

.note-stats {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #bbb;
  align-items: center;
}

.note-stats span {
  display: flex;
  align-items: center;
  gap: 3px;
}

.note-empty {
  column-span: all;
}
</style>
