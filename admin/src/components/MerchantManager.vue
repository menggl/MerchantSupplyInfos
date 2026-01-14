<template>
  <div style="height: 100%; display: flex; flex-direction: column;">
    <el-card
      class="box-card"
      style="flex: 1; overflow: hidden;"
      :body-style="{ height: '100%', padding: '0', display: 'flex', flexDirection: 'column', minHeight: 0 }"
    >
      <div style="padding: 20px; display: flex; flex-direction: column; flex: 1; min-height: 0;">
        <div style="padding-bottom: 12px; display: flex; align-items: center; justify-content: space-between;">
          <div style="display: flex; gap: 10px; align-items: center;">
            <el-input v-model="searchForm.merchantName" placeholder="商家名称" style="width: 150px;" clearable @keyup.enter="handleSearch" />
            <el-input v-model="searchForm.phone" placeholder="手机号" style="width: 150px;" clearable @keyup.enter="handleSearch" />
            <el-select v-model="searchForm.isMember" placeholder="会员状态" style="width: 120px;" clearable>
              <el-option label="是会员" :value="1" />
              <el-option label="非会员" :value="0" />
            </el-select>
            <el-select v-model="searchForm.cityCode" placeholder="选择城市" style="width: 150px;" clearable filterable>
              <el-option
                v-for="item in cityOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-date-picker
              v-model="searchForm.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 340px;"
            />
            <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          </div>
          <el-button type="primary" :icon="Refresh" :loading="loading" @click="fetchMerchants">刷新</el-button>
        </div>

        <div style="flex: 1; min-height: 0;">
          <el-table
            :data="merchants"
            border
            height="100%"
            v-loading="loading"
          >
            <el-table-column prop="id" label="商家ID" width="80" align="center" />
            <el-table-column prop="wechatId" label="微信Id" min-width="140">
              <template #default="{ row }">
                <span class="link-text" @click="router.push(`/merchant/${row.id}`)">{{ row.wechatId }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="merchantName" label="商家名称" min-width="140">
              <template #default="{ row }">
                <span class="link-text" @click="router.push(`/merchant/${row.id}`)">{{ row.merchantName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="contactName" label="联系人名称" min-width="120" />
            <el-table-column prop="merchantPhone" label="联系人电话" min-width="140" />
            <el-table-column prop="cityName" label="所在城市" min-width="120" />
            <el-table-column prop="merchantAddress" label="所在地址" min-width="220" show-overflow-tooltip />
            <el-table-column label="商家注册时间" min-width="170">
              <template #default="{ row }">
                {{ formatTime(row.registrationDate) }}
              </template>
            </el-table-column>
            <el-table-column label="是否是会员" width="100" align="center">
              <template #default="{ row }">
                {{ row.isMember === 1 ? '是' : '否' }}
              </template>
            </el-table-column>
            <el-table-column label="会员注册时间" min-width="170">
              <template #default="{ row }">
                {{ formatTime(row.memberRegisterDate) }}
              </template>
            </el-table-column>
            <el-table-column label="会员截止时间" min-width="170">
              <template #default="{ row }">
                {{ formatTime(row.memberExpireDate) }}
              </template>
            </el-table-column>
            <el-table-column label="经纬度坐标" min-width="180">
              <template #default="{ row }">
                {{ formatCoordinates(row) }}
              </template>
            </el-table-column>
            <el-table-column label="商家状态" width="130" align="center">
              <template #default="{ row }">
                <el-button 
                  :type="row.isValid === 1 ? 'success' : 'danger'" 
                  size="small" 
                  @click="toggleStatus(row)"
                >
                  {{ row.isValid === 1 ? '有效' : '无效' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchMerchants"
          @current-change="fetchMerchants"
          style="margin-top: 10px; justify-content: flex-end;"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const merchants = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchForm = ref({
  merchantName: '',
  phone: '',
  isMember: '',
  cityCode: '',
  dateRange: []
})
const cityOptions = ref([])

const fetchCities = async () => {
  try {
    const resp = await axios.get('/admin/cities')
    if (resp.data && Array.isArray(resp.data)) {
      cityOptions.value = resp.data
        .filter(city => city.valid === 1)
        .map(city => ({
          label: city.cityName,
          value: city.cityCode
        }))
    }
  } catch (e) {
    console.error(e)
  }
}

const formatTime = (val) => {
  if (!val) return ''
  if (Array.isArray(val)) {
    const [year, month, day, hour, minute] = val
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
  }
  // Handle ISO string like "2026-01-06T08:15:00"
  if (typeof val === 'string' && val.includes('T')) {
    // Replace T with space and take first 16 chars (yyyy-MM-dd HH:mm)
    return val.replace('T', ' ').substring(0, 16)
  }
  return val
}

const formatCoordinates = (row) => {
  const lat = row?.latitude
  const lng = row?.longitude
  if (lat == null && lng == null) return ''
  if (lat == null) return String(lng)
  if (lng == null) return String(lat)
  return `${lat}, ${lng}`
}

const handleSearch = () => {
  currentPage.value = 1
  fetchMerchants()
}

const fetchMerchants = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      merchantName: searchForm.value.merchantName || undefined,
      phone: searchForm.value.phone || undefined,
      isMember: searchForm.value.isMember !== '' ? searchForm.value.isMember : undefined,
      cityCode: (searchForm.value.cityCode && searchForm.value.cityCode !== '000000') ? searchForm.value.cityCode : undefined
    }
    if (searchForm.value.dateRange && searchForm.value.dateRange.length === 2) {
      params.startDate = searchForm.value.dateRange[0]
      params.endDate = searchForm.value.dateRange[1]
    }

    const resp = await axios.get('/admin/merchants', { params })
    if (resp.data && typeof resp.data === 'object' && 'list' in resp.data) {
      merchants.value = resp.data.list
      total.value = resp.data.total
    } else {
      merchants.value = Array.isArray(resp.data) ? resp.data : []
      total.value = merchants.value.length
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取商家列表失败')
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (row) => {
  const id = row?.id
  if (!id) return
  
  const currentStatus = row.isValid
  const newStatus = currentStatus === 1 ? 0 : 1
  const actionText = currentStatus === 1 ? '置为无效' : '置为有效'
  const type = currentStatus === 1 ? 'warning' : 'success'

  try {
    await ElMessageBox.confirm(`确定要将该商家${actionText}吗？`, '确认修改', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: type
    })
  } catch (e) {
    return
  }

  try {
    const resp = await axios.put(`/admin/merchants/${id}/status`, { isValid: newStatus })
    if (resp.data && resp.data.success) {
      row.isValid = newStatus
      ElMessage.success('修改成功')
    } else {
      ElMessage.error('修改失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('修改失败')
  }
}

onMounted(() => {
  fetchCities()
  fetchMerchants()
})
</script>

<style scoped>
.link-text {
  color: var(--el-color-primary);
  text-decoration: underline;
  cursor: pointer;
  font-weight: normal;
}
.link-text:hover {
  opacity: 0.8;
}
</style>
