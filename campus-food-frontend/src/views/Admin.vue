<!-- 管理后台页，提供店铺管理、用户管理和内容审核功能 -->
<script setup>
import { ref, onMounted } from 'vue'
import { admin as adminApi, shop as shopApi, review as reviewApi, note as noteApi } from '../api/index'
import { ElMessage } from 'element-plus'

const activeTab = ref('shops')

// 店铺管理
const shops = ref([])
const shopTotal = ref(0)
const shopPage = ref(1)
const shopLoading = ref(false)
const shopDialogVisible = ref(false)
const editingShop = ref(null)
const shopForm = ref({ name: '', category: '', address: '', phone: '', description: '', businessHours: '', studentDiscount: 0 })
const categories = ['快餐', '中餐', '西餐', '小吃', '饮品', '甜品', '火锅', '烧烤']
const shopSubmitting = ref(false)

async function fetchShops() {
  shopLoading.value = true
  try {
    const res = await shopApi.list({ page: shopPage.value, size: 10 })
    shops.value = res.records || []
    shopTotal.value = res.total || 0
  } finally {
    shopLoading.value = false
  }
}

function openAddShop() {
  editingShop.value = null
  shopForm.value = { name: '', category: '', address: '', phone: '', description: '', businessHours: '', studentDiscount: 0 }
  shopDialogVisible.value = true
}

function openEditShop(row) {
  editingShop.value = row
  shopForm.value = { name: row.name, category: row.category, address: row.address, phone: row.phone || '', description: row.description || '', businessHours: row.businessHours || '', studentDiscount: row.studentDiscount || 0 }
  shopDialogVisible.value = true
}

async function submitShop() {
  if (!shopForm.value.name || !shopForm.value.category || !shopForm.value.address) {
    return ElMessage.warning('请填写店铺名称、分类和地址')
  }
  shopSubmitting.value = true
  try {
    if (editingShop.value) {
      await shopApi.update(editingShop.value.id, shopForm.value)
      ElMessage.success('更新成功')
    } else {
      await shopApi.create(shopForm.value)
      ElMessage.success('添加成功')
    }
    shopDialogVisible.value = false
    await fetchShops()
  } finally {
    shopSubmitting.value = false
  }
}

async function deleteShop(id) {
  await shopApi.delete(id)
  ElMessage.success('已删除')
  await fetchShops()
}

async function toggleShopStatus(row) {
  const newStatus = row.status === 0 ? 1 : 0
  await shopApi.toggleStatus(row.id, newStatus)
  row.status = newStatus
  ElMessage.success('操作成功')
}

// 用户管理
const users = ref([])
const userTotal = ref(0)
const userQuery = ref({ keyword: '', status: null, page: 1, size: 10 })
const userLoading = ref(false)

async function fetchUsers() {
  userLoading.value = true
  try {
    const res = await adminApi.users(userQuery.value)
    users.value = res.records || []
    userTotal.value = res.total || 0
  } finally {
    userLoading.value = false
  }
}

async function toggleUserStatus(row) {
  const newStatus = row.status === 0 ? 1 : 0
  await adminApi.toggleUserStatus(row.id, newStatus)
  row.status = newStatus
  ElMessage.success('操作成功')
}

// 审核
const pendingReviews = ref([])
const pendingNotes = ref([])

async function fetchPendingReviews() {
  const res = await reviewApi.pending()
  pendingReviews.value = res.records || []
}

async function fetchPendingNotes() {
  const res = await noteApi.list({ page: 1, size: 20 })
  pendingNotes.value = (res.records || []).filter(n => n.status === 1)
}

async function reviewNote(id, status) {
  await noteApi.adminReview(id, status)
  await fetchPendingNotes()
  ElMessage.success(status === 2 ? '已通过' : '已拒绝')
}

async function reviewReview(id, status) {
  await reviewApi.adminReview(id, status)
  await fetchPendingReviews()
  ElMessage.success(status === 1 ? '已通过' : '已拒绝')
}

onMounted(async () => {
  await fetchShops()
  await fetchUsers()
  await fetchPendingReviews()
  await fetchPendingNotes()
})
</script>

