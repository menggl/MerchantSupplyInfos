<template>
  <div style="height: 100%; display: flex; flex-direction: column;">
    <el-card
      class="box-card"
      style="flex: 1; overflow: hidden;"
      :body-style="{ height: '100%', padding: '0', display: 'flex', flexDirection: 'column', minHeight: 0 }"
    >
      <div style="padding: 20px; display: flex; flex-direction: column; flex: 1; min-height: 0;">
        <!-- Search Bar -->
        <div style="padding-bottom: 12px; display: flex; align-items: center; justify-content: space-between;">
          <div style="display: flex; gap: 10px; align-items: center;">
            <el-input v-model="searchForm.id" placeholder="ID" style="width: 100px;" clearable @keyup.enter="handleSearch" />
            <el-input v-model="searchForm.merchantName" placeholder="商家名称" style="width: 150px;" clearable @keyup.enter="handleSearch" />
             <el-select v-model="searchForm.cityCode" placeholder="选择城市" style="width: 150px;" clearable filterable>
              <el-option
                v-for="item in cityOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
          </div>
        </div>
        
        <!-- Table -->
        <div style="flex: 1; overflow: hidden;">
          <el-table
            v-loading="loading"
            :data="requests"
            style="width: 100%; height: 100%;"
            border
            stripe
          >
            <el-table-column prop="id" label="ID" width="80" align="center" />
            
            <el-table-column label="求购产品" min-width="200">
               <template #default="{ row }">
                 <div style="cursor: pointer; color: #409eff;" @click="showDetail(row)">
                   <div style="font-weight: bold;">{{ row.brandName }} {{ row.seriesName }}</div>
                   <div>{{ row.modelName }} {{ row.specName }}</div>
                 </div>
               </template>
            </el-table-column>
            
            <el-table-column label="商家信息" min-width="150">
               <template #default="{ row }">
                 <div>{{ row.merchantName }}</div>
               </template>
            </el-table-column>
            
            <el-table-column label="城市" width="100" align="center">
              <template #default="{ row }">
                {{ row.cityName || row.cityCode || '-' }}
              </template>
            </el-table-column>
            
            <el-table-column label="类型" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.productType === 0 ? 'success' : 'warning'">
                  {{ row.productType === 0 ? '新机' : '二手机' }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column label="数量" width="80" align="center" prop="buyCount" />
            
            <el-table-column label="价格范围" width="150" align="center">
               <template #default="{ row }">
                 <div style="color: #f56c6c; font-weight: bold;">¥{{ row.minPrice }} - ¥{{ row.maxPrice }}</div>
               </template>
            </el-table-column>

             <el-table-column label="花费积分" width="100" align="center" prop="costIntegral" />
            
            <el-table-column label="截止时间" width="160" align="center">
              <template #default="{ row }">
                {{ formatTime(row.deadline) }}
              </template>
            </el-table-column>
            
             <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag 
                  :type="row.isValid === 1 ? 'success' : 'danger'"
                  :style="{ cursor: row.isValid === 1 ? 'pointer' : 'pointer' }"
                  @click="handleStatusChange(row)"
                >
                  {{ row.isValid === 1 ? '有效' : '无效' }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column label="发布时间" width="160" align="center">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>

          </el-table>
        </div>
        
        <!-- Pagination -->
        <div style="padding-top: 12px; display: flex; justify-content: flex-end;">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSearch"
            @current-change="fetchRequests"
          />
        </div>
      </div>
    </el-card>

    <!-- Detail Drawer -->
    <el-drawer
      v-model="drawerVisible"
      title="求购详情"
      direction="rtl"
      size="50%"
    >
      <div v-if="currentRequest" style="padding: 20px;">
        <el-descriptions title="基本信息" :column="1" border>
          <el-descriptions-item label="品牌">{{ currentRequest.brandName }}</el-descriptions-item>
          <el-descriptions-item label="系列">{{ currentRequest.seriesName }}</el-descriptions-item>
          <el-descriptions-item label="型号">{{ currentRequest.modelName }}</el-descriptions-item>
          <el-descriptions-item label="规格">{{ currentRequest.specName }}</el-descriptions-item>
          <el-descriptions-item label="产品类型">
            <el-tag :type="currentRequest.productType === 0 ? 'success' : 'warning'">
              {{ currentRequest.productType === 0 ? '新机' : '二手机' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="求购数量">{{ currentRequest.buyCount }}</el-descriptions-item>
          <el-descriptions-item label="价格范围">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ currentRequest.minPrice }} - ¥{{ currentRequest.maxPrice }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="花费积分">{{ currentRequest.costIntegral }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ formatTime(currentRequest.deadline) }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatTime(currentRequest.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
             <el-tag :type="currentRequest.isValid === 1 ? 'success' : 'danger'">
               {{ currentRequest.isValid === 1 ? '有效' : '无效' }}
             </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div style="margin-top: 20px;"></div>
        
        <el-descriptions title="商家信息" :column="1" border>
          <el-descriptions-item label="商家名称">{{ currentRequest.merchantName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentRequest.merchantPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所在城市">{{ currentRequest.cityName || currentRequest.cityCode || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const requests = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const cityOptions = ref([])
const drawerVisible = ref(false)
const currentRequest = ref(null)

const searchForm = ref({
  id: '',
  merchantName: '',
  cityCode: ''
})

const formatTime = (time) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const showDetail = (row) => {
  currentRequest.value = row
  drawerVisible.value = true
}

const fetchRequests = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      id: searchForm.value.id || undefined,
      merchantName: searchForm.value.merchantName || undefined,
      cityCode: searchForm.value.cityCode || undefined
    }
    
    const response = await axios.get('/admin/buy-requests', { params })
    requests.value = response.data.list
    total.value = response.data.total
  } catch (error) {
    console.error(error)
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchRequests()
}

const resetSearch = () => {
  searchForm.value = {
    id: '',
    merchantName: '',
    cityCode: ''
  }
  handleSearch()
}

const loadCities = async () => {
  try {
    const response = await axios.get('/admin/cities')
    cityOptions.value = response.data.map(city => ({
      label: city.cityName,
      value: city.cityCode
    }))
  } catch (error) {
    console.error(error)
  }
}

const handleStatusChange = (row) => {
  const newStatus = row.isValid === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '上架' : '下架'
  
  ElMessageBox.confirm(
    `确认将该求购信息${actionText}吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: newStatus === 1 ? 'success' : 'warning',
    }
  ).then(async () => {
    try {
      await axios.put(`/admin/buy-requests/${row.id}/status`, { status: newStatus })
      ElMessage.success('操作成功')
      fetchRequests()
    } catch (e) {
      console.error(e)
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchRequests()
  loadCities()
})
</script>
