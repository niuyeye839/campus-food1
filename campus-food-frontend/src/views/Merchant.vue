<!-- 商家管理后台页，提供店铺基础资料管理、数据分析和评价管理功能 -->
<script setup>
import { ref, onMounted, computed, onMounted as onMountedHook, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { shop as shopApi, review as reviewApi, admin as adminApi, comment as commentApi } from '../api/index'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const activeTab = ref(route.query.tab || 'shop')

// 店铺管理
const shopData = ref(null)
const shopLoading = ref(false)
const shopDialogVisible = ref(false)
const shopForm = ref({ name: '', category: '', address: '', phone: '', description: '', businessHours: '', studentDiscount: 0 })
const categories = ['快餐', '中餐', '西餐', '小吃', '饮品', '甜品', '火锅', '烧烤']
const shopSubmitting = ref(false)

// 数据分析
const analyticsLoading = ref(false)
const statsData = ref(null)
const chartData = ref({
  ratingDistribution: [],
  reviewTrend: [],
  trafficTrend: []
})

// 图表引用
const ratingChartRef = ref(null)
const reviewChartRef = ref(null)
const trafficChartRef = ref(null)
let ratingChart = null
let reviewChart = null
let trafficChart = null

// 评价管理
const reviews = ref([])
const reviewTotal = ref(0)
const reviewPage = ref(1)
const reviewLoading = ref(false)
const reviewQuery = ref({
  sortBy: 'time', // time, rating
  order: 'desc', // asc, desc
  rating: null
})

// 回复功能
const replyDialogVisible = ref(false)
const currentReview = ref(null)
const replyContent = ref('')
const replySubmitting = ref(false)

async function fetchShop() {
  shopLoading.value = true
  try {
    // 这里需要根据商家ID获取店铺信息，暂时获取第一个店铺
    const res = await shopApi.list({ page: 1, size: 1 })
    if (res.records && res.records.length > 0) {
      shopData.value = res.records[0]
      shopForm.value = {
        name: shopData.value.name,
        category: shopData.value.category,
        address: shopData.value.address,
        phone: shopData.value.phone || '',
        description: shopData.value.description || '',
        businessHours: shopData.value.businessHours || '',
        studentDiscount: shopData.value.studentDiscount || 0
      }
    }
  } finally {
    shopLoading.value = false
  }
}

function openEditShop() {
  if (!shopData.value) {
    ElMessage.warning('暂无店铺信息')
    return
  }
  shopDialogVisible.value = true
}

async function submitShop() {
  if (!shopForm.value.name || !shopForm.value.category || !shopForm.value.address) {
    return ElMessage.warning('请填写店铺名称、分类和地址')
  }
  shopSubmitting.value = true
  try {
    if (shopData.value) {
      await shopApi.update(shopData.value.id, shopForm.value)
      ElMessage.success('更新成功')
      await fetchShop()
    } else {
      await shopApi.create(shopForm.value)
      ElMessage.success('添加成功')
      await fetchShop()
    }
    shopDialogVisible.value = false
  } finally {
    shopSubmitting.value = false
  }
}

function initCharts() {
  // 初始化评分分布图表
  if (ratingChartRef.value) {
    ratingChart = echarts.init(ratingChartRef.value)
  }
  
  // 初始化评价趋势图表
  if (reviewChartRef.value) {
    reviewChart = echarts.init(reviewChartRef.value)
  }
  
  // 初始化流量趋势图表
  if (trafficChartRef.value) {
    trafficChart = echarts.init(trafficChartRef.value)
  }
  
  updateCharts()
}

function updateCharts() {
  // 更新评分分布图表
  if (ratingChart) {
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: chartData.value.ratingDistribution.map(item => item.name)
      },
      series: [
        {
          name: '评分分布',
          type: 'pie',
          radius: '60%',
          center: ['50%', '50%'],
          data: chartData.value.ratingDistribution,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          itemStyle: {
            color: function(params) {
              const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de']
              return colors[params.dataIndex]
            }
          }
        }
      ]
    }
    ratingChart.setOption(option)
  }
  
  // 更新评价趋势图表
  if (reviewChart) {
    const option = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['评价数']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: chartData.value.reviewTrend.dates
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '评价数',
          type: 'line',
          data: chartData.value.reviewTrend.counts,
          smooth: true,
          lineStyle: {
            color: '#5470c6'
          }
        }
      ]
    }
    reviewChart.setOption(option)
  }
  
  // 更新流量趋势图表
  if (trafficChart) {
    const option = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['浏览量']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: chartData.value.trafficTrend.dates
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '浏览量',
          type: 'bar',
          data: chartData.value.trafficTrend.views,
          itemStyle: {
            color: '#91cc75'
          }
        }
      ]
    }
    trafficChart.setOption(option)
  }
}

