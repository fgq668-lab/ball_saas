const { request, centText } = require('../../utils/api')

Page({
  data: {
    matches: [],
    error: '',
    creating: false,
    form: {
      bookingOrderId: '',
      minPlayers: 2,
      maxPlayers: 4
    }
  },
  onLoad(query) {
    if (query.bookingId) {
      this.setData({ 'form.bookingOrderId': String(query.bookingId) })
    }
  },
  onShow() {
    this.loadMatches()
  },
  loadMatches() {
    request('/api/app/matches')
      .then((res) => this.setData({ matches: this.decorateMatches(res.data || []), error: '' }))
      .catch((error) => this.setData({ matches: [], error: error.message || '约战加载失败' }))
  },
  decorateMatches(matches) {
    return matches.map((item) => ({
      ...item,
      aaText: centText(item.perUserAmountCent),
      timeText: `${this.formatDate(item.startAt)} - ${this.formatDate(item.endAt)}`
    }))
  },
  updateBookingId(event) {
    this.setData({ 'form.bookingOrderId': event.detail.value })
  },
  updateMinPlayers(event) {
    this.setData({ 'form.minPlayers': Number(event.detail.value) })
  },
  updateMaxPlayers(event) {
    this.setData({ 'form.maxPlayers': Number(event.detail.value) })
  },
  createMatch() {
    const bookingOrderId = Number(this.data.form.bookingOrderId)
    if (!bookingOrderId) {
      wx.showToast({ title: '请输入预约订单ID', icon: 'none' })
      return
    }
    this.setData({ creating: true })
    request('/api/app/matches', {
      method: 'POST',
      data: {
        bookingOrderId,
        minPlayers: Number(this.data.form.minPlayers),
        maxPlayers: Number(this.data.form.maxPlayers)
      }
    })
      .then((res) => {
        wx.showToast({ title: '约战已创建' })
        this.setData({ creating: false, 'form.bookingOrderId': '' })
        this.loadMatches()
        wx.navigateTo({ url: `/pages/match-detail/index?id=${res.data.id}` })
      })
      .catch((error) => {
        this.setData({ creating: false })
        wx.showToast({ title: error.message || '创建失败', icon: 'none' })
      })
  },
  openDetail(event) {
    wx.navigateTo({ url: `/pages/match-detail/index?id=${event.currentTarget.dataset.id}` })
  },
  formatDate(value) {
    if (!value) return ''
    return String(value).replace('T', ' ').slice(0, 16)
  }
})
