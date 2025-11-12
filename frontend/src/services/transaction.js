import request from '@/utils/request'

export const transactionAPI = {
  getAll(params) {
    return request.get('/transactions', { params })
  },

  getById(id) {
    return request.get(`/transactions/${id}`)
  },

  create(data) {
    return request.post('/transactions', data)
  },

  update(id, data) {
    return request.put(`/transactions/${id}`, data)
  },

  delete(id) {
    return request.delete(`/transactions/${id}`)
  }
}
