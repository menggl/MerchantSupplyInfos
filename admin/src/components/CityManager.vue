<template>
  <div style="height: 100%; display: flex; flex-direction: column;">
    <!-- Draggable List -->
    <el-card
      class="box-card"
      style="flex: 1; overflow: hidden;"
      :body-style="{ height: '100%', padding: '0', display: 'flex', flexDirection: 'column', minHeight: 0 }"
    >
      <div style="padding: 20px; display: flex; flex-direction: column; flex: 1; min-height: 0;">
        <div style="padding-bottom: 12px; display: flex; align-items: center; justify-content: flex-end;">
          <el-button type="primary" :icon="Plus" @click="openAddDialog">添加城市</el-button>
        </div>
        <div class="list-header" style="display: flex; padding: 10px; background: #f5f7fa; font-weight: bold; border-bottom: 1px solid #ebeef5;">
          <div style="width: 80px; text-align: center;">ID</div>
          <div style="flex: 1; padding-left: 20px;">城市代码</div>
          <div style="flex: 1;">城市名称</div>
          <div style="width: 80px; text-align: center;">排序</div>
          <div style="width: 100px; text-align: center;">状态</div>
          <div style="width: 180px;">创建时间</div>
          <div style="width: 150px; text-align: center;">操作</div>
        </div>
        
        <div style="flex: 1; overflow-y: auto; min-height: 0;">
          <draggable 
            v-model="cities" 
            item-key="id" 
            @end="onDragEnd"
            animation="200"
            ghost-class="ghost"
          >
            <template #item="{ element }">
              <div class="list-item" style="display: flex; align-items: center; padding: 12px 10px; border-bottom: 1px solid #ebeef5; cursor: move;">
                <div style="width: 80px; text-align: center;">{{ element.id }}</div>
                <div style="flex: 1; padding-left: 20px;">
                  <el-input
                    v-if="isInlineEditing(element, 'cityCode')"
                    ref="inlineInputRef"
                    v-model="inlineEdit.value"
                    size="small"
                    @click.stop
                    @keyup.enter="commitInlineEdit(element)"
                    @blur="commitInlineEdit(element)"
                  />
                  <span v-else style="cursor: text;" @dblclick.stop="startInlineEdit(element, 'cityCode')">{{ element.cityCode }}</span>
                </div>
                <div style="flex: 1;">
                  <el-input
                    v-if="isInlineEditing(element, 'cityName')"
                    ref="inlineInputRef"
                    v-model="inlineEdit.value"
                    size="small"
                    @click.stop
                    @keyup.enter="commitInlineEdit(element)"
                    @blur="commitInlineEdit(element)"
                  />
                  <span v-else style="cursor: text;" @dblclick.stop="startInlineEdit(element, 'cityName')">{{ element.cityName }}</span>
                </div>
                <div style="width: 80px; text-align: center;">{{ element.sort }}</div>
                <div style="width: 100px; text-align: center;">
                  <el-button 
                    :type="element.valid === 1 ? 'success' : 'info'" 
                    size="small" 
                    @click.stop="toggleStatus(element)"
                  >
                    {{ element.valid === 1 ? '有效' : '无效' }}
                  </el-button>
                </div>
                <div style="width: 180px;">{{ formatTime(element.createTime) }}</div>
                <div style="width: 150px; text-align: center;">
                  <el-button type="danger" link :icon="Delete" @click.stop="handleDelete(element)">删除</el-button>
                </div>
              </div>
            </template>
          </draggable>
          <div v-if="cities.length === 0" style="text-align: center; padding: 20px; color: #909399;">暂无数据</div>
        </div>
      </div>
    </el-card>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
        <el-form-item label="城市代码" prop="cityCode">
          <el-input v-model="form.cityCode" placeholder="请输入城市代码" />
        </el-form-item>
        <el-form-item label="城市名称" prop="cityName">
          <el-input v-model="form.cityName" placeholder="请输入城市名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, nextTick } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import draggable from 'vuedraggable'

const cities = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加城市')
const formRef = ref(null)
const inlineInputRef = ref(null)
const inlineEdit = reactive({
  id: null,
  field: '',
  value: ''
})
const inlineCommitInProgress = ref(false)

const form = reactive({
  id: null,
  cityCode: '',
  cityName: ''
})

const rules = {
  cityCode: [{ required: true, message: '请输入城市代码', trigger: 'blur' }],
  cityName: [{ required: true, message: '请输入城市名称', trigger: 'blur' }]
}

