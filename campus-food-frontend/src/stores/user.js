// 用户状态管理 Store，维护登录 Token 和用户信息
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { user as userApi } from '../api/index'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  function setToken(t) {
    token.value = t
    localStorage.setItem('token', t)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  async function fetchProfile() {
    if (!token.value) return
    userInfo.value = await userApi.profile()
  }

  const isLoggedIn = () => !!token.value
  const isAdmin = () => userInfo.value?.role === 'ADMIN'
  const isMerchant = () => userInfo.value?.role === 'MERCHANT'

  return { token, userInfo, setToken, logout, fetchProfile, isLoggedIn, isAdmin, isMerchant }
})
