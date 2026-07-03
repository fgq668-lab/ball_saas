const { request, centText } = require('../../utils/api')

Page({
  data: {
    form: {
      paymentOrderId: '',
      venueId: '',
      amountCent: '',
      reason: '用户申请退款'
    },
    refund: null,
    submitting: false
  },
  onLoad(query) {
    this.setData({
      'form.paymentOrderId': query.paymentOrderId || '',
      'form.venueId': query.venueId || '',
      'form.amountCent': query.amountCent || ''
    })
  },
  updatePaymentOrderId(event) { this.setData({ 'form.paymentOrderId': event.detail.value }) },
  updateVenueId(event) { this.setData({ 'form.venueId': event.detail.value }) },
  updateAmountCent(event) { this.setData({ 'form.amountCent': event.detail.value }) },
  updateReason(event) { this.setData({ 'form.reason': event.detail.value }) },
  submitRefund() {
    const paymentOrderId = Number(this.data.form.paymentOrderId)
    const venueId = Number(this.data.form.venueId)
    const amountCent = Number(this.data.form.amountCent)
    if (!paymentOrderId || !venueId || !amountCent) {
      wx.showToast({ title: '请填写支付单、场馆和金额', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    request('/api/app/refunds', {
      method: 'POST',
      data: {
        paymentOrderId,
        venueId,
        amountCent,
        reason: this.data.form.reason || '用户申请退款'
      }
    })
      .then((res) => {
        this.setData({ submitting: false, refund: this.decorateRefund(res.data) })
        wx.showToast({ title: '退款已提交' })
      })
      .catch((error) => {
        this.setData({ submitting: false })
        wx.showToast({ title: error.message || '提交失败', icon: 'none' })
      })
  },
  decorateRefund(refund) {
    return { ...refund, amountText: centText(refund.amountCent) }
  }
})