const formatTime = (timeArr) => {
  if (!timeArr) return ''
  if (Array.isArray(timeArr)) {
    // Handle [year, month, day, hour, minute, second]
    const [year, month, day, hour, minute, second] = timeArr
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}`
  }
  return timeArr
}

const fetchCities = async () => {
  try {
    const response = await axios.get('/admin/cities')
    cities.value = response.data
  } catch (error) {
    console.error('Failed to fetch cities:', error)
    ElMessage.error('获取城市列表失败')
  }
}

const isInlineEditing = (city, field) => {
  return inlineEdit.id === city.id && inlineEdit.field === field
}

const stopInlineEdit = () => {
  inlineEdit.id = null
  inlineEdit.field = ''
  inlineEdit.value = ''
}

const startInlineEdit = (city, field) => {
  inlineCommitInProgress.value = false
  inlineEdit.id = city.id
  inlineEdit.field = field
  inlineEdit.value = city[field] ?? ''
  nextTick(() => {
    inlineInputRef.value?.focus?.()
  })
}

const commitInlineEdit = async (city) => {
  if (inlineCommitInProgress.value) return
  if (inlineEdit.id !== city.id) return
  if (inlineEdit.field !== 'cityCode' && inlineEdit.field !== 'cityName') return

  const field = inlineEdit.field
  const newValue = String(inlineEdit.value ?? '').trim()
  const oldValue = String(city[field] ?? '').trim()

  if (newValue === oldValue) {
    stopInlineEdit()
    return
  }

  if (!newValue) {
    ElMessage.warning('内容不能为空')
    stopInlineEdit()
    return
  }

  inlineCommitInProgress.value = true
  const label = field === 'cityCode' ? '城市代码' : '城市名称'

  try {
    await ElMessageBox.confirm(`确定要将${label}从 "${oldValue}" 修改为 "${newValue}" 吗？`, '确认修改', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch (e) {
    inlineCommitInProgress.value = false
    stopInlineEdit()
    return
  }

  try {
    const payload = {
      cityCode: field === 'cityCode' ? newValue : city.cityCode,
      cityName: field === 'cityName' ? newValue : city.cityName,
      valid: city.valid
    }
    const response = await axios.put(`/admin/cities/${city.id}`, payload)
    if (response.data && response.data.success) {
      city[field] = newValue
      ElMessage.success('更新成功')
    } else {
      ElMessage.error('更新失败')
      await fetchCities()
    }
  } catch (error) {
    console.error('Update failed:', error)
    ElMessage.error('更新失败')
    await fetchCities()
  } finally {
    inlineCommitInProgress.value = false
    stopInlineEdit()
  }
}

const toggleStatus = async (city) => {
  const newStatus = city.valid === 1 ? 0 : 1
  try {
    const response = await axios.put(`/admin/cities/${city.id}`, {
      ...city,
      valid: newStatus
    })
    
    if (response.data && response.data.success) {
      ElMessage.success('状态已更新')
      
      // Update local status
      city.valid = newStatus
      
      // Re-sort: Valid cities first, then Invalid cities
      // Preserve relative order within groups by using current list order
      const validCities = cities.value.filter(c => c.valid === 1)
      const invalidCities = cities.value.filter(c => c.valid !== 1)
      
      const newOrder = [...validCities, ...invalidCities].map((c, index) => ({
        ...c,
        sort: index + 1
      }))
      
      cities.value = newOrder
      
      // Sync new sort order with backend
      await axios.put('/admin/cities/sort', newOrder)
    } else {
      ElMessage.error('状态更新失败')
    }
  } catch (error) {
    console.error('Failed to update status:', error)
    ElMessage.error('状态更新异常')
  }
}

const onDragEnd = async () => {
  // Update sort order based on current list index
  const updatedCities = cities.value.map((city, index) => ({
    ...city,
    sort: index + 1
  }))
  
  // Update local state to reflect new sort values immediately
  cities.value = updatedCities
  
  try {
    const response = await axios.put('/admin/cities/sort', updatedCities)
    if (response.data && response.data.success) {
      ElMessage.success('排序已更新')
    } else {
      ElMessage.warning('排序更新可能未生效')
    }
  } catch (error) {
    console.error('Failed to update sort order:', error)
    ElMessage.error('排序更新失败')
    fetchCities() // Revert to original order
  }
}

const openAddDialog = () => {
  dialogTitle.value = '添加城市'
  form.cityCode = ''
  form.cityName = ''
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除城市 "${row.cityName}" 吗？删除后不可恢复。`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const response = await axios.delete(`/admin/cities/${row.id}`)
        if (response.data && response.data.success) {
          ElMessage.success('删除成功')
          cities.value = cities.value.filter(c => c.id !== row.id)
        } else {
          ElMessage.error('删除失败')
        }
      } catch (error) {
        console.error('Delete failed:', error)
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await axios.post('/admin/cities', {
          cityCode: form.cityCode,
          cityName: form.cityName
        })
        ElMessage.success('添加成功')
        dialogVisible.value = false
        fetchCities()
      } catch (error) {
        console.error('Operation failed:', error)
        ElMessage.error('添加失败')
      }
    }
  })
}

onMounted(() => {
  fetchCities()
})
</script>

<style scoped>
.box-card {
  border-radius: 4px;
}
:deep(.el-card__body) {
  height: 100%;
  padding: 0;
  min-height: 0;
}
.list-item:hover {
  background-color: #f5f7fa;
}
.ghost {
  opacity: 0.5;
  background: #c8ebfb;
}
</style>
