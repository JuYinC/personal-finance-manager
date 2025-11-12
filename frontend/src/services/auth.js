import request from '@/utils/request'

export const authAPI = {
  login(data) {
    return request.post('/auth/login', data)
  },

  register(data) {
    return request.post('/auth/register', data)
  }
}
