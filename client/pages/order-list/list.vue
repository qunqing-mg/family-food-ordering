<template>
  <view class="page">
    <view class="tabs">
      <text class="tab" :class="{ active: tab === 'my' }" @tap="tab = 'my'; load()">我下的单</text>
      <text class="tab" :class="{ active: tab === 'all' }" @tap="tab = 'all'; load()">所有订单</text>
    </view>
    <!-- 新订单通知 -->
    <view class="noti-bar" v-if="unreadCount > 0" @tap="showNotis">
      <text class="noti-icon">🔔</text>
      <text class="noti-text">{{ unreadCount }} 条新消息</text>
      <text class="noti-dismiss" @tap.stop="dismissNotis">忽略</text>
    </view>

    <view class="date-tabs">
      <text class="dt" :class="{ active: day === 'today' }" @tap="day = 'today'">今天</text>
      <text class="dt" :class="{ active: day === 'week' }" @tap="day = 'week'">本周</text>
      <text class="dt" :class="{ active: day === 'all' }" @tap="day = 'all'">全部</text>
    </view>

    <view class="order-card" v-for="o in filteredOrders" :key="o.id">
      <view class="oc-header">
        <view class="oc-hleft">
          <text class="oc-status" :class="'status-' + o.status">{{ ['待接单','已接单','已完成','已取消'][o.status] }}</text>
          <text class="oc-user" v-if="o.userNickname"> · {{ o.userNickname }}</text>
        </view>
        <text class="oc-time">{{ fmtTime(o.createTime) }}</text>
      </view>
      <view class="oc-item" v-for="it in o.items" :key="it.id">
        <image :src="imgUrl(it.dishImage)" mode="aspectFill" class="oc-img" />
        <text class="oc-name">{{ it.dishName }}</text>
        <text class="oc-qty">×{{ it.quantity }}</text>
        <view class="oc-feedback" v-if="o.status === 2 && !it.feedback">
          <text class="fb-btn" @tap="submitFeedback(it.id, 1)">👍</text>
          <text class="fb-btn" @tap="submitFeedback(it.id, 2)">👎</text>
        </view>
        <text class="fb-done" v-else-if="it.feedback">{{ it.feedback === 1 ? '👍' : '👎' }}</text>
      </view>
      <view class="oc-remark" v-if="o.remark">
        <text>📝 {{ o.remark }}</text>
      </view>
      <view class="oc-footer">
        <text class="oc-total">{{ o.items?.length || 0 }} 种菜品</text>
        <view class="oc-actions" v-if="o.status === 0">
          <text class="oc-btn acc" @tap="update(o.id, 1)">接单</text>
          <text class="oc-btn danger" @tap="update(o.id, 3)">取消</text>
        </view>
        <view class="oc-actions" v-else-if="o.status === 1">
          <text class="oc-btn done" @tap="update(o.id, 2)">完成</text>
        </view>
        <text class="oc-btn reorder" v-else-if="o.status === 2" @tap="reOrder(o)">再来一单</text>
      </view>
    </view>
    <view class="empty" v-if="filteredOrders.length === 0">
      <text class="empty-icon">🍽️</text>
      <text>还没有订单，快去点餐吧</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api, imgUrl } from '../../api/request'
import { useCartStore } from '../../stores/cart'

const cart = useCartStore()
const tab = ref('my')
const day = ref('all')
const orders = ref([])
const unreadCount = ref(0)

async function checkNotis() {
  try { unreadCount.value = await api.getUnreadCount() || 0 } catch (e) {}
}

function showNotis() {
  api.markReadAll()
  unreadCount.value = 0
  uni.showToast({ title: '消息已读', icon: 'success' })
  load()
}
function dismissNotis() { api.markReadAll(); unreadCount.value = 0 }

async function submitFeedback(itemId, fb) {
  await api.submitFeedback(itemId, fb)
  load()
  uni.showToast({ title: fb === 1 ? '谢谢反馈！' : '收到，会改进', icon: 'none' })
}

