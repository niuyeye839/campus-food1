// 前端路由配置，定义页面路径与组件的映射及登录守卫
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../views/Home.vue') },
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/register', component: () => import('../views/Register.vue') },
  { path: '/shops', component: () => import('../views/ShopList.vue') },
  { path: '/shops/:id', component: () => import('../views/ShopDetail.vue') },
  { path: '/notes', component: () => import('../views/NoteList.vue') },
  { path: '/notes/create', component: () => import('../views/NoteEdit.vue'), meta: { auth: true } },
  { path: '/notes/:id/edit', component: () => import('../views/NoteEdit.vue'), meta: { auth: true } },
  { path: '/notes/:id', component: () => import('../views/NoteDetail.vue') },
  { path: '/profile', component: () => import('../views/Profile.vue'), meta: { auth: true } },
  { path: '/messages', component: () => import('../views/Messages.vue'), meta: { auth: true } },
  { path: '/discounts', component: () => import('../views/Discounts.vue') },
  { path: '/admin', component: () => import('../views/Admin.vue'), meta: { auth: true, admin: true } },
  { path: '/merchant', component: () => import('../views/Merchant.vue'), meta: { auth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

import { useUserStore } from '../stores/user'

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.auth && !token) return next('/login')

  // 管理员页面只能管理员访问
  if (to.meta.admin) {
    const userStore = useUserStore()
    if (!userStore.isAdmin()) {
      return next('/')
    }
  }

  next()
})

export default router
