<!-- 注册页，提供学号、姓名和密码注册表单 -->
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { auth } from '../api/index'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const form = ref({ studentId: '', creditCode: '', realName: '', password: '', confirmPassword: '', role: 'USER' })
const loading = ref(false)

async function handleRegister() {
  if (form.value.role === 'USER' && !form.value.studentId) {
    return ElMessage.warning('请填写学号')
  }
  if (form.value.role === 'MERCHANT' && !form.value.creditCode) {
    return ElMessage.warning('请填写统一社会信用代码')
  }
  if (!form.value.realName || !form.value.password) {
    return ElMessage.warning('请填写完整信息')
  }
  if (form.value.password !== form.value.confirmPassword) {
    return ElMessage.error('两次密码不一致')
  }
  loading.value = true
  try {
    const res = await auth.register({
      studentId: form.value.role === 'USER' ? form.value.studentId : form.value.creditCode,
      realName: form.value.realName,
      password: form.value.password,
      role: form.value.role
    })
    userStore.setToken(res.token)
    await userStore.fetchProfile()
    ElMessage.success('注册成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <el-card class="register-card">
      <h2 style="text-align:center;margin-bottom:24px">注册</h2>
      <el-form :model="form" @submit.prevent="handleRegister">
        <el-form-item>
          <el-select v-model="form.role" placeholder="选择角色" size="large" style="width:100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="商家" value="MERCHANT" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.role === 'USER'">
          <el-input v-model="form.studentId" placeholder="学号" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item v-if="form.role === 'MERCHANT'">
          <el-input v-model="form.creditCode" placeholder="统一社会信用代码（18位）" prefix-icon="Document" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.realName" placeholder="姓名" prefix-icon="UserFilled" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码（6-20位）" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" size="large" style="width:100%">注册</el-button>
      </el-form>
      <div style="text-align:center;margin-top:16px">
        <el-link @click="router.push('/login')">已有账号？去登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.register-page { display:flex; justify-content:center; align-items:center; min-height:80vh; }
.register-card { width:420px; padding:20px; }
</style>
