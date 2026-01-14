import { createRouter, createWebHashHistory } from 'vue-router'
import MerchantManager from '../components/MerchantManager.vue'
import MerchantDetail from '../components/MerchantDetail.vue'
import ProductManager from '../components/ProductManager.vue'
import CityManager from '../components/CityManager.vue'
import PhoneRemarkManager from '../components/PhoneRemarkManager.vue'
import SupplyManager from '../components/SupplyManager.vue'
import BuyRequestManager from '../components/BuyRequestManager.vue'
import Login from '../components/Login.vue'
import Home from '../components/Home.vue'

// Define a placeholder component for other routes to avoid errors until they are implemented
const PlaceholderComponent = { template: '<div>Page under construction</div>' }

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/login', component: Login },
  { path: '/home', component: Home },
  { path: '/merchant', component: MerchantManager },
  { path: '/merchant/:id', component: MerchantDetail },
  { path: '/product', component: ProductManager },
  { path: '/city', component: CityManager },
  { path: '/phoneRemark', component: PhoneRemarkManager },
  { path: '/supply', component: SupplyManager },
  { path: '/buy', component: BuyRequestManager },
  // Add placeholders for other menu items to prevent routing errors
  { path: '/others', component: PlaceholderComponent }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (to.path === '/login') {
    if (token) {
      next('/home')
    } else {
      next()
    }
    return
  }
  if (!token) {
    next('/login')
    return
  }
  next()
})

export default router
