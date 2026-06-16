<template>
  <view class="page">
    <!-- 顶部 -->
    <view class="header">
      <text class="title">🏠 {{ familyName || '家庭点餐' }}</text>
      <text class="hint" v-if="todayChef">👨‍🍳{{ todayChef }} · ⏰{{ deadline||'不限' }}</text>
      <text class="action" @tap="showModal = true">{{ familyId ? '管理' : '创建/加入' }}</text>
    </view>

    <!-- 搜索 -->
    <view class="search-bar" v-if="familyId && !loading">
      <text class="search-icon">🔍</text>
      <input v-model="searchText" placeholder="搜你想吃的..." class="search-input" @input="onSearch" />
      <text class="search-clear" v-if="searchText" @tap="searchText='';loadDishes(activeCate)">✕</text>
    </view>

    <!-- 引导页 -->
    <view class="onboard" v-if="!familyId && !loading && showOnboard">
      <swiper class="ob-swiper" :current="obStep" @change="e=>obStep=e.detail.current" circular>
        <swiper-item>
          <view class="ob-page"><text class="ob-emoji">🍳</text><text class="ob-title">家庭厨房</text><text class="ob-desc">一家人共享的菜单<br/>想吃什么，点就是了</text></view>
        </swiper-item>
        <swiper-item>
          <view class="ob-page"><text class="ob-emoji">🏠</text><text class="ob-title">创建家庭</text><text class="ob-desc">起个名字，生成邀请码<br/>分享给家人，一起加入</text></view>
        </swiper-item>
        <swiper-item>
          <view class="ob-page"><text class="ob-emoji">🥢</text><text class="ob-title">开始点餐</text><text class="ob-desc">有人下单，有人接单<br/>厨房温度，从这里开始</text></view>
        </swiper-item>
      </swiper>
      <view class="ob-dots">
        <text v-for="i in 3" :key="i" class="ob-dot" :class="{on:obStep===i-1}" @tap="obStep=i-1" />
      </view>
      <button class="ob-btn" @tap="showOnboard=false;showModal=true">开始使用</button>
    </view>

    <view class="no-family" v-if="!familyId && !loading && !showOnboard">
      <text class="nf-icon">🍳</text>
      <text class="nf-title">欢迎来到家庭厨房</text>
      <text class="nf-text">创建或加入一个家庭，开始点餐吧</text>
      <button class="nf-btn" @tap="showModal = true">开始使用</button>
    </view>

    <view class="main" v-if="familyId">
      <!-- 加载骨架 -->
      <view v-if="loading" class="skeleton-wrap">
        <view class="sk-side"><view class="sk-cate" v-for="i in 5" :key="i" /></view>
        <view class="sk-grid"><view class="sk-card" v-for="i in 6" :key="i" /></view>
      </view>

      <template v-else>
        <scroll-view class="cate-side" scroll-y>
          <view class="cate-item" :class="{ active: activeCate === 0 }" @tap="activeCate=0;loadDishes()">
            <text>🔍 全部</text>
          </view>
          <view class="cate-item" :class="{ active: activeCate === -1 }" @tap="activeCate=-1;loadFreq()">
            <text>🔥 常点</text>
          </view>
          <view v-for="c in categories" :key="c.id"
            class="cate-item" :class="{ active: activeCate === c.id }"
            @tap="activeCate = c.id; loadDishes(c.id)">
            <text>{{ cateEmoji(c.name) }} {{ c.name }}</text>
          </view>
        </scroll-view>

        <scroll-view class="dish-area" scroll-y>
          <!-- 猜你喜欢 -->
          <view class="rec-bar" v-if="recommend.length && activeCate===0 && !searchText">
            <text class="rec-label">💡 猜你喜欢</text>
            <scroll-view scroll-x class="rec-scroll">
              <view class="rec-chip" v-for="d in recommend" :key="d.dishId" @tap="recAdd(d)">
                <text>{{ d.name }}</text>
              </view>
            </scroll-view>
          </view>

          <view class="dish-grid">
            <view v-for="d in filteredDishes" :key="d.id" class="dish-card" @tap="showDetail(d)">
              <image :src="imgUrl(d.imageUrl)" mode="aspectFill" class="dish-img" />
              <view class="dish-info">
                <text class="dish-name">{{ d.name }}</text>
                <view class="dish-row">
                  <text class="dish-desc-short" v-if="d.description">{{ d.description }}</text>
                  <view class="qty-ctrl">
                    <view class="qty-btn" @tap.stop="cart.removeItem(d.id)"><text class="qty-txt">−</text></view>
                    <text class="qty-num">{{ getQty(d.id) }}</text>
                    <view class="qty-btn plus" @tap.stop="doAdd(d)"><text class="qty-txt">+</text></view>
                  </view>
                </view>
              </view>
            </view>
          </view>
          <view class="empty" v-if="filteredDishes.length === 0">
            <text class="empty-big">🥢</text>
            <text>{{ searchText ? '没找到匹配的菜品' : '还没有菜品，去管理页添加吧' }}</text>
          </view>
        </scroll-view>
      </template>
    </view>

    <!-- 购物车 -->
    <view class="cart-bar" :class="{ bounce: cartBounce }" v-if="familyId && cart.totalCount > 0" @tap="goOrder">
      <view class="cart-left">
        <text class="cart-icon">🛒</text>
        <text class="cart-badge">{{ cart.totalCount }}</text>
      </view>
      <text class="cart-price">{{ cart.totalCount }} 件</text>
      <text class="cart-btn-text">去下单</text>
    </view>

    <!-- 菜品详情 -->
    <view class="modal-mask" v-if="detailDish" @tap="detailDish = null">
      <view class="detail-box" @tap.stop>
        <image :src="imgUrl(detailDish.imageUrl)" mode="aspectFill" class="detail-img" />
        <text class="detail-name">{{ detailDish.name }}</text>
        <text class="detail-desc">{{ detailDish.description || '暂无描述' }}</text>
        <view class="detail-actions">
          <view class="qty-btn" @tap="cart.removeItem(detailDish.id);detailDish=null"><text class="qty-txt">−</text></view>
          <text class="qty-num">{{ getQty(detailDish.id) }}</text>
          <view class="qty-btn plus" @tap="doAdd(detailDish)"><text class="qty-txt">+</text></view>
        </view>
      </view>
    </view>

    <!-- 家庭弹窗 -->
    <view class="modal-mask" v-if="showModal" @tap="showModal = false">
      <view class="modal-box" @tap.stop>
        <text class="modal-title">家庭设置</text>
        <view class="modal-sec">
          <text class="label">创建家庭</text>
          <input v-model="newName" placeholder="输入家庭名称，如：陈家厨房" class="input" />
          <button class="btn" @tap="doCreate">创建</button>
        </view>
        <view class="divider"><text>或</text></view>
        <view class="modal-sec">
          <text class="label">加入家庭</text>
          <input v-model="inviteCode" placeholder="输入6位邀请码" class="input" maxlength="6" />
          <button class="btn join" @tap="doJoin">加入</button>
        </view>
        <text class="close" @tap="showModal = false">取消</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api, imgUrl } from '../../api/request'
