import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])

  const totalCount = computed(() => items.value.reduce((s, i) => s + i.quantity, 0))
  const totalPrice = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0))

  function addItem(dish) {
    const exist = items.value.find(i => i.dishId === dish.id)
    if (exist) { exist.quantity++ }
    else { items.value.push({ dishId: dish.id, name: dish.name, imageUrl: dish.imageUrl, price: dish.price, description: dish.description, quantity: 1 }) }
    save()
  }

  function removeItem(dishId) {
    const exist = items.value.find(i => i.dishId === dishId)
    if (exist && exist.quantity > 1) { exist.quantity-- }
    else { items.value = items.value.filter(i => i.dishId !== dishId) }
    save()
  }

  function clearCart() { items.value = []; save() }

  function save() { uni.setStorageSync('cart_items', JSON.stringify(items.value)) }

  function loadCart() {
    const saved = uni.getStorageSync('cart_items')
    if (saved) {
      try { items.value = JSON.parse(saved) } catch (e) { items.value = [] }
    }
  }

  return { items, totalCount, totalPrice, addItem, removeItem, clearCart, loadCart }
})
