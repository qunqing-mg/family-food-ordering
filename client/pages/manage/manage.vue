<template>
  <view class="page">
    <!-- 分类管理 -->
    <view class="section">
      <view class="section-title">📂 菜品分类</view>
      <view class="cate-row">
        <input v-model="newCateName" placeholder="新分类名称" class="cate-input" />
        <button class="cate-btn" @tap="addCate">添加</button>
      </view>
      <view class="tag-list">
        <text class="tag" v-for="c in categories" :key="c.id">
          {{ c.name }}
          <text class="tag-del" @tap="deleteCate(c.id)">×</text>
        </text>
      </view>
    </view>

    <!-- 新增菜品 -->
    <view class="section">
      <view class="section-title">🍳 新增菜品</view>
      <text class="field-label">菜品名称</text>
      <input v-model="form.name" class="field" />
      <text class="field-label">选择分类</text>
      <picker mode="selector" :range="cateNames" @change="onCateChange" class="picker">
        <view class="picker-text">{{ form.categoryName || '请选择' }}</view>
      </picker>
      <text class="field-label">菜品图片</text>
      <view class="img-row">
        <image v-if="form.imageUrl" :src="form.imageUrl" mode="aspectFill" class="img-preview" />
        <view class="img-btn" @tap="chooseImage">
          <text class="img-btn-text">{{ form.imageUrl ? '换图' : '+ 选择图片' }}</text>
        </view>
      </view>
      <text class="field-label">描述（选填）</text>
      <input v-model="form.description" class="field" />
      <button class="submit-btn" @tap="addDish">发布菜品</button>
    </view>

    <!-- 已发布菜品 -->
    <view class="section">
      <view class="section-title">📋 已发布菜品</view>
      <view class="dish-item" v-for="d in allDishes" :key="d.id">
        <text class="di-name">{{ d.name }}</text>
        <checkbox :checked="d.status === 1" @change="toggleDish(d.id)" color="#E8634A" />
        <text class="di-edit" @tap="startEdit(d)">编辑</text>
        <text class="di-del" @tap="deleteDish(d.id)">删除</text>
      </view>
      <!-- 编辑弹窗 -->
      <view class="modal-mask" v-if="editing" @tap="editing=null">
        <view class="detail-box" @tap.stop>
          <text class="modal-title">编辑菜品</text>
          <input v-model="editForm.name" class="field" />
          <picker mode="selector" :range="cateNames" @change="e=>editForm.categoryId=categories[e.detail.value].id" class="picker">
            <view class="picker-text">{{ editForm.cateName || '选择分类' }}</view>
          </picker>
          <input v-model="editForm.description" placeholder="描述" class="field" />
          <view class="modal-btns">
            <button class="btn cancel" @tap="editing=null">取消</button>
            <button class="btn save" @tap="saveEdit">保存</button>
          </view>
        </view>
      </view>
      <view class="empty" v-if="allDishes.length === 0"><text>暂无菜品，快去添加吧</text></view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../../api/request'
import request from '../../api/request'

const categories = ref([])
const allDishes = ref([])
const newCateName = ref('')
const form = ref({ name: '', categoryId: '', categoryName: '', imageUrl: '', description: '' })

const cateNames = computed(() => categories.value.map(c => c.name))

function onCateChange(e) {
  const idx = e.detail.value
  form.value.categoryId = categories.value[idx].id
  form.value.categoryName = categories.value[idx].name
}

async function loadCates() { categories.value = await api.getCategories() || [] }
async function loadDishes() { allDishes.value = await api.getDishes(null, 1) || [] }

async function addCate() {
  if (!newCateName.value) return uni.showToast({ title: '请输入名称', icon: 'none' })
  await api.addCategory(newCateName.value, categories.value.length + 1)
  newCateName.value = ''
  loadCates()
}

async function deleteCate(id) {
  const res = await uni.showModal({ title: '确认删除', content: '删除分类将同时删除下所有菜品' })
  if (!res.confirm) return
  await request({ url: '/category/' + id, method: 'DELETE' })
  loadCates()
}

async function chooseImage() {
  const res = await uni.chooseImage({ count: 1, sizeType: ['compressed'], sourceType: ['album', 'camera'] })
  uni.showLoading({ title: '上传中' })
  try {
    const url = await api.uploadFile(res.tempFilePaths[0])
    form.value.imageUrl = url
    uni.hideLoading()
  } catch (e) { uni.hideLoading() }
}

async function addDish() {
  if (!form.value.name || !form.value.categoryId) {
    return uni.showToast({ title: '请完善信息', icon: 'none' })
  }
  await api.addDish({
    name: form.value.name, categoryId: form.value.categoryId,
    price: 0, description: form.value.description,
    imageUrl: form.value.imageUrl
  })
  form.value = { name: '', categoryId: '', categoryName: '', imageUrl: '', description: '' }
  loadDishes()
}

const editing = ref(null)
const editForm = ref({})

