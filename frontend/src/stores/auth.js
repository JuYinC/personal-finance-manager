import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI } from '@/services/auth'
import { userAPI } from '@/services/user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || null)
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isAuthenticated = computed(() => !!token.value)

  async function login(credentials) {
    try {
      const response = await authAPI.login(credentials)
      token.value = response.token
      user.value = response.user
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(response.user))
      return true
    } catch (error) {
      console.error('Login error:', error)
      throw error
    }
  }

  async function register(userData) {
    try {
      const response = await authAPI.register(userData)
      token.value = response.token
      user.value = response.user
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(response.user))
      return true
    } catch (error) {
      console.error('Registration error:', error)
      throw error
    }
  }

  async function fetchCurrentUser() {
    try {
      const userData = await userAPI.getCurrentUser()
      user.value = userData
      localStorage.setItem('user', JSON.stringify(userData))
    } catch (error) {
      console.error('Fetch user error:', error)
      logout()
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return {
    token,
    user,
    isAuthenticated,
    login,
    register,
    logout,
    fetchCurrentUser
  }
})
