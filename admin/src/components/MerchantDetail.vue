<template>
  <div style="height: 100%; display: flex; flex-direction: column;">
    <el-card class="box-card" style="flex: 1; overflow-y: auto;">
      <template #header>
        <div class="card-header">
          <div style="display: flex; align-items: center;">
            <el-button @click="router.back()" :icon="ArrowLeft" circle style="margin-right: 12px" />
            <span style="font-size: 18px; font-weight: bold;">商家详情</span>
          </div>
        </div>
      </template>
      
      <div v-loading="loading" style="padding: 20px;">
        <div v-if="merchant">
          <!-- 基本信息 -->
          <el-descriptions title="基本信息" :column="2" border>
            <el-descriptions-item label="商家ID">{{ merchant.id }}</el-descriptions-item>
            <el-descriptions-item label="微信ID">{{ merchant.wechatId }}</el-descriptions-item>
            <el-descriptions-item label="微信名称">{{ merchant.wechatName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="商家名称">{{ merchant.merchantName }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ merchant.contactName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ merchant.merchantPhone }}</el-descriptions-item>
            <el-descriptions-item label="所在城市">{{ merchant.cityName }}</el-descriptions-item>
            <el-descriptions-item label="详细地址" :span="2">{{ merchant.merchantAddress }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ formatTime(merchant.registrationDate) }}</el-descriptions-item>
            <el-descriptions-item label="商家状态">
              <el-tag :type="merchant.isValid === 1 ? 'success' : 'danger'">
                {{ merchant.isValid === 1 ? '有效' : '无效' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="会员状态">
              <el-tag :type="merchant.isMember === 1 ? 'warning' : 'info'">
                {{ merchant.isMember === 1 ? '是会员' : '非会员' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="会员过期时间">
              {{ formatTime(merchant.memberExpireDate) || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="当前积分">{{ merchant.integral }}</el-descriptions-item>
          </el-descriptions>

          <el-divider />

          <div style="margin-top: 20px;">
            <div style="font-weight: bold; margin-bottom: 12px;">会员充值记录</div>
            <el-table :data="merchant.rechargeRecords" border style="width: 100%">
              <el-table-column label="ID" min-width="80">
                <template #default="{ row }">
                  {{ row.id }}
                </template>
              </el-table-column>
              <el-table-column label="充值金额" min-width="120">
                <template #default="{ row }">
                  ¥{{ row.rechargeAmount }}
                </template>
              </el-table-column>
              <el-table-column label="原价" min-width="120">
                <template #default="{ row }">
                  ¥{{ row.originalPrice }}
                </template>
              </el-table-column>
              <el-table-column label="优惠金额" min-width="120">
                <template #default="{ row }">
                  ¥{{ row.discountAmount }}
                </template>
              </el-table-column>
              <el-table-column label="充值类型" min-width="120">
                <template #default="{ row }">
                  <el-tag v-if="row.rechargeType === 1" type="info">月会员</el-tag>
                  <el-tag v-else-if="row.rechargeType === 2" type="primary">年会员</el-tag>
                  <el-tag v-else-if="row.rechargeType === 3" type="warning">终身会员</el-tag>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="充值时间" min-width="180">
                <template #default="{ row }">
                  {{ formatTime(row.rechargeTime) }}
                </template>
              </el-table-column>
              <el-table-column label="状态" min-width="100">
                <template #default="{ row }">
                  <el-tag :type="row.isValid === 1 ? 'success' : 'danger'" size="small">
                    {{ row.isValid === 1 ? '有效' : '无效' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div style="margin-top: 20px;">
            <div style="font-weight: bold; margin-bottom: 12px;">积分变更记录</div>
            <el-table :data="merchant.integralRecords" border style="width: 100%">
              <el-table-column label="ID" min-width="80">
                <template #default="{ row }">
                  {{ row.id }}
                </template>
              </el-table-column>
              <el-table-column label="变更前积分" min-width="100" prop="integralBeforeSpend" />
              <el-table-column label="变更后积分" min-width="100" prop="integralAfterSpend" />
              <el-table-column label="变更数量" min-width="100">
                <template #default="{ row }">
                  <span :style="{ color: row.changeAmount > 0 ? 'green' : 'red' }">
                    {{ row.changeAmount > 0 ? '+' : '' }}{{ row.changeAmount }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="变更原因" min-width="150" prop="changeReason" />
              <el-table-column label="变更时间" min-width="180">
                <template #default="{ row }">
                  {{ formatTime(row.changeTime) }}
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-divider />

          <!-- 营业执照 -->
          <div style="margin-bottom: 20px;">
            <div style="font-weight: bold; margin-bottom: 12px;">营业执照</div>
            <div v-if="merchant.businessLicenseUrl">
              <el-image 
                style="width: 200px; height: 150px; border-radius: 4px;"
                :src="merchant.businessLicenseUrl" 
                :preview-src-list="[merchant.businessLicenseUrl]"
                fit="cover"
              >
                <template #error>
                  <div style="display: flex; justify-content: center; align-items: center; width: 100%; height: 100%; background: #f5f7fa; color: #909399;">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
            <div v-else style="color: #909399; font-size: 14px;">暂无营业执照</div>
          </div>

          <el-divider />

          <!-- 地图位置 -->
          <div style="margin-bottom: 20px;">
            <div style="font-weight: bold; margin-bottom: 12px;">商家位置</div>
            <div v-if="merchant.latitude && merchant.longitude" style="height: 400px; width: 100%; position: relative;">
               <div :id="mapId" style="width: 100%; height: 100%;"></div>
               <div style="position: absolute; top: 10px; right: 10px; background: rgba(255,255,255,0.9); padding: 8px; border-radius: 4px; font-size: 12px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                 经度: {{ merchant.longitude }}, 纬度: {{ merchant.latitude }}
               </div>
            </div>
            <div v-else style="color: #909399; font-size: 14px;">暂无位置信息</div>
          </div>

        </div>
        <div v-else-if="!loading" style="text-align: center; padding: 40px; color: #909399;">
          未找到商家信息
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ArrowLeft, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const route = useRoute()
const router = useRouter()
const merchant = ref(null)
const loading = ref(false)
const mapId = `map-${Date.now()}`
let mapInstance = null

const formatTime = (val) => {
  if (!val) return ''
  if (Array.isArray(val)) {
    const [year, month, day, hour, minute] = val
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
  }
  if (typeof val === 'string' && val.includes('T')) {
    return val.replace('T', ' ').substring(0, 16)
  }
  return val
}

const initMap = () => {
  if (!merchant.value || !merchant.value.latitude || !merchant.value.longitude) return
  
  // Use Leaflet (OpenStreetMap)
  mapInstance = L.map(mapId).setView([merchant.value.latitude, merchant.value.longitude], 15)

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  }).addTo(mapInstance)

  L.marker([merchant.value.latitude, merchant.value.longitude])
    .addTo(mapInstance)
    .bindPopup(merchant.value.merchantName)
    .openPopup()
}

const fetchDetail = async () => {
  const id = route.params.id
  if (!id) return
  
  loading.value = true
  try {
    const resp = await axios.get(`/admin/merchants/${id}`)
    if (resp.data) {
      merchant.value = resp.data
      nextTick(() => {
        initMap()
      })
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取商家详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>