import { useCartStore } from '../../stores/cart'

const cart = useCartStore()
const familyId = ref(null)
const familyName = ref('')
const categories = ref([])
const dishes = ref([])
const activeCate = ref(0)
const showModal = ref(false)
const newName = ref('')
const inviteCode = ref('')
const loading = ref(true)
const cartBounce = ref(false)
const detailDish = ref(null)
const todayChef = ref('')
const deadline = ref('')
const freqDishes = ref([])
const recommend = ref([])
const searchText = ref('')
const showOnboard = ref(false)
const obStep = ref(0)
const allDishes = ref([])

const filteredDishes = computed(() => {
  if (!searchText.value) return dishes.value
  const kw = searchText.value.toLowerCase()
  return allDishes.value.filter(d =>
    d.name.toLowerCase().includes(kw) ||
    (d.description && d.description.includes(kw))
  )
})

const CAT_EMOJI = { '家常菜':'🥘','主食':'🍚','汤品':'🍲','凉菜':'🥒','小吃':'🥟','饮品':'🥤','甜品':'🍮' }
function cateEmoji(name) { return CAT_EMOJI[name] || '🍽️' }
function getQty(dishId) { const item = cart.items.find(i => i.dishId === dishId); return item ? item.quantity : 0 }
function doAdd(d) { cart.addItem(d); cartBounce.value = true; setTimeout(() => cartBounce.value = false, 300) }
function showDetail(d) { detailDish.value = d }

