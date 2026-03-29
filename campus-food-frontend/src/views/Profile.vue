<!-- 个人中心页，展示用户信息、我的笔记和收藏内容 -->
<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { user as userApi, upload as uploadApi, shop as shopApi, favoriteFolder as folderApi } from '../api/index'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('info')
const profileForm = ref({ tastePreference: '', budget: null })
const myNotes = ref([])
const favoriteShops = ref([])
const favoriteNotes = ref([])
const favoriteFolders = ref([])
const currentFolderId = ref(null)
const saving = ref(false)
const showCreateFolderDialog = ref(false)
const newFolderName = ref('')
const myShop = ref(null)
const myShopLoading = ref(false)

// 判断是否是商家用户
const isMerchant = computed(() => userStore.isMerchant())

onMounted(async () => {
  await userStore.fetchProfile()
  const u = userStore.userInfo
  if (u) {
    profileForm.value.tastePreference = u.tastePreference || ''
    profileForm.value.budget = u.budget || null
  }
  
  // 商家用户加载店铺信息，普通用户加载笔记和收藏
  if (isMerchant.value) {
    await loadMyShop()
  } else {
    await loadFolders()
    const [notesRes, fShopsRes, fNotesRes] = await Promise.all([
      userApi.myNotes(), 
      userApi.favoriteShops(currentFolderId.value), 
      userApi.favoriteNotes(currentFolderId.value)
    ])
    myNotes.value = notesRes.records || []
    favoriteShops.value = fShopsRes.records || []
    favoriteNotes.value = fNotesRes.records || []
  }
})

async function loadFolders() {
  favoriteFolders.value = await folderApi.list()
  if (!currentFolderId.value && favoriteFolders.value.length > 0) {
    currentFolderId.value = favoriteFolders.value.find(f => f.isDefault)?.id || favoriteFolders.value[0].id
  }
}

async function switchFolder(folderId) {
  currentFolderId.value = folderId
  const [fShopsRes, fNotesRes] = await Promise.all([
    userApi.favoriteShops(folderId),
    userApi.favoriteNotes(folderId)
  ])
  favoriteShops.value = fShopsRes.records || []
  favoriteNotes.value = fNotesRes.records || []
}

