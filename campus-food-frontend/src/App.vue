<!-- 根组件，包含顶部导航栏和路由视图出口 -->
<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from './stores/user'
import { message as msgApi } from './api/index'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const unreadCount = ref(0)

// 判断是否是商家用户，如果是商家用户则不显示导航菜单
const isMerchantPage = computed(() => userStore.isMerchant())

onMounted(async () => {
  if (userStore.isLoggedIn()) {
    await userStore.fetchProfile()
    try { unreadCount.value = await msgApi.unreadCount() } catch {}
  }
})

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="app-layout">
    <el-header class="app-header">
      <div class="logo" @click="router.push('/')">🍜 校园美食</div>
      
      <!-- 非商家端页面显示导航菜单 -->
      <el-menu v-if="!isMerchantPage" mode="horizontal" :ellipsis="false" router class="nav-menu">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/shops">店铺</el-menu-item>
        <el-menu-item index="/notes">笔记</el-menu-item>
        <el-menu-item index="/discounts">优惠</el-menu-item>
      </el-menu>
      
      <div class="header-right">
        <template v-if="userStore.isLoggedIn()">
          <!-- 商家用户点击铃铛显示评价管理，普通用户显示消息 -->
          <el-badge :value="unreadCount || ''" :hidden="!unreadCount || isMerchantPage">
            <el-button text @click="isMerchantPage ? router.push('/merchant?tab=reviews') : router.push('/messages')">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>
          <el-dropdown>
            <el-avatar :src="userStore.userInfo?.avatar" :size="32" style="cursor:pointer">
              {{ userStore.userInfo?.username?.[0] }}
            </el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin()" @click="router.push('/admin')">管理后台</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isMerchant()" @click="router.push('/merchant')">商家后台</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button @click="router.push('/login')">登录</el-button>
          <el-button type="primary" @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>
    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.app-layout { min-height: 100vh; }
.app-header {
  display: flex; align-items: center; gap: 20px;
  background: #fff; border-bottom: 1px solid #eee;
  padding: 0 24px; position: sticky; top: 0; z-index: 100;
}
.logo { font-size: 20px; font-weight: bold; cursor: pointer; white-space: nowrap; color: #e6a23c; }
.nav-menu { flex: 1; border-bottom: none; }
.header-right { display: flex; align-items: center; gap: 12px; margin-left: auto; }
.app-main { background: #f5f5f5; min-height: calc(100vh - 60px); padding: 0; }
</style>