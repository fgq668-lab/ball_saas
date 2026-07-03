const { request, centText } = require('../../utils/api')

Page({
  data: {
    match: null,
    players: [],
    sharePayments: [],
    shareRefunds: [],
    mySharePayment: null,
    error: '',
    busy: false
  },
  onLoad(query) {
    this.matchId = query.id
    this.loadMatch()
  },
  onShow() {
    if (this.matchId) this.loadMatch()
  },
  loadMatch() {
    Promise.all([
      request(`/api/app/matches/${this.matchId}`),
      request(`/api/app/matches/${this.matchId}/players`),
      request(`/api/app/matches/${this.matchId}/share-payments`),
      request(`/api/app/matches/${this.matchId}/share-refunds`)
    ])
      .then(([matchRes, playersRes, paymentsRes, refundsRes]) => {
        const app = getApp()
        const match = this.decorateMatch(matchRes.data)
        const payments = this.decoratePayments(paymentsRes.data || [])
        this.setData({
          match,
          players: this.decoratePlayers(playersRes.data || []),
          sharePayments: payments,
          shareRefunds: this.decorateRefunds(refundsRes.data || []),
          mySharePayment: payments.find((item) => item.userId === app.globalData.mockUserId) || null,
          error: ''
        })
      })
      .catch((error) => this.setData({ match: null, players: [], sharePayments: [], shareRefunds: [], mySharePayment: null, error: error.message || '约战详情加载失败' }))
  },
  decorateMatch(match) {
    return {
      ...match,
      aaText: centText(match.perUserAmountCent),
      timeText: `${this.formatDate(match.startAt)} - ${this.formatDate(match.endAt)}`
    }
  },
  decoratePlayers(players) {
    return players.map((item) => ({ ...item, joinedText: this.formatDate(item.joinedAt) }))
  },
  decoratePayments(payments) {
    return payments.map((item) => ({ ...item, amountText: centText(item.amountCent) }))
  },
  decorateRefunds(refunds) {
    return refunds.map((item) => ({ ...item, amountText: centText(item.amountCent) }))
  },
  joinMatch() {
    this.runAction(() => request(`/api/app/matches/${this.matchId}/join`, {
      method: 'POST',
      data: {}
    }), '已加入', '加入失败')
  },
  leaveMatch() {
    this.runAction(() => request(`/api/app/matches/${this.matchId}/leave`, {
      method: 'POST',
      data: {}
    }), '已退出', '退出失败')
  },
  createSharePayment() {
    const match = this.data.match
    this.runAction(() => request(`/api/app/matches/${this.matchId}/share-payments`, {
      method: 'POST',
      data: { amountCent: match.perUserAmountCent }
    }), 'AA单已创建', '创建AA单失败')
  },
  payShare() {
    const payment = this.data.mySharePayment
    if (!payment || !payment.paymentNo) {
      wx.showToast({ title: '请先创建AA单', icon: 'none' })
      return
    }
    this.runAction(() => request(`/api/pay/mock/success/${payment.paymentNo}`, { method: 'POST' }), '支付成功', '支付失败')
  },
  cancelNotFormed() {
    this.runAction(() => request(`/api/app/matches/${this.matchId}/cancel-not-formed`, { method: 'POST' }), '已取消约战', '取消失败')
  },
  runAction(task, successTitle, failTitle) {
    if (this.data.busy) return
    this.setData({ busy: true })
    task()
      .then(() => {
        wx.showToast({ title: successTitle })
        this.setData({ busy: false })
        this.loadMatch()
      })
      .catch((error) => {
        this.setData({ busy: false })
        wx.showToast({ title: error.message || failTitle, icon: 'none' })
      })
  },
  formatDate(value) {
    if (!value) return ''
    return String(value).replace('T', ' ').slice(0, 16)
  }
})
