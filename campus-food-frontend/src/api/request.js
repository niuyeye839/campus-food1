// Axios 请求封装，统一处理 Token 注入、响应解包和错误提示
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '',
  timeout: 60000  // AI 推理模型响应较慢，设为 60 秒
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  res => {
    const data = res.data
    if (data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(data)
    }
    return data.data
  },
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    ElMessage.error(err.response?.data?.message || '网络错误')
    return Promise.reject(err)
  }
)

export default request