function recAdd(d) {
  cart.addItem({ id: d.dishId, name: d.name, imageUrl: d.imageUrl, price: 0 })
  cartBounce.value = true; setTimeout(() => cartBounce.value = false, 300)
}

function onSearch() {
  if (searchText.value) {
    dishes.value = allDishes.value.filter(d =>
      d.name.toLowerCase().includes(searchText.value.toLowerCase()) ||
      (d.description && d.description.includes(searchText.value))
    )
  } else {
    loadDishes(activeCate.value)
  }
}

async function loadCategories() {
  const res = await api.getCategories(); categories.value = res || []
  if (categories.value.length > 0) { activeCate.value = 0; loadDishes() }
}
async function loadDishes(id) {
  if (id && id > 0) { dishes.value = await api.getDishes(id) || [] }
  else { dishes.value = await api.getDishes() || [] }
  allDishes.value = dishes.value
}
async function loadFreq() {
  dishes.value = freqDishes.value.map(d => ({ id: d.dishId, name: d.name, imageUrl: d.imageUrl, price: 0, description: '' }))
}
async function loadDashboard() {
  try {
    const data = await api.getDashboard()
    if (data) {
      todayChef.value = data.todayChef || ''; freqDishes.value = data.freqDishes || []
      recommend.value = data.recommend || []
    }
  } catch (e) {}
}
function goOrder() { uni.navigateTo({ url: '/pages/order/order' }) }

async function doCreate() {
  if (!newName.value) return uni.showToast({ title: '请输入家庭名称', icon: 'none' })
  const res = await api.createFamily(newName.value)
  familyId.value = res.id; familyName.value = res.name
  uni.setStorageSync('family_info', JSON.stringify({ id: res.id, name: res.name, inviteCode: res.inviteCode }))
  showModal.value = false; loadCategories(); loadDashboard()
}
async function doJoin() {
  if (!inviteCode.value) return uni.showToast({ title: '请输入邀请码', icon: 'none' })
  const res = await api.joinFamily(inviteCode.value)
  familyId.value = res.id; familyName.value = res.name
  uni.setStorageSync('family_info', JSON.stringify({ id: res.id, name: res.name, inviteCode: res.inviteCode }))
  showModal.value = false; loadCategories(); loadDashboard()
}

async function doLogin(code, nickname) {
  const data = await api.wxLogin({ code, nickname: nickname || '', avatarUrl: '' })
  if (data && data.token) { uni.setStorageSync('token', data.token); uni.setStorageSync('user', JSON.stringify(data)); await restoreFamily() }
}
async function restoreFamily() {
  try {
    const members = await api.getMembers()
    if (members && members.length > 0) {
      familyId.value = members[0].familyId
      const cached = uni.getStorageSync('family_info')
      if (cached) { try { familyName.value = JSON.parse(cached).name || '' } catch (e) {} }
      try { const info = await api.getFamilyInfo(); if (info) { familyName.value = info.name; uni.setStorageSync('family_info', JSON.stringify(info)) } } catch (e) {}
      loadCategories(); loadDashboard()
    }
  } catch (e) {}
  loading.value = false
}
async function initLogin() {
  const savedToken = uni.getStorageSync('token')
  if (savedToken) {
    try {
      const members = await api.getMembers()
      if (members && members.length > 0) { familyId.value = members[0].familyId; await restoreFamily(); return }
    } catch (e) {}
  }
  const devId = 'dev_' + (uni.getStorageSync('dev_user_id') || Date.now())
  uni.setStorageSync('dev_user_id', devId); doLogin(devId, '家人')
}
onMounted(() => {
  cart.loadCart()
  // 检查是否首次使用
  const everUsed = uni.getStorageSync('ever_used')
  if (!everUsed) { showOnboard.value = true; uni.setStorageSync('ever_used', '1') }
  setTimeout(() => { initLogin() }, 500)
  setTimeout(() => { loading.value = false }, 3000)
})
</script>

