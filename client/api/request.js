const BASE = 'http://127.0.0.1:8080/api'
const SERVER = 'http://127.0.0.1:8080'

export function imgUrl(path) {
  if (!path) return '/static/no-img.png'
  if (path.startsWith('http')) return path
  return SERVER + path
}

function request(options) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    uni.request({
      url: BASE + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? 'Bearer ' + token : ''
      },
      success: (res) => {
        if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.reLaunch({ url: '/pages/index/index' })
          return
        }
        if (res.data.code === 200) {
          resolve(res.data.data)
        } else {
          let msg = res.data.message || '操作失败'
          if (msg.includes('已下架')) msg = '该菜品已下架，请刷新后重试'
          if (msg.includes('截止')) msg = msg.replace('20:00','').trim()
          uni.showToast({ title: msg, icon: 'none', duration: 2500 })
          reject(res.data)
        }
      },
      fail: (err) => {
        const msg = err.errMsg || ''
        if (msg.includes('timeout')) {
          uni.showToast({ title: '网络超时，请检查后端是否启动', icon: 'none', duration: 3000 })
        } else if (msg.includes('fail')) {
          uni.showToast({ title: '网络连接失败，请稍后重试', icon: 'none', duration: 2500 })
        } else {
          uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
        }
        reject(err)
      }
    })
  })
}

export const api = {
  wxLogin: (data) => request({ url: '/auth/wx-login', method: 'POST', data }),
  createFamily: (name) => request({ url: '/family', method: 'POST', data: { name } }),
  joinFamily: (inviteCode) => request({ url: '/family/join', method: 'POST', data: { inviteCode } }),
  getFamilyInfo: () => request({ url: '/family/info' }),
  getMembers: () => request({ url: '/family/members' }),
  leaveFamily: () => request({ url: '/family/leave', method: 'DELETE' }),
  addCategory: (name, sortOrder) => request({ url: '/category', method: 'POST', data: { name, sortOrder } }),
  getCategories: () => request({ url: '/category/list' }),
  addDish: (data) => request({ url: '/dish', method: 'POST', data }),
  getDishes: (categoryId, all) => request({ url: `/dish/list${categoryId ? '?categoryId=' + categoryId + (all ? '&all=1' : '') : (all ? '?all=1' : '')}` }),
  deleteDish: (id) => request({ url: `/dish/${id}`, method: 'DELETE' }),
  updateDish: (id, data) => request({ url: `/dish/${id}`, method: 'PUT', data }),
  toggleDishStatus: (id) => request({ url: `/dish/${id}/status`, method: 'PUT' }),
  createOrder: (data) => request({ url: '/order', method: 'POST', data }),
  getOrders: (type) => request({ url: `/order/list?type=${type || 'all'}` }),
  updateOrderStatus: (id, status) => request({ url: `/order/${id}/status`, method: 'PUT', data: { status } }),
  getUserProfile: () => request({ url: '/user/profile' }),
  updateUserProfile: (data) => request({ url: '/user/profile', method: 'PUT', data }),
  getDashboard: () => request({ url: '/stats/dashboard' }),
  rotateChef: () => request({ url: '/stats/rotate-chef', method: 'POST' }),
  getUnreadCount: () => request({ url: '/noti/unread-count' }),
  getUnreadList: () => request({ url: '/noti/unread-list' }),
  markReadAll: () => request({ url: '/noti/read-all', method: 'POST' }),
  submitFeedback: (itemId, feedback) => request({ url: `/order/item/${itemId}/feedback`, method: 'PUT', data: { feedback } }),
  updateFamilySettings: (data) => request({ url: '/family/settings', method: 'PUT', data }),
  updateCategory: (id, name) => request({ url: `/category/${id}`, method: 'PUT', data: { name } }),
  uploadFile: (filePath) => new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE + '/upload',
      filePath,
      name: 'file',
      header: { 'Authorization': 'Bearer ' + uni.getStorageSync('token') },
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.code === 200) resolve(data.data)
        else { uni.showToast({ title: '上传失败', icon: 'none' }); reject(data) }
      },
      fail: reject
    })
  }),
}

export default request
