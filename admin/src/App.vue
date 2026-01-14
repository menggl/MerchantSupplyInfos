<template>
  <router-view v-if="isLoginPage"></router-view>
  <el-container v-else style="height: 100vh;">
    <el-aside width="200px" style="background-color: #545c64; overflow-x: hidden;">
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical-demo"
        background-color="#545c64"
        text-color="#fff"
        active-text-color="#ffd04b"
        @select="handleSelect"
        style="border-right: none;"
      >
        <div style="height: 60px; line-height: 60px; text-align: center; color: #fff; font-weight: bold; font-size: 18px; border-bottom: 1px solid #666;">
          巡机宝宝
        </div>
        <el-menu-item index="home">
          <el-icon><Collection /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="merchant">
          <el-icon><Collection /></el-icon>
          <span>商家管理</span>
        </el-menu-item>
        <el-menu-item index="supply">
          <el-icon><Goods /></el-icon>
          <span>上架管理</span>
        </el-menu-item>
        <el-menu-item index="buy">
          <el-icon><Tools /></el-icon>
          <span>求购管理</span>
        </el-menu-item>
        <el-sub-menu index="dict">
          <template #title>
            <el-icon><Collection /></el-icon>
            <span>字典管理</span>
          </template>
          <el-menu-item index="product">
            <el-icon><Goods /></el-icon>
            <span>产品管理</span>
          </el-menu-item>
          <el-menu-item index="city">
            <el-icon><Location /></el-icon>
            <span>城市列表</span>
          </el-menu-item>
          <el-menu-item index="phoneRemark">
            <el-icon><Collection /></el-icon>
            <span>手机备注</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="others">
          <el-icon><Tools /></el-icon>
          <span>其他设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header style="text-align: right; font-size: 12px; border-bottom: 1px solid #eee; display: flex; align-items: center; justify-content: space-between; background-color: #fff;">
        <div style="font-size: 20px; font-weight: bold; margin-left: 20px;">巡机宝宝后台管理系统</div>
        <el-dropdown @command="handleDropdownCommand">
          <el-button type="text">
            <el-icon><setting /></el-icon>
            <span>设置</span>
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      
      <el-main style="padding: 20px; background-color: #f0f2f5; height: calc(100vh - 60px); overflow: hidden;">
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Setting, ArrowDown, Goods, Tools, Collection, Location } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const isLoginPage = computed(() => route.path === '/login')
const activeMenu = ref('home')
const supplyTab = ref('new')

watch(route, (newRoute) => {
  if (newRoute.path.startsWith('/home') || newRoute.path === '/') {
    activeMenu.value = 'home'
  } else if (newRoute.path.startsWith('/merchant')) {
    activeMenu.value = 'merchant'
  } else if (newRoute.path.startsWith('/supply')) {
    activeMenu.value = 'supply'
  } else if (newRoute.path.startsWith('/buy')) {
    activeMenu.value = 'buy'
  } else if (newRoute.path.startsWith('/product')) {
    activeMenu.value = 'product'
  } else if (newRoute.path.startsWith('/city')) {
    activeMenu.value = 'city'
  } else if (newRoute.path.startsWith('/phoneRemark')) {
    activeMenu.value = 'phoneRemark'
  } else if (newRoute.path.startsWith('/others')) {
    activeMenu.value = 'others'
  }
}, { immediate: true })

const handleSelect = (key) => {
  activeMenu.value = key
  if (key === 'home') {
    router.push('/home')
  } else if (key === 'merchant') {
    router.push('/merchant')
  } else if (key === 'supply') {
    router.push('/supply')
  } else if (key === 'buy') {
    router.push('/buy')
  } else if (key === 'product') {
    router.push('/product')
  } else if (key === 'city') {
    router.push('/city')
  } else if (key === 'phoneRemark') {
    router.push('/phoneRemark')
  } else if (key === 'others') {
    router.push('/others')
  }
}

const handleDropdownCommand = async (command) => {
  if (command === 'logout') {
    try {
      await axios.post('/admin/auth/logout')
    } catch (e) {
      console.error(e)
    } finally {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_token_expireTime')
      if (router.currentRoute.value.path !== '/login') {
        router.replace('/login')
      }
    }
  }
}
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', sans-serif;
}
</style>
