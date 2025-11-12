import request from '@/utils/request'

export const categoryAPI = {
  getAll() {
    return request.get('/categories')
  },

  create(data) {
    return request.post('/categories', data)
  },

  update(id, data) {
    return request.put(`/categories/${id}`, data)
  },

  delete(id) {
    return request.delete(`/categories/${id}`)
  }
}
