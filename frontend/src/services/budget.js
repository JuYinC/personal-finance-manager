import request from '@/utils/request'

export const budgetAPI = {
  getAll(params) {
    return request.get('/budgets', { params })
  },

  createOrUpdate(data) {
    return request.post('/budgets', data)
  },

  delete(id) {
    return request.delete(`/budgets/${id}`)
  }
}
