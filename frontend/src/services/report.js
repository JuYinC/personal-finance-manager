import request from '@/utils/request'

export const reportAPI = {
  getSummary(params) {
    return request.get('/reports/summary', { params })
  },

  getByCategory(params) {
    return request.get('/reports/by-category', { params })
  },

  getTrends(params) {
    return request.get('/reports/trends', { params })
  }
}
