import request from '@/utils/request'

export const accountAPI = {
  getAll() {
    return request.get('/accounts')
  },

  getById(id) {
    return request.get(`/accounts/${id}`)
  },

  create(data) {
    return request.post('/accounts', data)
  },

  update(id, data) {
    return request.put(`/accounts/${id}`, data)
  },

  delete(id) {
    return request.delete(`/accounts/${id}`)
  }
}
