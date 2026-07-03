const { request } = require('../../utils/api')

Page({
  data: {
    orders: [],
    error: ''
  },
  onShow() {
    this.loadOrders()
  },
  loadOrders() {
    request('/api/app/bookings')
      .then((res) => this.setData({ orders: res.data || [], error: '' }))
      .catch((error) => this.setData({ orders: [], error: error.message || '订单加载失败' }))
  },
  openDetail(event) {
    wx.navigateTo({ url: `/pages/order-detail/index?id=${event.currentTarget.dataset.id}` })
  }
})
