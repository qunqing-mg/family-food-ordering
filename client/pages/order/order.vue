<template>
  <view class="page">
    <view class="title">确认订单</view>
    <view class="item" v-for="(it, i) in items" :key="i" @tap="showDetail(it)">
      <image :src="imgUrl(it.imageUrl)" mode="aspectFill" class="item-img" />
      <view class="item-info">
        <view class="item-name">{{ it.name }}</view>
        <view class="item-price">×{{ it.quantity }}</view>
      </view>
    </view>
    <view class="remark-box">
      <text class="remark-label">备注</text>
      <input v-model="remark" placeholder="如：少盐、不要香菜…" class="remark-input" maxlength="100" />
    </view>
    <view class="empty" v-if="items.length === 0"><text>🛒</text><text>购物车是空的</text></view>
    <view class="total">共 {{ items.length }} 种，{{ totalQty }} 份</view>
    <button class="submit-btn" :disabled="submitting" @click="doSubmit">
      {{ submitting ? '提交中...' : '提交订单' }}
    </button>

    <!-- 菜品详情弹窗 -->
    <view class="modal-mask" v-if="detailDish" @tap="detailDish = null">
      <view class="detail-box" @tap.stop>
        <image :src="imgUrl(detailDish.imageUrl)" mode="aspectFill" class="detail-img" />
        <text class="detail-name">{{ detailDish.name }}</text>
        <text class="detail-price">¥{{ detailDish.price }}</text>
        <text class="detail-desc">{{ detailDish.description || '暂无描述' }}</text>
        <text class="detail-close" @tap="detailDish = null">关闭</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { api, imgUrl } from '../../api/request'
import { useCartStore } from '../../stores/cart'

const cart = useCartStore()
const items = ref([])
const remark = ref('')
const submitting = ref(false)
const detailDish = ref(null)
const totalQty = computed(() => items.value.reduce((s, i) => s + i.quantity, 0))

function showDetail(it) { detailDish.value = it }

onLoad(() => {
  items.value = cart.items.map(i => ({
    dishId: i.dishId, name: i.name, imageUrl: i.imageUrl, price: i.price,
    quantity: i.quantity, description: i.description
  }))
})

async function doSubmit() {
  submitting.value = true
  try {
    await api.createOrder({
      items: items.value.map(i => ({ dishId: i.dishId, quantity: i.quantity })),
      remark: remark.value
    })
    cart.clearCart()
    uni.showToast({ title: '下单成功', icon: 'success' })
    setTimeout(() => uni.switchTab({ url: '/pages/order-list/list' }), 1000)
  } catch (e) {
    submitting.value = false
  }
}
</script>

<style scoped>
.page { padding: 24rpx; min-height: 100vh; background: #FFFAF5; }
.title { font-size: 34rpx; font-weight: 700; margin-bottom: 24rpx; color: #3D2C26; }
.item { display: flex; align-items: center; background: #fff; padding: 20rpx; border-radius: 16rpx; margin-bottom: 12rpx; }
.item-img { width: 100rpx; height: 100rpx; border-radius: 12rpx; background: #F5EDE7; }
.item-info { flex: 1; margin-left: 16rpx; }
.item-name { font-size: 28rpx; font-weight: 600; color: #3D2C26; }
.item-price { font-size: 24rpx; color: #9B8579; margin-top: 6rpx; }
.item-sub { font-size: 28rpx; color: #E8634A; font-weight: 600; }
.remark-box { display: flex; align-items: center; background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 12rpx; }
.remark-label { font-size: 28rpx; color: #9B8579; margin-right: 16rpx; white-space: nowrap; }
.remark-input { flex: 1; font-size: 28rpx; color: #3D2C26; }
.empty { text-align: center; padding: 80rpx 0; color: #9B8579; font-size: 28rpx; display: flex; flex-direction: column; gap: 12rpx; }
.total { text-align: right; font-size: 30rpx; margin: 24rpx 0; color: #3D2C26; }
.total-price { color: #E8634A; font-size: 38rpx; font-weight: 700; }
.submit-btn { background: #E8634A; color: #fff; border: none; width: 100%; padding: 28rpx; border-radius: 40rpx; font-size: 32rpx; font-weight: 600; margin-top: 30rpx; }
.submit-btn[disabled] { opacity: 0.5; }
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(61,44,38,0.5); z-index: 200; display: flex; align-items: center; justify-content: center; }
.detail-box { width: 560rpx; background: #fff; border-radius: 24rpx; padding: 40rpx; text-align: center; }
.detail-img { width: 300rpx; height: 300rpx; border-radius: 16rpx; background: #F5EDE7; margin-bottom: 20rpx; }
.detail-name { font-size: 36rpx; font-weight: 700; color: #3D2C26; display: block; }
.detail-desc { font-size: 26rpx; color: #9B8579; margin: 16rpx 0; display: block; }
.detail-close { color: #9B8579; font-size: 28rpx; margin-top: 20rpx; display: block; }
</style>
