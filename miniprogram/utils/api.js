function getBaseUrl() {
  const app = getApp()
  return app.globalData.env === 'prod' ? app.globalData.prodApiBaseUrl : app.globalData.apiBaseUrl
}

let loginPromise = null

function ensureLogin(options = {}) {
  const app = getApp()
  if (options.auth === false) {
    return Promise.resolve(null)
  }
  if (app.globalData.userToken) {
    return Promise.resolve(app.globalData.userToken)
  }
  if (loginPromise) {
    return loginPromise
  }
  loginPromise = new Promise((resolve, reject) => {
    wx.request({
      url: `${getBaseUrl()}/api/app/auth/wx-login`,
      method: 'POST',
      data: { code: app.globalData.mockLoginCode || 'mock-code' },
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300 && res.data && res.data.success !== false) {
          const session = res.data.data
          app.globalData.userToken = session.token
          app.globalData.mockUserId = session.userId
          resolve(session.token)
        } else {
          const message = res.data && res.data.message ? res.data.message : `login failed: ${res.statusCode}`
          reject(new Error(message))
        }
      },
      fail: reject,
      complete() {
        loginPromise = null
      }
    })
  })
  return loginPromise
}

function request(path, options = {}) {
  const app = getApp()
  return ensureLogin(options).then(() => new Promise((resolve, reject) => {
    const header = Object.assign({}, options.header || {})
    if (app.globalData.userToken && options.auth !== false) {
      header.Authorization = `Bearer ${app.globalData.userToken}`
    }
    wx.request({
      url: `${getBaseUrl()}${path}`,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300 && res.data && res.data.success !== false) {
          resolve(res.data)
        } else {
          const message = res.data && res.data.message ? res.data.message : `request failed: ${res.statusCode}`
          reject(new Error(message))
        }
      },
      fail: reject
    })
  }))
}

function centText(value) {
  return `￥${((value || 0) / 100).toFixed(2)}`
}

module.exports = {
  request,
  ensureLogin,
  centText
}
