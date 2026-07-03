const { request, centText } = require('../../utils/api')

function pad(value) {
  return value < 10 ? `0${value}` : `${value}`
}

function defaultDate(offsetDays) {
  const date = new Date()
  date.setDate(date.getDate() + offsetDays)
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function toIso(date, time) {
  return `${date}T${time}:00+08:00`
}

Page({
  data: {
    venueId: null,
    courtId: null,
    courtName: '',
    date: defaultDate(1),
    startTime: '10:00',
    endTime: '11:00',
    submitting: false,
    message: '',
    createdOrder: null,
    payment: null
  },

  onLoad(query) {
    this.setData({
      venueId: Number(query.venueId),
      courtId: Number(query.courtId),
      courtName: decodeURIComponent(query.courtName || '')
    })
  },

  onDateChange(event) {
    this.setData({ date: event.detail.value, message: '' })
  },

  onStartChange(event) {
    this.setData({ startTime: event.detail.value, message: '' })
  },

  onEndChange(event) {
    this.setData({ endTime: event.detail.value, message: '' })
  },

  createBooking() {
    if (this.data.submitting) return
    this.setData({ submitting: true, message: '', createdOrder: null, payment: null })
    request('/api/app/bookings', {
      method: 'POST',
      data: {
        venueId: this.data.venueId,
        courtId: this.data.courtId,
        startAt: toIso(this.data.date, this.data.startTime),
        endAt: toIso(this.data.date, this.data.endTime)
      }
    })
      .then((res) => {
        this.setData({
          createdOrder: Object.assign({}, res.data, { priceText: centText(res.data.payableCent) }),
          message: '订单已创建，请在 10 分钟内支付'
        })
      })
      .catch((error) => this.setData({ message: error.message || '创建预约失败' }))
      .finally(() => this.setData({ submitting: false }))
  },

  payCreatedOrder() {
    const order = this.data.createdOrder
    if (!order) return
    this.setData({ submitting: true, message: '' })
    request('/api/app/payments/prepay', {
      method: 'POST',
      data: { bookingId: order.id }
    })
      .then((prepayRes) => {
        const payment = prepayRes.data
        return request(`/api/pay/mock/success/${payment.paymentNo}`, { method: 'POST' })
          .then((payRes) => ({ payment, paid: payRes.data }))
      })
      .then(({ payment }) => {
        this.setData({ payment, message: '模拟支付成功' })
        wx.showToast({ title: '支付成功' })
      })
      .catch((error) => this.setData({ message: error.message || '支付失败' }))
      .finally(() => this.setData({ submitting: false }))
  },

  openOrder() {
    if (!this.data.createdOrder) return
    wx.navigateTo({ url: `/pages/order-detail/index?id=${this.data.createdOrder.id}` })
  }
})
