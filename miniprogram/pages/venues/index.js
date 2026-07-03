const { request } = require('../../utils/api')

Page({
  data: {
    venues: [],
    error: ''
  },
  onLoad() {
    this.loadVenues()
  },
  loadVenues() {
    request('/api/app/venues')
      .then((res) => {
        this.setData({ venues: res.data || [], error: '' })
      })
      .catch(() => {
        this.setData({ venues: [], error: '后端服务未连接' })
      })
  }
})