function fmtTime(t) {
  if (!t) return ''
  const d = new Date(t.replace('T', ' '))
  const pad = n => String(n).padStart(2, '0')
  return `${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function isSameDay(a, b) {
  return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate()
}

const filteredOrders = computed(() => {
  if (day.value === 'all') return orders.value
  const now = new Date()
  return orders.value.filter(o => {
    const d = new Date(o.createTime.replace('T', ' '))
    if (day.value === 'today') return isSameDay(d, now)
    if (day.value === 'week') {
      const weekAgo = new Date(now.getTime() - 7 * 86400000)
      return d >= weekAgo
    }
    return true
  })
})

async function load() { orders.value = await api.getOrders(tab.value) || [] }
async function update(id, status) { await api.updateOrderStatus(id, status); load() }

async function reOrder(o) {
  for (const it of o.items) {
    cart.addItem({ id: it.dishId, name: it.dishName, imageUrl: it.dishImage, price: it.price })
  }
  uni.switchTab({ url: '/pages/index/index' })
  uni.showToast({ title: '已加入购物车', icon: 'success' })
}

onMounted(() => {
  load()
  checkNotis()
  setInterval(() => checkNotis(), 10000)
})
</script>

<style scoped>
.page { padding: 24rpx; min-height: 100vh; background: #FFFAF5; }
.tabs { display: flex; background: #fff; border-radius: 16rpx; margin-bottom: 16rpx; overflow: hidden; }
.tab { flex: 1; text-align: center; padding: 28rpx; font-size: 28rpx; color: #9B8579; border-bottom: 4rpx solid transparent; }
.tab.active { color: #E8634A; border-bottom-color: #E8634A; font-weight: 600; }
.noti-bar { display: flex; align-items: center; background: #FFF0E8; border-radius: 12rpx; padding: 16rpx 20rpx; margin-bottom: 16rpx; }
.noti-icon { font-size: 32rpx; margin-right: 12rpx; }
.noti-text { flex: 1; font-size: 26rpx; color: #E8634A; font-weight: 600; }
.noti-dismiss { font-size: 24rpx; color: #9B8579; }

.date-tabs { display: flex; gap: 16rpx; margin-bottom: 24rpx; }
.dt { padding: 10rpx 28rpx; border-radius: 30rpx; font-size: 24rpx; color: #9B8579; background: #fff; }
.dt.active { background: #FFF0E8; color: #E8634A; font-weight: 600; }

.order-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 24rpx; }
.oc-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.oc-hleft { display: flex; align-items: center; }
.oc-status { font-size: 28rpx; font-weight: 600; color: #E8634A; }
.oc-status.status-3 { color: #C8B8AF; }
.oc-status.status-2 { color: #5B9A5E; }
.oc-user { font-size: 24rpx; color: #9B8579; }
.oc-time { font-size: 24rpx; color: #9B8579; }
.oc-item { display: flex; align-items: center; padding: 16rpx 0; border-bottom: 1rpx solid #F0E8E2; }
.oc-img { width: 72rpx; height: 72rpx; border-radius: 10rpx; background: #F5EDE7; }
.oc-name { flex: 1; font-size: 26rpx; margin-left: 16rpx; color: #3D2C26; }
.oc-qty { font-size: 24rpx; color: #9B8579; margin: 0 16rpx; }
.oc-feedback { display: flex; gap: 8rpx; margin-left: 8rpx; }
.fb-btn { font-size: 32rpx; padding: 4rpx; }
.fb-done { font-size: 28rpx; margin-left: 8rpx; }
.oc-price { font-size: 26rpx; color: #E8634A; font-weight: 600; }
.oc-remark { padding: 10rpx 0; font-size: 24rpx; color: #9B8579; border-bottom: 1rpx solid #F0E8E2; }
.oc-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 16rpx; }
.oc-total { font-size: 28rpx; font-weight: 600; color: #3D2C26; }
.oc-actions { display: flex; gap: 16rpx; }
.oc-btn { padding: 8rpx 28rpx; border-radius: 30rpx; font-size: 24rpx; }
.oc-btn.acc { background: #5B9A5E; color: #fff; }
.oc-btn.danger { background: #D4452B; color: #fff; }
.oc-btn.done { background: #E8634A; color: #fff; }
.oc-btn.reorder { background: #FFF0E8; color: #E8634A; border: 1rpx solid #E8634A; }

.empty { text-align: center; color: #9B8579; padding: 120rpx 0; font-size: 28rpx; display: flex; flex-direction: column; align-items: center; }
.empty-icon { display: block; font-size: 100rpx; margin-bottom: 20rpx; }
</style>