async function fetchAnalytics() {
  analyticsLoading.value = true
  try {
    // 模拟数据，实际应从后端API获取
    statsData.value = {
      totalReviews: 128,
      averageRating: 4.5,
      positiveRate: 85,
      monthlyGrowth: 12
    }
    
    // 模拟评分分布数据
    chartData.value.ratingDistribution = [
      { value: 45, name: '5星' },
      { value: 35, name: '4星' },
      { value: 15, name: '3星' },
      { value: 3, name: '2星' },
      { value: 2, name: '1星' }
    ]
    
    // 模拟评价趋势数据
    chartData.value.reviewTrend = {
      dates: ['1月', '2月', '3月', '4月', '5月', '6月'],
      counts: [15, 20, 18, 25, 30, 20]
    }
    
    // 模拟流量趋势数据
    chartData.value.trafficTrend = {
      dates: ['1月', '2月', '3月', '4月', '5月', '6月'],
      views: [120, 150, 130, 180, 200, 160]
    }
    
    // 更新图表
    updateCharts()
  } finally {
    analyticsLoading.value = false
  }
}

async function fetchReviews() {
  if (!shopData.value) return
  
  reviewLoading.value = true
  try {
    const res = await reviewApi.listByShop(shopData.value.id, reviewPage.value)
    reviews.value = res.records || []
    reviewTotal.value = res.total || 0
  } finally {
    reviewLoading.value = false
  }
}

function handleReviewSort() {
  reviewPage.value = 1
  fetchReviews()
}

function openReplyDialog(row) {
  currentReview.value = row
  // 如果已有回复，显示之前的回复内容
  replyContent.value = row.merchantReply || ''
  replyDialogVisible.value = true
}

async function submitReply() {
  if (!replyContent.value.trim()) {
    return ElMessage.warning('请输入回复内容')
  }
  
  replySubmitting.value = true
  try {
    await reviewApi.merchantReply(currentReview.value.id, replyContent.value.trim())
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    replyContent.value = ''
    currentReview.value = null
    // 刷新评价列表
    await fetchReviews()
  } catch (error) {
    ElMessage.error('回复失败，请重试')
  } finally {
    replySubmitting.value = false
  }
}

onMounted(async () => {
  await fetchShop()
  await fetchAnalytics()
  if (shopData.value) {
    await fetchReviews()
  }
  
  // 初始化图表
  setTimeout(() => {
    initCharts()
  }, 100)
  
  // 监听窗口大小变化，重新调整图表大小
  window.addEventListener('resize', () => {
    ratingChart?.resize()
    reviewChart?.resize()
    trafficChart?.resize()
  })
  
  // 监听路由变化，切换到指定标签页
  watch(() => route.query.tab, (newTab) => {
    if (newTab && newTab !== activeTab.value) {
      activeTab.value = newTab
    }
  })
})
</script>

