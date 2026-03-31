// 前端 API 模块，封装所有后端接口调用方法
import request from './request'

// 认证
export const auth = {
  register: data => request.post('/api/auth/register', data),
  login: data => request.post('/api/auth/login', data),
  changePassword: (oldPassword, newPassword) =>
    request.post('/api/auth/change-password', null, { params: { oldPassword, newPassword } })
}

// 用户
export const user = {
  profile: () => request.get('/api/user/profile'),
  updateProfile: params => request.put('/api/user/profile', null, { params }),
  uploadAvatar: formData => request.post('/api/user/avatar', formData),
  myNotes: (page = 1, size = 10) => request.get('/api/user/notes', { params: { page, size } }),
  favoriteShops: (folderId = null, page = 1, size = 10) => 
    request.get('/api/user/favorites/shops', { params: { folderId, page, size } }),
  favoriteNotes: (folderId = null, page = 1, size = 10) => 
    request.get('/api/user/favorites/notes', { params: { folderId, page, size } })
}

// 收藏夹
export const favoriteFolder = {
  list: () => request.get('/api/favorite-folders'),
  create: name => request.post('/api/favorite-folders', null, { params: { name } }),
  rename: (id, name) => request.put(`/api/favorite-folders/${id}`, null, { params: { name } }),
  delete: id => request.delete(`/api/favorite-folders/${id}`)
}

// 店铺
export const shop = {
  hot: (size = 5) => request.get('/api/shops/top/like', { params: { size } }),
  list: params => request.get('/api/shops/list', { params }),
  detail: id => request.get(`/api/shops/${id}`),
  discounts: id => request.get(`/api/shops/${id}/discounts`),
  myShop: () => request.get('/api/shops/my'),
  create: data => request.post('/api/shops', data),
  update: (id, data) => request.put(`/api/shops/${id}`, data),
  delete: id => request.delete(`/api/shops/${id}`),
  toggleStatus: (id, status) => request.put(`/api/shops/${id}/status`, null, { params: { status } }),
  // 优惠管理
  createDiscount: (shopId, data) => request.post(`/api/shops/${shopId}/discounts`, data),
  deleteDiscount: (shopId, discountId) => request.delete(`/api/shops/${shopId}/discounts/${discountId}`)
}

// 评价
export const review = {
  create: data => request.post('/api/reviews', data),
  listByShop: (shopId, page = 1, size = 10) =>
    request.get(`/api/reviews/shop/${shopId}`, { params: { page, size } }),
  listPublishedByShop: (shopId, page = 1, size = 10) =>
    request.get(`/api/reviews/shop/${shopId}/published`, { params: { page, size } }),
  listWithOwnReviews: (shopId, page = 1, size = 10) =>
    request.get(`/api/reviews/shop/${shopId}/with-own`, { params: { page, size } }),
  pending: (page = 1, size = 10) => request.get('/api/reviews/pending', { params: { page, size } }),
  adminReview: (id, status) => request.put(`/api/reviews/${id}/review`, null, { params: { status } }),
  merchantReply: (id, content) => request.post(`/api/reviews/${id}/reply`, null, { params: { content } })
}

// 笔记
export const note = {
  list: params => request.get('/api/notes/list', { params }),
  detail: id => request.get(`/api/notes/${id}`),
  publish: data => request.post('/api/notes', data),
  saveDraft: data => request.post('/api/notes/draft', data),
  getDraft: () => request.get('/api/notes/draft'),
  update: (id, data) => request.put(`/api/notes/${id}`, data),
  delete: id => request.delete(`/api/notes/${id}`),
  isLiked: id => request.get(`/api/notes/${id}/liked`),
  adminReview: (id, status) => request.put(`/api/notes/${id}/review`, null, { params: { status } })
}

// 文件上传
export const upload = {
  media: formData => request.post('/api/upload/media', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 评论
export const comment = {
  create: data => request.post('/api/comments', data),
  listByNote: (noteId, page = 1, size = 10) =>
    request.get(`/api/comments/note/${noteId}`, { params: { page, size } }),
  replies: parentId => request.get(`/api/comments/${parentId}/replies`),
  delete: id => request.delete(`/api/comments/${id}`)
}

// 互动
export const interact = {
  like: (targetType, targetId) => request.post(`/api/interact/like/${targetType}/${targetId}`),
  favorite: (targetType, targetId, folderId = null) => 
    request.post(`/api/interact/favorite/${targetType}/${targetId}`, null, { params: { folderId } }),
  isFavorited: (targetType, targetId) => request.get(`/api/interact/favorite/${targetType}/${targetId}`)
}

// 消息
export const message = {
  list: (type, page = 1, size = 20) => request.get('/api/messages', { params: { type, page, size } }),
  unreadCount: () => request.get('/api/messages/unread-count'),
  markRead: type => request.put('/api/messages/read', null, { params: { type } })
}

// 推荐
export const recommend = {
  shops: (size = 10) => request.get('/api/recommend/shops', { params: { size } })
}

// 排行
export const rank = {
  total: (size = 20) => request.get('/api/rank/total', { params: { size } }),
  monthly: (size = 20) => request.get('/api/rank/monthly', { params: { size } }),
  category: (category, size = 20) => request.get('/api/rank/category', { params: { category, size } })
}

// AI
export const ai = {
  chat: data => request.post('/api/ai/chat', data)
}

// 折扣
export const discount = {
  list: (page = 1, size = 10) => request.get('/api/discounts/list', { params: { page, size } })
}

// 管理员
export const admin = {
  users: params => request.get('/api/admin/users', { params }),
  toggleUserStatus: (id, status) => request.put(`/api/admin/users/${id}/status`, null, { params: { status } }),
  stats: () => request.get('/api/admin/stats'),
  pendingShops: (page = 1, size = 10) => request.get('/api/admin/shops/pending', { params: { page, size } }),
  reviewShop: (id, status, remark = '') =>
    request.put(`/api/admin/shops/${id}/review`, null, { params: { status, remark } })
}