<template>
  <div class="admin-page">
    <el-card>
      <template #header>管理后台</template>
      <el-tabs v-model="activeTab">

        <!-- 店铺管理 -->
        <el-tab-pane label="店铺管理" name="shops">
          <div style="margin-bottom:16px">
            <el-button type="primary" @click="openAddShop">
              <el-icon><Plus /></el-icon> 添加店铺
            </el-button>
          </div>
          <el-table :data="shops" v-loading="shopLoading">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="name" label="店铺名称" />
            <el-table-column prop="category" label="分类" width="80" />
            <el-table-column prop="address" label="地址" show-overflow-tooltip />
            <el-table-column prop="score" label="评分" width="70" />
            <el-table-column prop="reviewCount" label="评价数" width="80" />
            <el-table-column label="学生折扣" width="90">
              <template #default="{ row }">
                <el-tag :type="row.studentDiscount ? 'warning' : 'info'" size="small">
                  {{ row.studentDiscount ? '有' : '无' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
                  {{ row.status === 0 ? '正常' : '下线' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" @click="openEditShop(row)">编辑</el-button>
                <el-button size="small" :type="row.status === 0 ? 'warning' : 'success'" @click="toggleShopStatus(row)">
                  {{ row.status === 0 ? '下线' : '上线' }}
                </el-button>
                <el-button size="small" type="danger" @click="deleteShop(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="shopPage"
            :page-size="10"
            :total="shopTotal"
            layout="prev, pager, next"
            @current-change="fetchShops"
            style="margin-top:16px;justify-content:center;display:flex"
          />
        </el-tab-pane>

        <!-- 用户管理 -->
        <el-tab-pane label="用户管理" name="users">
          <div style="display:flex;gap:12px;margin-bottom:16px">
            <el-input v-model="userQuery.keyword" placeholder="搜索学号/姓名" clearable style="width:220px" />
            <el-select v-model="userQuery.status" placeholder="状态" clearable style="width:120px">
              <el-option label="正常" :value="0" />
              <el-option label="禁用" :value="1" />
            </el-select>
            <el-button type="primary" @click="fetchUsers">搜索</el-button>
          </div>
          <el-table :data="users" v-loading="userLoading">
            <el-table-column prop="studentId" label="学号" />
            <el-table-column prop="realName" label="姓名" />
            <el-table-column prop="role" label="角色" width="100">
              <template #default="{ row }">
                <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">{{ row.role }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" :type="row.status === 0 ? 'danger' : 'success'" @click="toggleUserStatus(row)">
                  {{ row.status === 0 ? '禁用' : '启用' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="userQuery.page"
            :page-size="userQuery.size"
            :total="userTotal"
            layout="prev, pager, next"
            @current-change="fetchUsers"
            style="margin-top:16px;justify-content:center;display:flex"
          />
        </el-tab-pane>

        <!-- 评价审核 -->
        <el-tab-pane :label="`评价审核(${pendingReviews.length})`" name="reviews">
          <el-table :data="pendingReviews">
            <el-table-column prop="shopId" label="店铺ID" width="100" />
            <el-table-column prop="rating" label="评分" width="120">
              <template #default="{ row }">
                <el-rate :model-value="row.rating" disabled size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="content" label="内容" show-overflow-tooltip />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button size="small" type="success" @click="reviewReview(row.id, 1)">通过</el-button>
                <el-button size="small" type="danger" @click="reviewReview(row.id, 2)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!pendingReviews.length" description="暂无待审核评价" />
        </el-tab-pane>

        <!-- 笔记审核 -->
        <el-tab-pane :label="`笔记审核(${pendingNotes.length})`" name="notes">
          <el-table :data="pendingNotes">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="type" label="类型" width="80" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button size="small" type="success" @click="reviewNote(row.id, 2)">通过</el-button>
                <el-button size="small" type="danger" @click="reviewNote(row.id, 3)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!pendingNotes.length" description="暂无待审核笔记" />
        </el-tab-pane>

      </el-tabs>
    </el-card>

    <!-- 添加/编辑店铺弹窗 -->
    <el-dialog v-model="shopDialogVisible" :title="editingShop ? '编辑店铺' : '添加店铺'" width="560px">
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
.admin-page { max-width: 1200px; margin: 0 auto; padding: 24px; }
</style>
