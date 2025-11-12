import request from '@/utils/request'

export const userAPI = {
  getCurrentUser() {
    return request.get('/users/me')
  },

  updateProfile(data) {
    return request.put('/users/me', data)
  },

  changePassword(data) {
    return request.put('/users/me/password', data)
  }
}
