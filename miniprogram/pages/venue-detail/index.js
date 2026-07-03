const { request } = require('../../utils/api')

Page({
  data: {
    venueId: null,
    venue: null,
    courts: [],
    error: ''
  },

  onLoad(query) {
    this.setData({ venueId: Number(query.id) })
    this.loadDetail()
  },

  loadDetail() {
    const venueId = this.data.venueId
    Promise.all([
      request(`/api/app/venues/${venueId}`),
      request(`/api/app/venues/${venueId}/courts/availability`)
    ])
      .then(([venueRes, courtsRes]) => {
        this.setData({
          venue: venueRes.data,
          courts: courtsRes.data || [],
          error: ''
        })
      })
      .catch((error) => {
        this.setData({ error: error.message || '场馆详情加载失败' })
      })
  },

  openBooking(event) {
    const court = event.currentTarget.dataset.court
    wx.navigateTo({
      url: `/pages/booking/index?venueId=${this.data.venueId}&courtId=${court.id}&courtName=${encodeURIComponent(court.name)}`
    })
  }
})
