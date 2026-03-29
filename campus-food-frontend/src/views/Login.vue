<!-- 登录页，提供账号密码登录表单 -->
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { auth } from '../api/index'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const form = ref({ account: '', password: '', role: 'USER' })
const loading = ref(false)

async function handleLogin() {
  loading.value = true
  try {
    const res = await auth.login(form.value)
    userStore.setToken(res.token)
    await userStore.fetchProfile()
    ElMessage.success('登录成功')
    if (res.role === 'ADMIN') {
      router.push('/admin')
    } else if (res.role === 'MERCHANT') {
      router.push('/merchant')
    } else {
      router.push('/')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 style="text-align:center;margin-bottom:24px">登录</h2>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-select v-model="form.role" placeholder="选择角色" size="large" style="width:100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="商家" value="MERCHANT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.account" placeholder="用户名/邮箱" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" size="large" style="width:100%">登录</el-button>
      </el-form>
      <div style="text-align:center;margin-top:16px">
        <el-link @click="router.push('/register')">没有账号？去注册</el-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.login-page { display:flex; justify-content:center; align-items:center; min-height:80vh; }
.login-card { width:400px; padding:20px; }
</style>