<style scoped>
.page { height: 100vh; display: flex; flex-direction: column; background: #FFFAF5; }
.header { display: flex; align-items: center; padding: 20rpx 30rpx; background: #E8634A; color: #fff; flex-shrink: 0; gap: 12rpx; }
.title { font-size: 32rpx; font-weight: 700; }
.hint { flex: 1; font-size: 22rpx; opacity: 0.85; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.action { font-size: 24rpx; opacity: 0.9; white-space: nowrap; }

.search-bar { display: flex; align-items: center; margin: 16rpx 16rpx 0; padding: 16rpx 20rpx; background: #fff; border-radius: 30rpx; flex-shrink: 0; }
.search-icon { font-size: 28rpx; margin-right: 12rpx; }
.search-input { flex: 1; font-size: 26rpx; color: #3D2C26; }
.search-clear { font-size: 24rpx; color: #9B8579; padding: 0 8rpx; }

.onboard { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 40rpx; }
.ob-swiper { width: 100%; height: 400rpx; }
.ob-page { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; }
.ob-emoji { font-size: 120rpx; }
.ob-title { font-size: 36rpx; font-weight: 700; color: #3D2C26; margin: 24rpx 0 12rpx; }
.ob-desc { font-size: 26rpx; color: #9B8579; text-align: center; line-height: 1.6; }
.ob-dots { display: flex; gap: 16rpx; margin-bottom: 30rpx; }
.ob-dot { width: 16rpx; height: 16rpx; border-radius: 50%; background: #E8DDD6; }
.ob-dot.on { background: #E8634A; width: 40rpx; border-radius: 8rpx; }
.ob-btn { background: #E8634A; color: #fff; border: none; padding: 16rpx 80rpx; border-radius: 40rpx; font-size: 28rpx; }

.no-family { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.nf-icon { font-size: 100rpx; }
.nf-title { margin: 24rpx 0 8rpx; color: #3D2C26; font-size: 34rpx; font-weight: 700; }
.nf-text { color: #9B8579; font-size: 28rpx; }
.nf-btn { background: #E8634A; color: #fff; border: none; padding: 16rpx 60rpx; border-radius: 40rpx; font-size: 28rpx; margin-top: 30rpx; }

.main { flex: 1; display: flex; overflow: hidden; }
.skeleton-wrap { flex: 1; display: flex; padding: 16rpx; gap: 16rpx; }
.sk-side { width: 180rpx; display: flex; flex-direction: column; gap: 16rpx; }
.sk-cate { height: 56rpx; background: #eee; border-radius: 8rpx; animation: pulse 1.2s infinite; }
.sk-grid { flex: 1; display: flex; flex-wrap: wrap; gap: 16rpx; }
.sk-card { width: calc(50% - 8rpx); height: 260rpx; background: #eee; border-radius: 16rpx; animation: pulse 1.2s infinite; }
@keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.4; } }

.cate-side { width: 180rpx; background: #fff; }
.cate-item { padding: 26rpx 16rpx; font-size: 24rpx; color: #9B8579; text-align: center; border-left: 4rpx solid transparent; }
.cate-item.active { color: #E8634A; background: #FFF0E8; border-left-color: #E8634A; font-weight: 600; }

.dish-area { flex: 1; padding: 16rpx; }
.rec-bar { display: flex; align-items: center; margin-bottom: 16rpx; }
.rec-label { font-size: 24rpx; color: #E8634A; font-weight: 600; margin-right: 12rpx; white-space: nowrap; }
.rec-scroll { flex: 1; white-space: nowrap; display: flex; gap: 12rpx; }
.rec-chip { display: inline-flex; background: #FFF0E8; padding: 8rpx 20rpx; border-radius: 20rpx; font-size: 24rpx; color: #E8634A; }

.dish-grid { display: flex; flex-wrap: wrap; gap: 16rpx; }
.dish-card { width: calc(50% - 8rpx); background: #fff; border-radius: 16rpx; overflow: hidden; box-shadow: 0 2rpx 16rpx rgba(232,99,74,0.06); }
.dish-img { width: 100%; height: 180rpx; background: #F5EDE7; }
.dish-info { padding: 14rpx 16rpx; }
.dish-name { font-size: 28rpx; font-weight: 600; color: #3D2C26; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: block; }
.dish-desc-short { font-size: 22rpx; color: #9B8579; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; margin-right: 8rpx; }
.dish-row { display: flex; align-items: center; margin-top: 8rpx; justify-content: flex-end; }
.qty-ctrl { display: flex; align-items: center; gap: 10rpx; }
.qty-btn { width: 40rpx; height: 40rpx; border-radius: 50%; background: #F5EDE7; display: flex; align-items: center; justify-content: center; }
.qty-btn.plus { background: #FFF0E8; }
.qty-txt { font-size: 26rpx; color: #3D2C26; }
.qty-num { font-size: 24rpx; min-width: 28rpx; text-align: center; font-weight: 600; }

.empty { text-align: center; padding: 80rpx 0; color: #9B8579; font-size: 26rpx; display: flex; flex-direction: column; align-items: center; gap: 12rpx; }
.empty-big { font-size: 80rpx; }

.cart-bar { position: fixed; bottom: 0; left: 0; right: 0; background: #3D2C26; display: flex; align-items: center; padding: 16rpx 24rpx; z-index: 100; transition: transform 0.15s; }
.cart-bar.bounce { transform: scale(1.02); }
.cart-left { position: relative; }
.cart-icon { font-size: 48rpx; }
.cart-badge { position: absolute; top: -10rpx; right: -10rpx; background: #F4A340; color: #fff; font-size: 20rpx; width: 34rpx; height: 34rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; text-align: center; }
.cart-price { flex: 1; font-size: 32rpx; font-weight: 700; color: #fff; margin-left: 20rpx; }
.cart-btn-text { background: #E8634A; color: #fff; padding: 14rpx 36rpx; border-radius: 40rpx; font-size: 28rpx; font-weight: 600; }

.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(61,44,38,0.5); z-index: 200; display: flex; align-items: center; justify-content: center; }
.modal-box { width: 600rpx; background: #fff; border-radius: 24rpx; padding: 40rpx; }
.modal-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 30rpx; display: block; color: #3D2C26; }
.modal-sec { margin-bottom: 20rpx; }
.label { font-size: 26rpx; color: #9B8579; margin-bottom: 10rpx; display: block; }
.input { border: 1rpx solid #E8DDD6; padding: 18rpx 16rpx; border-radius: 12rpx; font-size: 28rpx; margin: 10rpx 0; color: #3D2C26; background: #FFFAF5; }
.btn { background: #E8634A; color: #fff; border: none; padding: 18rpx; border-radius: 12rpx; font-size: 28rpx; margin-top: 10rpx; text-align: center; }
.btn.join { background: #F4A340; }
.divider { text-align: center; margin: 20rpx 0; color: #C8B8AF; font-size: 24rpx; }
.close { text-align: center; color: #9B8579; margin-top: 20rpx; font-size: 26rpx; display: block; }

.detail-box { width: 560rpx; background: #fff; border-radius: 24rpx; padding: 40rpx; text-align: center; }
.detail-img { width: 300rpx; height: 300rpx; border-radius: 16rpx; background: #F5EDE7; margin-bottom: 20rpx; }
.detail-name { font-size: 36rpx; font-weight: 700; color: #3D2C26; display: block; }
.detail-desc { font-size: 26rpx; color: #9B8579; margin: 16rpx 0; display: block; }
.detail-actions { display: flex; align-items: center; justify-content: center; gap: 16rpx; margin-top: 20rpx; }
</style>
