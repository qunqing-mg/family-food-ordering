<template>
  <view class="page">
    <view class="title">📊 数据看板</view>

    <!-- 总览卡片 -->
    <view class="cards">
      <view class="card" style="flex:2">
        <text class="card-num">{{ totalOrders }}</text>
        <text class="card-label">累计订单</text>
      </view>
    </view>

    <!-- 今日主厨 -->
    <view class="chef-card" v-if="todayChef" @tap="rotateChef">
      <text class="chef-icon">👨‍🍳</text>
      <text class="chef-text">今日主厨：<text class="chef-name">{{ todayChef }}</text></text>
      <text class="chef-hint">点此换人 ›</text>
    </view>

    <!-- 常点菜品 -->
    <view class="section" v-if="freqDishes.length">
      <view class="section-title">🔥 你常点的</view>
      <scroll-view scroll-x class="freq-scroll">
        <view class="freq-item" v-for="d in freqDishes" :key="d.dishId">
          <image :src="imgUrl(d.imageUrl)" mode="aspectFill" class="freq-img" />
          <text class="freq-name">{{ d.name }}</text>
          <text class="freq-count">×{{ d.orderCount }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 排行榜 -->
    <view class="section" v-if="topDishes.length">
      <view class="section-title">🏆 菜品排行榜</view>
      <view class="rank-item" v-for="(d, i) in topDishes" :key="i">
        <text class="rank-num" :class="'top' + (i+1)">{{ i + 1 }}</text>
        <text class="rank-name">{{ d.name }}</text>
        <view class="rank-bar-wrap">
          <view class="rank-bar" :style="{ width: barWidth(i) + '%' }" />
        </view>
        <text class="rank-count">{{ d.count }}份</text>
      </view>
    </view>

    <!-- 下单截止 -->
    <view class="section">
      <view class="section-title">⏰ 下单截止时间</view>
      <view class="deadline-row">
        <picker mode="time" :value="deadline" @change="onDeadlineChange">
          <view class="deadline-picker">
            <text>{{ deadline || '不限制' }}</text>
            <text class="arrow">›</text>
          </view>
        </picker>
        <text class="deadline-hint">超过时间将无法下单</text>
      </view>
    </view>

    <view class="footer"><text>暖食记 · 家的味道</text></view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api, imgUrl } from '../../api/request'

const totalOrders = ref(0)
const todayChef = ref('')
const topDishes = ref([])
const freqDishes = ref([])
const deadline = ref('')
const maxCount = ref(1)

function barWidth(i) { return maxCount.value > 0 ? Math.round(topDishes.value[i].count / maxCount.value * 100) : 0 }

async function load() {
  const data = await api.getDashboard()
  if (!data) return
  totalOrders.value = data.totalOrders || 0
  todayChef.value = data.todayChef || ''
  topDishes.value = data.topDishes || []
  freqDishes.value = data.freqDishes || []
  if (topDishes.value.length) maxCount.value = topDishes.value[0].count
}

async function rotateChef() {
  await api.rotateChef()
  load()
  uni.showToast({ title: '主厨已轮换', icon: 'success' })
}

function onDeadlineChange(e) {
  deadline.value = e.detail.value
  api.updateFamilySettings({ orderDeadline: deadline.value })
  uni.showToast({ title: '截止时间已更新', icon: 'success' })
}

onMounted(() => { load() })
</script>

<style scoped>
.page { padding: 24rpx; min-height: 100vh; background: #FFFAF5; }
.title { font-size: 34rpx; font-weight: 700; margin-bottom: 24rpx; color: #3D2C26; }

.cards { display: flex; gap: 16rpx; margin-bottom: 24rpx; }
.card { flex: 1; background: #fff; border-radius: 16rpx; padding: 28rpx; text-align: center; }
.card-num { font-size: 40rpx; font-weight: 700; color: #E8634A; display: block; }
.card-label { font-size: 24rpx; color: #9B8579; margin-top: 8rpx; display: block; }

.chef-card { display: flex; align-items: center; background: #FFF0E8; border-radius: 16rpx; padding: 24rpx; margin-bottom: 24rpx; }
.chef-icon { font-size: 48rpx; margin-right: 16rpx; }
.chef-text { flex: 1; font-size: 28rpx; color: #3D2C26; }
.chef-name { font-weight: 700; color: #E8634A; }
.chef-hint { font-size: 24rpx; color: #9B8579; }

.section { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 24rpx; }
.section-title { font-size: 30rpx; font-weight: 700; margin-bottom: 20rpx; color: #3D2C26; }

.freq-scroll { white-space: nowrap; display: flex; gap: 16rpx; }
.freq-item { display: inline-flex; flex-direction: column; align-items: center; width: 140rpx; }
.freq-img { width: 120rpx; height: 120rpx; border-radius: 12rpx; background: #F5EDE7; }
.freq-name { font-size: 24rpx; color: #3D2C26; margin-top: 8rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 120rpx; }
.freq-count { font-size: 22rpx; color: #E8634A; }

.rank-item { display: flex; align-items: center; padding: 14rpx 0; border-bottom: 1rpx solid #F0E8E2; }
.rank-item:last-child { border-bottom: none; }
.rank-num { width: 48rpx; font-size: 28rpx; font-weight: 700; color: #C8B8AF; text-align: center; }
.rank-num.top1 { color: #F4A340; }
.rank-num.top2 { color: #9B8579; }
.rank-num.top3 { color: #D4452B; }
.rank-name { width: 160rpx; font-size: 26rpx; color: #3D2C26; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rank-bar-wrap { flex: 1; height: 12rpx; background: #F5EDE7; border-radius: 6rpx; margin: 0 16rpx; overflow: hidden; }
.rank-bar { height: 100%; background: #E8634A; border-radius: 6rpx; min-width: 8rpx; }
.rank-count { font-size: 24rpx; color: #9B8579; width: 60rpx; text-align: right; }

.deadline-row { display: flex; flex-direction: column; gap: 10rpx; }
.deadline-picker { display: flex; justify-content: space-between; align-items: center; background: #FFFAF5; border: 1rpx solid #E8DDD6; padding: 20rpx 16rpx; border-radius: 10rpx; font-size: 28rpx; color: #3D2C26; }
.deadline-hint { font-size: 24rpx; color: #9B8579; }
.arrow { color: #C8B8AF; font-size: 36rpx; }

.footer { text-align: center; padding: 30rpx 0; color: #C8B8AF; font-size: 24rpx; }
</style>
