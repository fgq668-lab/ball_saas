const { ensureLogin } = require('../../utils/api')

const app = getApp()

Page({
  data: {
    env: app.globalData.env,
    apiBaseUrl: app.globalData.apiBaseUrl,
    userId: app.globalData.mockUserId,
    hasToken: false,
    message: ''
  },

  onShow() {
    this.refreshSession()
  },

  refreshSession() {
    ensureLogin()
      .then(() => {
        this.setData({
          env: app.globalData.env,
          apiBaseUrl: app.globalData.apiBaseUrl,
          userId: app.globalData.mockUserId,
          hasToken: !!app.globalData.userToken,
          message: '已连接测试账号'
        })
      })
      .catch((error) => {
        this.setData({ message: error.message || '登录失败' })
      })
  }
})
