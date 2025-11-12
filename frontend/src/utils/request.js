import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

// Request interceptor
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// Response interceptor
request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('Response error:', error)

    if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 401:
          ElMessage.error('Unauthorized. Please login again.')
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          router.push('/login')
          break
        case 403:
          ElMessage.error('Access forbidden')
          break
        case 404:
          ElMessage.error(data.message || 'Resource not found')
          break
        case 500:
          ElMessage.error('Server error. Please try again later.')
          break
        default:
          ElMessage.error(data.message || 'An error occurred')
      }
    } else if (error.request) {
      ElMessage.error('Network error. Please check your connection.')
    } else {
      ElMessage.error('An error occurred')
    }

    return Promise.reject(error)
  }
)

export default request
