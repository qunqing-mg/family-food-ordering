<template>
  <view class="page">
    <!-- 个人信息区 -->
    <view class="profile-section">
      <!-- 微信原生头像选择 -->
      <button open-type="chooseAvatar" @chooseavatar="onChooseAvatar" class="avatar-btn">
        <image :src="user.avatarUrl || '/static/avatar.png'" class="avatar" mode="aspectFill" />
        <view class="avatar-edit"><text>📷</text></view>
      </button>
      <!-- 昵称（真机用微信原生，模拟器可手动编辑） -->
      <view class="name-row" @tap="openNickEdit">
        <input type="nickname" v-model="user.nickname" @blur="onNicknameSave" class="name-input"
          :placeholder="user.nickname || '点击设置昵称'" :disabled="true" />
        <text class="edit-hint">✏️</text>
      </view>
    </view>

    <!-- 家庭信息 -->
    <view class="info-card">
      <view class="info-row">
        <text class="info-label">🏠 家庭名称</text>
        <text class="info-val">{{ familyName || '未加入' }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">🔑 邀请码</text>
        <text class="info-val invite" @longpress="copyInvite">{{ inviteCode || '—' }}</text>
      </view>
    </view>

    <!-- 菜单 -->
    <view class="menu-list">
      <view class="menu-item" @click="goPage('/pages/stats/stats')">
        <text>📊 数据看板</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="goPage('/pages/manage/manage')">
        <text>📋 菜品管理</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item danger" @click="doLeave">
        <text>🚪 退出家庭</text>
        <text class="arrow">›</text>
      </view>
    </view>

    <!-- 编辑昵称弹窗 -->
    <view class="modal-mask" v-if="showNickEdit" @tap="showNickEdit = false">
      <view class="modal-box" @tap.stop>
        <text class="modal-title">设置昵称</text>
        <input v-model="nickInput" placeholder="输入昵称" class="modal-input" maxlength="20" />
        <view class="modal-btns">
          <button class="btn cancel" @tap="showNickEdit = false">取消</button>
          <button class="btn save" @tap="saveNickname">保存</button>
        </view>
      </view>
    </view>

    <view class="footer"><text>暖食记 · 家的味道</text></view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../../api/request'

const user = ref({})
const familyName = ref('')
const inviteCode = ref('')
const showNickEdit = ref(false)
const nickInput = ref('')

function openNickEdit() {
  nickInput.value = user.value.nickname || ''
  showNickEdit.value = true
}

async function saveNickname() {
  if (!nickInput.value.trim()) return uni.showToast({ title: '昵称不能为空', icon: 'none' })
  try {
    const updated = await api.updateUserProfile({ nickname: nickInput.value.trim() })
    user.value.nickname = updated.nickname
    uni.setStorageSync('user', JSON.stringify(updated))
    showNickEdit.value = false
    uni.showToast({ title: '昵称已更新', icon: 'success' })
  } catch (e) {}
}

function goPage(url) { uni.navigateTo({ url }) }

// 微信原生：选择头像 → 上传 → 保存
async function onChooseAvatar(e) {
  const avatarUrl = e.detail.avatarUrl
  uni.showLoading({ title: '上传中...' })
  try {
    const url = await api.uploadFile(avatarUrl)
    const updated = await api.updateUserProfile({ avatarUrl: url })
    user.value.avatarUrl = updated.avatarUrl
    uni.setStorageSync('user', JSON.stringify(updated))
    uni.hideLoading()
    uni.showToast({ title: '头像已更新', icon: 'success' })
  } catch (e) {
    uni.hideLoading()
  }
}

// 微信原生：昵称输入完成 → 保存
async function onNicknameSave() {
  if (!user.value.nickname) return
  try {
    const updated = await api.updateUserProfile({ nickname: user.value.nickname })
    user.value.nickname = updated.nickname
    uni.setStorageSync('user', JSON.stringify(updated))
  } catch (e) {}
}

function copyInvite() {
  if (inviteCode.value) {
    uni.setClipboardData({ data: inviteCode.value })
    uni.showToast({ title: '邀请码已复制', icon: 'success' })
  }
}

async function doLeave() {
  const res = await uni.showModal({ title: '确认退出？', content: '退出后需要重新加入家庭' })
  if (res.confirm) {
    await api.leaveFamily()
    uni.removeStorageSync('family_info')
    uni.removeStorageSync('cart_items')
    uni.showToast({ title: '已退出', icon: 'success' })
    setTimeout(() => uni.reLaunch({ url: '/pages/index/index' }), 800)
  }
}

onMounted(async () => {
  // 先从缓存读
  const raw = uni.getStorageSync('user')
  if (raw) {
    try { user.value = JSON.parse(raw) } catch (e) {}
  }
  // 从后端同步最新
  try {
    const profile = await api.getUserProfile()
    if (profile) {
      user.value = profile
      uni.setStorageSync('user', JSON.stringify(profile))
    }
  } catch (e) {}
  // 家庭信息
  const cached = uni.getStorageSync('family_info')
  if (cached) {
    try {
      const info = JSON.parse(cached)
      familyName.value = info.name || ''
      inviteCode.value = info.inviteCode || ''
    } catch (e) {}
  }
  try {
    const info = await api.getFamilyInfo()
    if (info) {
      familyName.value = info.name
      inviteCode.value = info.inviteCode
      uni.setStorageSync('family_info', JSON.stringify(info))
    }
  } catch (e) {}
})
</script>

<style scoped>
.page { padding: 24rpx; min-height: 100vh; background: #FFFAF5; }
.profile-section { display: flex; flex-direction: column; align-items: center; padding: 40rpx 0 30rpx; }
.avatar-btn { position: relative; padding: 0; margin: 0; background: none; border: none; line-height: 1; width: auto; }
.avatar-btn::after { border: none; }
.avatar { width: 150rpx; height: 150rpx; border-radius: 50%; background: #F5EDE7; border: 4rpx solid #FFF0E8; display: block; }
.avatar-edit { position: absolute; bottom: 0; right: 0; width: 48rpx; height: 48rpx; background: #E8634A; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24rpx; box-shadow: 0 2rpx 8rpx rgba(232,99,74,0.3); }
.name-row { margin-top: 24rpx; }
.name-input { text-align: center; font-size: 36rpx; font-weight: 700; color: #3D2C26; height: auto; min-height: 50rpx; }

.info-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.info-row { display: flex; justify-content: space-between; padding: 16rpx 0; font-size: 28rpx; border-bottom: 1rpx solid #F0E8E2; }
.info-row:last-child { border-bottom: none; }
.info-label { color: #3D2C26; }
.info-val { color: #9B8579; }
.invite { color: #E8634A; font-weight: 700; letter-spacing: 4rpx; }

.menu-list { background: #fff; border-radius: 16rpx; overflow: hidden; }
.menu-item { display: flex; justify-content: space-between; padding: 28rpx 24rpx; font-size: 28rpx; border-bottom: 1rpx solid #F0E8E2; color: #3D2C26; }
.menu-item:last-child { border-bottom: none; }
.danger { color: #D4452B; }
.arrow { color: #C8B8AF; font-size: 36rpx; }

.name-row { display: flex; align-items: center; justify-content: center; margin-top: 24rpx; }
.name-input { text-align: center; font-size: 36rpx; font-weight: 700; color: #3D2C26; height: auto; min-height: 50rpx; }
.edit-hint { font-size: 28rpx; margin-left: 8rpx; }

.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(61,44,38,0.5); z-index: 200; display: flex; align-items: center; justify-content: center; }
.modal-box { width: 560rpx; background: #fff; border-radius: 24rpx; padding: 40rpx; }
.modal-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 24rpx; display: block; color: #3D2C26; }
.modal-input { border: 1rpx solid #E8DDD6; padding: 18rpx 16rpx; border-radius: 12rpx; font-size: 28rpx; color: #3D2C26; background: #FFFAF5; }
.modal-btns { display: flex; gap: 20rpx; margin-top: 24rpx; }
.btn { flex: 1; padding: 20rpx; border-radius: 12rpx; font-size: 28rpx; text-align: center; border: none; }
.btn.cancel { background: #F5EDE7; color: #9B8579; }
.btn.save { background: #E8634A; color: #fff; }

.footer { text-align: center; padding: 60rpx 0 30rpx; color: #C8B8AF; font-size: 24rpx; }
</style>