function startEdit(d) {
  const cate = categories.value.find(c => c.id === d.categoryId)
  editForm.value = { id: d.id, name: d.name, categoryId: d.categoryId || '', cateName: cate?.name || '', description: d.description || '' }
  editing.value = d
}
async function saveEdit() {
  await api.updateDish(editForm.value.id, {
    name: editForm.value.name,
    categoryId: editForm.value.categoryId || undefined,
    description: editForm.value.description
  })
  editing.value = null
  loadDishes()
  uni.showToast({ title: '已保存', icon: 'success' })
}

async function toggleDish(id) { await api.toggleDishStatus(id); loadDishes() }

async function deleteDish(id) {
  const res = await uni.showModal({ title: '确认删除', content: '删除后不可恢复' })
  if (!res.confirm) return
  await api.deleteDish(id)
  loadDishes()
}
onMounted(() => { loadCates(); loadDishes() })
</script>

<style scoped>
.page { padding: 24rpx; min-height: 100vh; background: #FFFAF5; }
.section { background: #fff; border-radius: 16rpx; padding: 32rpx; margin-bottom: 24rpx; }
.section-title { font-size: 32rpx; font-weight: 700; margin-bottom: 28rpx; color: #3D2C26; }
.cate-row { display: flex; gap: 16rpx; margin-bottom: 16rpx; }
.cate-input { flex: 1; border: 1rpx solid #E8DDD6; padding: 24rpx 16rpx; border-radius: 10rpx; font-size: 28rpx; color: #3D2C26; background: #FFFAF5; line-height: 1.4; }
.cate-btn { background: #E8634A; color: #fff; border: none; padding: 24rpx 32rpx; border-radius: 10rpx; font-size: 28rpx; line-height: 1.4; }
.tag-list { display: flex; flex-wrap: wrap; gap: 16rpx; }
.tag { background: #FFF0E8; color: #E8634A; font-size: 26rpx; padding: 8rpx 24rpx; border-radius: 20rpx; }
.tag-del { margin-left: 12rpx; color: #9B8579; }

.field-label { display: block; font-size: 26rpx; color: #9B8579; margin-bottom: 10rpx; }
.picker { display: block; border: 1rpx solid #E8DDD6; padding: 24rpx 16rpx; border-radius: 10rpx; margin-bottom: 32rpx; background: #FFFAF5; width: 100%; box-sizing: border-box; line-height: 1.4; }
.picker-text { font-size: 28rpx; color: #9B8579; line-height: 1.4; }
.field { display: block; border: 1rpx solid #E8DDD6; padding: 24rpx 16rpx; border-radius: 10rpx; font-size: 28rpx; margin-bottom: 32rpx; width: 100%; box-sizing: border-box; color: #3D2C26; background: #FFFAF5; line-height: 1.4; }
.img-row { display: flex; align-items: center; gap: 16rpx; margin-bottom: 32rpx; }
.img-preview { width: 120rpx; height: 120rpx; border-radius: 10rpx; background: #F5EDE7; }
.img-btn { flex: 1; border: 2rpx dashed #E8DDD6; border-radius: 10rpx; padding: 36rpx; text-align: center; background: #FFFAF5; }
.img-btn-text { font-size: 26rpx; color: #9B8579; }
.submit-btn { display: block; background: #E8634A; color: #fff; border: none; width: 100%; box-sizing: border-box; padding: 20rpx; border-radius: 40rpx; font-size: 28rpx; margin-top: 8rpx; line-height: 1.4; }

.dish-item { display: flex; align-items: center; padding: 20rpx 0; border-bottom: 1rpx solid #F0E8E2; }
.di-name { flex: 1; font-size: 28rpx; color: #3D2C26; }
.di-price { color: #E8634A; font-weight: 600; margin-right: 16rpx; font-size: 28rpx; }
.di-edit { font-size: 24rpx; color: #E8634A; padding: 4rpx 16rpx; border: 1rpx solid #E8634A; border-radius: 6rpx; margin-right: 12rpx; }
.di-del { font-size: 24rpx; color: #D4452B; padding: 4rpx 16rpx; border: 1rpx solid #D4452B; border-radius: 6rpx; }
.detail-box { width: 560rpx; background: #fff; border-radius: 24rpx; padding: 40rpx; }
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(61,44,38,0.5); z-index: 200; display: flex; align-items: center; justify-content: center; }
.modal-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 24rpx; display: block; color: #3D2C26; }
.modal-btns { display: flex; gap: 20rpx; margin-top: 24rpx; }
.btn { flex: 1; padding: 20rpx; border-radius: 12rpx; font-size: 28rpx; text-align: center; border: none; }
.btn.cancel { background: #F5EDE7; color: #9B8579; }
.btn.save { background: #E8634A; color: #fff; }
.empty { text-align: center; color: #9B8579; padding: 60rpx 0; font-size: 28rpx; }
</style>