<template>
  <div class="merchant-page">
    <el-card>
      <template #header>商家管理后台</template>
      <el-tabs v-model="activeTab">

        <!-- 店铺管理 -->
        <el-tab-pane label="店铺基础资料" name="shop">
          <div style="margin-bottom:16px">
            <el-button type="primary" @click="openEditShop">
              <el-icon><Edit /></el-icon> 编辑店铺资料
            </el-button>
          </div>
          
          <el-card v-loading="shopLoading">
            <template v-if="shopData">
              <div class="shop-info-item">
                <span class="label">店铺名称：</span>
                <span>{{ shopData.name }}</span>
              </div>
              <div class="shop-info-item">
                <span class="label">分类：</span>
                <span>{{ shopData.category }}</span>
              </div>
              <div class="shop-info-item">
                <span class="label">地址：</span>
                <span>{{ shopData.address }}</span>
              </div>
              <div class="shop-info-item">
                <span class="label">电话：</span>
                <span>{{ shopData.phone || '未设置' }}</span>
              </div>
              <div class="shop-info-item">
                <span class="label">营业时间：</span>
                <span>{{ shopData.businessHours || '未设置' }}</span>
              </div>
              <div class="shop-info-item">
                <span class="label">简介：</span>
                <span>{{ shopData.description || '未设置' }}</span>
              </div>
              <div class="shop-info-item">
                <span class="label">学生折扣：</span>
                <el-tag :type="shopData.studentDiscount ? 'warning' : 'info'" size="small">
                  {{ shopData.studentDiscount ? '有' : '无' }}
                </el-tag>
              </div>
              <div class="shop-info-item">
                <span class="label">评分：</span>
                <span>{{ shopData.score }}</span>
              </div>
              <div class="shop-info-item">
                <span class="label">评价数：</span>
                <span>{{ shopData.reviewCount }}</span>
              </div>
            </template>
            <el-empty v-else description="暂无店铺信息" />
          </el-card>
        </el-tab-pane>

        <!-- 数据分析 -->
        <el-tab-pane label="数据分析" name="analytics">
          <el-card v-loading="analyticsLoading">
            <template #header>
              <div style="display:flex;justify-content:space-between;align-items:center">
                <span>运营数据概览</span>
                <el-button type="primary" size="small" @click="fetchAnalytics">
                  <el-icon><Refresh /></el-icon> 刷新数据
                </el-button>
              </div>
            </template>
            
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-value">{{ statsData?.totalReviews || 0 }}</div>
                <div class="stat-label">总评价数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ statsData?.averageRating || 0 }}</div>
                <div class="stat-label">平均评分</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ statsData?.positiveRate || 0 }}%</div>
                <div class="stat-label">好评率</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">+{{ statsData?.monthlyGrowth || 0 }}%</div>
                <div class="stat-label">月增长率</div>
              </div>
            </div>
            
            <div class="chart-section">
              <h3>评分分布</h3>
              <div class="chart-container">
                <div ref="ratingChartRef" style="width: 100%; height: 300px;"></div>
              </div>
            </div>
            
            <div class="chart-section">
              <h3>评价趋势</h3>
              <div class="chart-container">
                <div ref="reviewChartRef" style="width: 100%; height: 300px;"></div>
              </div>
            </div>
            
            <div class="chart-section">
              <h3>流量趋势</h3>
              <div class="chart-container">
                <div ref="trafficChartRef" style="width: 100%; height: 300px;"></div>
              </div>
            </div>
          </el-card>
        </el-tab-pane>

        <!-- 评价管理 -->
        <el-tab-pane label="评价管理" name="reviews">
          <el-card>
            <template #header>
              <div style="display:flex;justify-content:space-between;align-items:center">
                <span>用户评价管理</span>
                <div style="display:flex;gap:8px">
                  <el-select v-model="reviewQuery.sortBy" placeholder="排序方式" @change="handleReviewSort">
                    <el-option label="按时间" value="time" />
                    <el-option label="按评分" value="rating" />
                  </el-select>
                  <el-select v-model="reviewQuery.order" placeholder="排序顺序" @change="handleReviewSort">
                    <el-option label="最新" value="desc" />
                    <el-option label="最早" value="asc" />
                  </el-select>
                  <el-select v-model="reviewQuery.rating" placeholder="评分筛选" @change="handleReviewSort">
                    <el-option label="全部" value="" />
                    <el-option label="5星" value="5" />
                    <el-option label="4星" value="4" />
                    <el-option label="3星" value="3" />
                    <el-option label="2星" value="2" />
                    <el-option label="1星" value="1" />
                  </el-select>
                </div>
              </div>
            </template>
            
            <el-table :data="reviews" v-loading="reviewLoading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column label="评分" width="120">
                <template #default="{ row }">
                  <el-rate :model-value="row.rating" disabled size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
              <el-table-column label="商家回复" min-width="200">
                <template #default="{ row }">
                  <div v-if="row.merchantReply" style="color: #67c23a;">
                    <el-tag type="success" size="small" style="margin-bottom: 4px;">已回复</el-tag>
                    <div style="font-size: 12px; color: #666;">{{ row.merchantReply }}</div>
                    <div style="font-size: 11px; color: #999;">{{ row.merchantReplyTime?.slice(0, 19) }}</div>
                  </div>
                  <el-tag v-else type="info" size="small">未回复</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="评价时间" width="180">
                <template #default="{ row }">
                  {{ row.createdAt?.slice(0, 19) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button size="small" :type="row.merchantReply ? 'info' : 'primary'" @click="openReplyDialog(row)">
                    {{ row.merchantReply ? '修改回复' : '回复' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <el-pagination
              v-model:current-page="reviewPage"
              :page-size="10"
              :total="reviewTotal"
              layout="prev, pager, next"
              @current-change="fetchReviews"
              style="margin-top:16px;justify-content:center;display:flex"
            />
          </el-card>
        </el-tab-pane>

        <!-- 优惠管理 -->
        <el-tab-pane label="优惠管理" name="discounts">
          <el-empty description="优惠管理功能开发中" />
        </el-tab-pane>

        <!-- 订单管理 -->
        <el-tab-pane label="订单管理" name="orders">
          <el-empty description="订单管理功能开发中" />
        </el-tab-pane>

      </el-tabs>
    </el-card>

    <!-- 回复评价弹窗 -->
    <el-dialog v-model="replyDialogVisible" title="回复用户评价" width="560px">
      <div v-if="currentReview" style="margin-bottom: 20px; padding: 16px; background: #f5f7fa; border-radius: 8px;">
        <div style="margin-bottom: 8px;">
          <span style="font-weight: bold;">用户评分：</span>
          <el-rate :model-value="currentReview.rating" disabled size="small" />
        </div>
        <div>
          <span style="font-weight: bold;">评价内容：</span>
          <span>{{ currentReview.content }}</span>
        </div>
      </div>
      <el-form label-width="80px">
        <el-form-item label="回复内容">
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="4"
            placeholder="请输入您的回复内容..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="replySubmitting" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>

    <!-- 编辑店铺弹窗 -->
    <el-dialog v-model="shopDialogVisible" title="编辑店铺资料" width="560px">
      <el-form :model="shopForm" label-width="90px">
        <el-form-item label="店铺名称">
          <el-input v-model="shopForm.name" placeholder="请输入店铺名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="shopForm.category" placeholder="选择分类" style="width:100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="shopForm.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="shopForm.phone" placeholder="联系电话（选填）" />
        </el-form-item>
        <el-form-item label="营业时间">
          <el-input v-model="shopForm.businessHours" placeholder="如：09:00-21:00（选填）" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="shopForm.description" type="textarea" :rows="3" placeholder="店铺简介（选填）" />
        </el-form-item>
        <el-form-item label="学生折扣">
          <el-switch v-model="shopForm.studentDiscount" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shopDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="shopSubmitting" @click="submitShop">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.merchant-page { max-width: 1200px; margin: 0 auto; padding: 24px; }
.shop-info-item { display: flex; margin-bottom: 12px; align-items: center; }
.shop-info-item .label { width: 100px; font-weight: bold; color: #666; }
.shop-info-item span { flex: 1; }

/* 数据分析样式 */
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
.stat-item { background: #f5f7fa; padding: 20px; border-radius: 8px; text-align: center; }
.stat-value { font-size: 24px; font-weight: bold; color: #1890ff; margin-bottom: 8px; }
.stat-label { font-size: 14px; color: #666; }

.chart-section { margin-bottom: 30px; }
.chart-section h3 { margin-bottom: 16px; font-size: 16px; color: #333; }
.chart-container { background: #f5f7fa; padding: 20px; border-radius: 8px; }

/* 评价管理样式 */
.el-table { margin-top: 16px; }
.el-pagination { margin-top: 16px; }
</style>