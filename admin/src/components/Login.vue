<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="login-title">
        巡机宝宝后台管理系统
      </div>
      <el-form :model="form" @keyup.enter="onSubmit" size="large">
        <el-form-item>
          <el-input 
            v-model="form.username" 
            placeholder="用户名" 
            :prefix-icon="User"
            autocomplete="username" 
          />
        </el-form-item>
        <el-form-item>
          <el-input 
            v-model="form.password" 
            placeholder="密码" 
            type="password" 
            show-password 
            :prefix-icon="Lock"
            autocomplete="current-password" 
          />
        </el-form-item>
        <el-form-item style="margin-bottom: 0;">
          <el-button type="primary" style="width: 100%;" :loading="loading" @click="onSubmit">登 录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const onSubmit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const res = await axios.post('/admin/auth/login', {
      username: form.username,
      password: form.password
    })
    const token = res.data?.token
    const expireTime = res.data?.expireTime
    if (!token) {
      ElMessage.error('登录失败')
      return
    }
    localStorage.setItem('admin_token', token)
    if (expireTime) {
      localStorage.setItem('admin_token_expireTime', String(expireTime))
    }
    ElMessage.success('登录成功')
    router.replace('/home')
  } catch (e) {
    const msg = e?.response?.data?.message || '登录失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-image: linear-gradient(135deg, #2d3a4b 0%, #4b6cb7 100%);
  background-size: cover;
}

.login-card {
  width: 400px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background-color: rgba(255, 255, 255, 0.95);
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}
</style>
