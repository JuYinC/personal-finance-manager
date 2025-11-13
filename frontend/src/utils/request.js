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
    // Log detailed error information for debugging
    console.error('Response error:', error)

    let errorMessage = 'An error occurred'
    let errorDetails = null

    if (error.response) {
      const { status, data, config } = error.response
      const url = config?.url || 'unknown'

      // Build detailed error information
      errorDetails = {
        status,
        url,
        method: config?.method?.toUpperCase(),
        data: data
      }

      console.error('Error details:', errorDetails)

      switch (status) {
        case 400:
          errorMessage = data.message || 'Bad request. Please check your input.'
          ElMessage.error({
            message: `${errorMessage} (${status})`,
            duration: 5000
          })
          break
        case 401:
          ElMessage.error('Unauthorized. Please login again.')
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          router.push('/login')
          break
        case 403:
          errorMessage = 'Access forbidden'
          ElMessage.error({
            message: `${errorMessage} (${status})`,
            duration: 5000
          })
          break
        case 404:
          errorMessage = data.message || 'Resource not found'
          ElMessage.error({
            message: `${errorMessage} (${status})`,
            duration: 5000
          })
          break
        case 500:
          errorMessage = data.message || 'Internal server error. Please try again later.'
          ElMessage.error({
            message: `${errorMessage} (${status}) - Endpoint: ${url}`,
            duration: 8000
          })
          break
        case 502:
          errorMessage = 'Bad Gateway. The server is unavailable or returned an invalid response.'
          ElMessage.error({
            message: `${errorMessage} (${status}) - Endpoint: ${url}`,
            duration: 8000,
            showClose: true
          })
          break
        case 503:
          errorMessage = 'Service unavailable. Please try again later.'
          ElMessage.error({
            message: `${errorMessage} (${status})`,
            duration: 5000
          })
          break
        default:
          errorMessage = data.message || 'An error occurred'
          ElMessage.error({
            message: `${errorMessage} (${status}) - ${url}`,
            duration: 5000
          })
      }
    } else if (error.request) {
      console.error('Network error - no response received:', error.request)
      ElMessage.error({
        message: 'Network error. Please check your connection.',
        duration: 5000
      })
    } else {
      console.error('Error setting up request:', error.message)
      ElMessage.error({
        message: `Error: ${error.message}`,
        duration: 5000
      })
    }

    return Promise.reject(error)
  }
)

export default request
