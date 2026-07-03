const { request } = require('../../utils/api')

Page({
  data: {
    order: null,
    error: '',
    paying: false
  },
  onLoad(query) {
    this.orderId = query.id
    this.loadOrder()
  },
  loadOrder() {
    request(`/api/app/bookings/${this.orderId}`)
      .then((res) => this.setData({ order: res.data, error: '' }))
      .catch((error) => this.setData({ order: null, error: error.message || '订单详情加载失败' }))
  },
  cancelOrder() {
    request(`/api/app/bookings/${this.orderId}/cancel`, { method: 'POST' })
      .then(() => {
        wx.showToast({ title: '已取消' })
        this.loadOrder()
      })
      .catch((error) => wx.showToast({ title: error.message || '取消失败', icon: 'none' }))
  },
  payOrder() {
    const order = this.data.order
    if (!order || this.data.paying) return
    this.setData({ paying: true })
    request('/api/app/payments/prepay', {
      method: 'POST',
      data: { bookingId: order.id }
    })
      .then((prepayRes) => request(`/api/pay/mock/success/${prepayRes.data.paymentNo}`, { method: 'POST' }))
      .then(() => {
        wx.showToast({ title: '支付成功' })
        this.loadOrder()
      })
      .catch((error) => wx.showToast({ title: error.message || '支付失败', icon: 'none' }))
      .finally(() => this.setData({ paying: false }))
  }
})