async function createFolder() {
  if (!newFolderName.value.trim()) {
    return ElMessage.warning('请输入收藏夹名称')
  }
  try {
    await folderApi.create(newFolderName.value.trim())
    await loadFolders()
    newFolderName.value = ''
    showCreateFolderDialog.value = false
    ElMessage.success('创建成功')
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

async function deleteFolder(id) {
  try {
    await folderApi.delete(id)
    await loadFolders()
    if (currentFolderId.value === id) {
      currentFolderId.value = favoriteFolders.value[0]?.id
      await switchFolder(currentFolderId.value)
    }
    ElMessage.success('删除成功')
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

async function loadMyShop() {
  myShopLoading.value = true
  try {
    const res = await shopApi.list({ page: 1, size: 1 })
    if (res.records && res.records.length > 0) {
      myShop.value = res.records[0]
    }
  } finally {
    myShopLoading.value = false
  }
}

async function saveProfile() {
  saving.value = true
  try {
    await userApi.updateProfile(profileForm.value)
    await userStore.fetchProfile()
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

async function uploadAvatar(file) {
  const fd = new FormData()
  fd.append('file', file.raw)
  const url = await userApi.uploadAvatar(fd)
  userStore.userInfo.avatar = url
  ElMessage.success('头像更新成功')
}
</script>

<template>
  <div class="profile-page">
    <el-row :gutter="24">
      <el-col :span="6">
        <el-card class="user-card">
          <div style="text-align:center">
            <el-upload action="#" :show-file-list="false" :auto-upload="false" @change="uploadAvatar" accept="image/*">
              <el-avatar :src="userStore.userInfo?.avatar" :size="80" style="cursor:pointer;margin-bottom:12px">
                {{ userStore.userInfo?.username?.[0] }}
              </el-avatar>
            </el-upload>
            <div class="username">{{ userStore.userInfo?.username }}</div>
            <div class="email">{{ userStore.userInfo?.email }}</div>
            <el-tag :type="userStore.isAdmin() ? 'danger' : userStore.isMerchant() ? 'warning' : 'primary'" style="margin-top:8px">
              {{ userStore.isAdmin() ? '管理员' : userStore.isMerchant() ? '商家用户' : '普通用户' }}
            </el-tag>
          </div>
        </el-card>
      </el-col>
      <el-col :span="18">
        <!-- 商家用户显示店铺信息 -->
        <el-card v-if="isMerchant">
          <div v-loading="myShopLoading">
            <div v-if="myShop" class="shop-info">
              <h3>{{ myShop.name }}</h3>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="分类">{{ myShop.category }}</el-descriptions-item>
                <el-descriptions-item label="地址">{{ myShop.address }}</el-descriptions-item>
                <el-descriptions-item label="电话">{{ myShop.phone || '未设置' }}</el-descriptions-item>
                <el-descriptions-item label="营业时间">{{ myShop.businessHours || '未设置' }}</el-descriptions-item>
                <el-descriptions-item label="评分">{{ myShop.score }}</el-descriptions-item>
                <el-descriptions-item label="评价数">{{ myShop.reviewCount }}</el-descriptions-item>
                <el-descriptions-item label="学生折扣">
                  <el-tag :type="myShop.studentDiscount ? 'warning' : 'info'" size="small">
                    {{ myShop.studentDiscount ? '有' : '无' }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
              <div style="margin-top:16px">
                <el-button type="primary" @click="router.push('/merchant')">进入商家后台</el-button>
              </div>
            </div>
            <el-empty v-else description="暂无店铺信息" />
          </div>
        </el-card>
        
        <!-- 普通用户显示笔记和收藏 -->
        <el-card v-else-if="!isMerchant">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="个人信息" name="info">
              <el-form :model="profileForm" label-width="100px" style="max-width:400px">
                <el-form-item label="口味偏好">
                  <el-input v-model="profileForm.tastePreference" placeholder="如：辣、清淡、甜" />
                </el-form-item>
                <el-form-item label="预算（元）">
                  <el-input-number v-model="profileForm.budget" :min="0" :max="1000" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="我的笔记" name="notes">
              <div class="item-list">
                <div v-for="n in myNotes" :key="n.id" class="list-item" @click="router.push(`/notes/${n.id}`)">
                  <span class="item-title">{{ n.title }}</span>
                  <el-tag size="small" :type="n.status === 2 ? 'success' : n.status === 3 ? 'danger' : 'warning'">
                    {{ ['草稿','待审核','已发布','已拒绝'][n.status] }}
                  </el-tag>
                </div>
                <el-empty v-if="!myNotes.length" description="暂无笔记" />
              </div>
            </el-tab-pane>

            <el-tab-pane label="收藏店铺" name="fshops">
              <div style="margin-bottom:16px;display:flex;gap:12px;align-items:center">
                <el-select v-model="currentFolderId" @change="switchFolder" style="width:200px">
                  <el-option 
                    v-for="folder in favoriteFolders" 
                    :key="folder.id" 
                    :label="`${folder.name} (${folder.count || 0})`" 
                    :value="folder.id"
                  />
                </el-select>
                <el-button @click="showCreateFolderDialog = true">新建收藏夹</el-button>
                <el-button 
                  v-if="favoriteFolders.find(f => f.id === currentFolderId)?.isDefault === 0"
                  type="danger" 
                  @click="deleteFolder(currentFolderId)"
                >
                  删除当前收藏夹
                </el-button>
              </div>
              <div class="item-list">
                <div v-for="s in favoriteShops" :key="s.id" class="list-item" @click="router.push(`/shops/${s.id}`)">
                  <span class="item-title">{{ s.name }}</span>
                  <el-tag size="small">{{ s.category }}</el-tag>
                </div>
                <el-empty v-if="!favoriteShops.length" description="暂无收藏" />
              </div>
            </el-tab-pane>

            <el-tab-pane label="收藏笔记" name="fnotes">
              <div class="item-list">
                <div v-for="n in favoriteNotes" :key="n.id" class="list-item" @click="router.push(`/notes/${n.id}`)">
                  <span class="item-title">{{ n.title }}</span>
                </div>
                <el-empty v-if="!favoriteNotes.length" description="暂无收藏" />
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- 创建收藏夹对话框 -->
    <el-dialog v-model="showCreateFolderDialog" title="新建收藏夹" width="400px">
      <el-input v-model="newFolderName" placeholder="请输入收藏夹名称" maxlength="20" show-word-limit />
      <template #footer>
        <el-button @click="showCreateFolderDialog = false">取消</el-button>
        <el-button type="primary" @click="createFolder">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-page { max-width: 1000px; margin: 0 auto; padding: 24px; }
.user-card { text-align: center; }
.username { font-size: 18px; font-weight: bold; margin-bottom: 4px; }
.email { color: #999; font-size: 13px; }
.item-list { display: flex; flex-direction: column; gap: 8px; }
.list-item { display: flex; justify-content: space-between; align-items: center; padding: 10px 12px; background: #f9f9f9; border-radius: 6px; cursor: pointer; }
.list-item:hover { background: #f0f0f0; }
.item-title { font-size: 14px; }

/* 商家店铺信息样式 */
.shop-info { padding: 20px; }
.shop-info h3 { margin: 0 0 16px; font-size: 20px; color: #333; }
</style>
