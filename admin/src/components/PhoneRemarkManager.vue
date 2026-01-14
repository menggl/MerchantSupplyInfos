<template>
  <div style="height: 100%; display: flex; flex-direction: column;">
    <el-card
      class="box-card"
      style="flex: 1; overflow: hidden;"
      :body-style="{ height: '100%', padding: '0', display: 'flex', flexDirection: 'column', minHeight: 0 }"
    >
      <div style="padding: 16px; display: flex; flex-direction: column; flex: 1; min-height: 0;">
        <el-tabs v-model="activeType" @tab-change="onTabChange">
          <el-tab-pane name="0" label="新机备注" />
          <el-tab-pane name="1" label="新机其它备注" />
          <el-tab-pane name="2" label="二手机版本" />
          <el-tab-pane name="3" label="二手机成色" />
          <el-tab-pane name="4" label="二手机拆修和功能" />
        </el-tabs>

        <div style="display: flex; justify-content: flex-end; padding: 0 0 12px 0;">
          <el-button type="primary" size="small" @click="createItem">新增</el-button>
        </div>

        <div style="flex: 1; min-height: 0; overflow: hidden; display: flex; flex-direction: column;">
          <div class="list-header">
            <div style="width: 90px; text-align: center;">ID</div>
            <div style="flex: 1; padding-left: 12px;">选项</div>
            <div style="width: 90px; text-align: center;">排序</div>
            <div style="width: 110px; text-align: center;">状态</div>
            <div style="width: 180px;">创建时间</div>
            <div style="width: 90px; text-align: center;">操作</div>
          </div>

          <div style="flex: 1; overflow-y: auto; min-height: 0;" v-loading="loading">
            <draggable
              v-model="currentItems"
              item-key="id"
              @start="stopInlineEdit"
              @end="onDragEnd"
              animation="200"
              ghost-class="ghost"
            >
              <template #item="{ element }">
                <div class="list-item">
                  <div style="width: 90px; text-align: center;">{{ element.id }}</div>
                  <div style="flex: 1; padding-left: 12px;">
                    <el-input
                      v-if="isInlineEditing(element)"
                      ref="inlineInputRef"
                      v-model="inlineEdit.value"
                      size="small"
                      @click.stop
                      @keyup.enter="commitInlineEdit(element)"
                      @blur="commitInlineEdit(element)"
                    />
                    <span v-else style="cursor: text;" @dblclick.stop="startInlineEdit(element)">
                      {{ element.remarkName }}
                    </span>
                  </div>
                  <div style="width: 90px; text-align: center;">{{ element.sort }}</div>
                  <div style="width: 110px; text-align: center;">
                    <el-button
                      :type="element.valid === 1 ? 'success' : 'info'"
                      size="small"
                      @click.stop="toggleValid(element)"
                    >
                      {{ element.valid === 1 ? '有效' : '无效' }}
                    </el-button>
                  </div>
                  <div style="width: 180px;">{{ formatTime(element.createTime) }}</div>
                  <div style="width: 90px; text-align: center;">
                    <el-button type="danger" size="small" @click.stop="deleteItem(element)">删除</el-button>
                  </div>
                </div>
              </template>
            </draggable>
            <div v-if="currentItems.length === 0 && !loading" style="text-align: center; padding: 20px; color: #909399;">
              暂无数据
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import draggable from 'vuedraggable'

const activeType = ref('0')
const loading = ref(false)
const inlineInputRef = ref(null)
const inlineEdit = reactive({
  id: null,
  value: ''
})
const inlineCommitInProgress = ref(false)
const listByType = reactive({
  0: [],
  1: [],
  2: [],
  3: [],
  4: []
})
const loadedType = reactive({
  0: false,
  1: false,
  2: false,
  3: false,
  4: false
})

const isInlineEditing = (row) => {
  return inlineEdit.id === row.id
}

const stopInlineEdit = () => {
  inlineEdit.id = null
  inlineEdit.value = ''
}

const startInlineEdit = (row) => {
  inlineCommitInProgress.value = false
  inlineEdit.id = row.id
  inlineEdit.value = row.remarkName ?? ''
  nextTick(() => {
    if (Array.isArray(inlineInputRef.value) && inlineInputRef.value.length > 0) {
      inlineInputRef.value[0]?.focus?.()
    } else {
      inlineInputRef.value?.focus?.()
    }
  })
}

const commitInlineEdit = async (row) => {
  if (inlineCommitInProgress.value) return
  if (inlineEdit.id !== row.id) return

  const newValue = String(inlineEdit.value ?? '').trim()
  const oldValue = String(row.remarkName ?? '').trim()

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
  try {
    await ElMessageBox.confirm('是否保存修改？', '提示', {
      confirmButtonText: '保存',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await axios.put(`/admin/phone-remarks/${row.id}`, { remarkName: newValue })
    if (response.data && response.data.success) {
      const type = typeof row.type === 'number' ? row.type : Number(activeType.value)
      const list = listByType[type] || []
      const index = list.findIndex(item => item.id === row.id)
      if (index >= 0) {
        list[index] = { ...list[index], remarkName: newValue }
      }
      ElMessage.success('保存成功')
    } else {
      ElMessage.error('保存失败')
    }
  } catch (error) {
  } finally {
    stopInlineEdit()
    inlineCommitInProgress.value = false
  }
}

const formatTime = (timeArr) => {
  if (!timeArr) return ''
  if (Array.isArray(timeArr)) {
    const [year, month, day, hour, minute, second] = timeArr
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}`
  }
  return timeArr
}

const fetchByType = async (type) => {
  if (loadedType[type]) return
  loading.value = true
  try {
    const response = await axios.get('/admin/phone-remarks', { params: { type } })
    listByType[type] = Array.isArray(response.data) ? response.data : []
    loadedType[type] = true
  } catch (error) {
    ElMessage.error('获取手机备注列表失败')
  } finally {
    loading.value = false
  }
}

const createItem = async () => {
  stopInlineEdit()
  try {
    const { value: remarkName } = await ElMessageBox.prompt('请输入选项名称', '新增', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S/,
      inputErrorMessage: '名称不能为空'
    })
    const type = currentType.value
    const response = await axios.post(`/admin/phone-remarks?type=${type}`, { remarkName })
    if (response.data && response.data.success) {
      ElMessage.success('新增成功')
      loadedType[type] = false
      await fetchByType(type)
    } else {
      ElMessage.error('新增失败')
    }
  } catch (error) {
  }
}

const onTabChange = async (name) => {
  const type = Number(name)
  if (Number.isNaN(type)) return
  stopInlineEdit()
  await fetchByType(type)
}

const currentType = computed(() => {
  const type = Number(activeType.value)
  return Number.isNaN(type) ? 0 : type
})

const currentItems = computed({
  get() {
    return listByType[currentType.value] || []
  },
  set(value) {
    listByType[currentType.value] = value
  }
})

const currentList = computed(() => {
  const type = Number(activeType.value)
  if (Number.isNaN(type)) return []
  return listByType[type] || []
})

const onDragEnd = async () => {
  stopInlineEdit()
  const updated = currentItems.value.map((item, index) => ({
    ...item,
    sort: index + 1
  }))
  currentItems.value = updated
  try {
    const response = await axios.put('/admin/phone-remarks/sort', updated)
    if (response.data && response.data.success) {
      ElMessage.success('排序已更新')
    } else {
      ElMessage.warning('排序更新可能未生效')
    }
  } catch (error) {
    ElMessage.error('排序更新失败')
  }
}

const reorderByValidAndResort = (items) => {
  const validItems = items.filter(i => i.valid === 1)
  const invalidItems = items.filter(i => i.valid !== 1)
  return [...validItems, ...invalidItems].map((i, index) => ({
    ...i,
    sort: index + 1
  }))
}

const toggleValid = async (row) => {
  stopInlineEdit()
  const nextValid = row.valid === 1 ? 0 : 1
  const original = currentItems.value.map(i => ({ ...i }))

  const nextItems = currentItems.value.map(i => (i.id === row.id ? { ...i, valid: nextValid } : i))
  const reordered = reorderByValidAndResort(nextItems)
  currentItems.value = reordered

  try {
    const updateResp = await axios.put(`/admin/phone-remarks/${row.id}`, { valid: nextValid })
    if (!updateResp.data || !updateResp.data.success) {
      throw new Error('update failed')
    }
    const sortResp = await axios.put('/admin/phone-remarks/sort', reordered)
    if (sortResp.data && sortResp.data.success) {
      ElMessage.success('状态已更新')
    } else {
      ElMessage.warning('状态已更新，排序可能未生效')
    }
  } catch (error) {
    currentItems.value = original
    ElMessage.error('状态更新失败')
  }
}

const deleteItem = async (row) => {
  stopInlineEdit()
  try {
    await ElMessageBox.confirm('确认删除该选项？删除后不可恢复', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await axios.delete(`/admin/phone-remarks/${row.id}`)
    if (response.data && response.data.success) {
      ElMessage.success('删除成功')
      const type = typeof row.type === 'number' ? row.type : currentType.value
      loadedType[type] = false
      await fetchByType(type)
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
  }
}

onMounted(async () => {
  await fetchByType(0)
})
</script>

<style scoped>
.box-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.list-header {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #f5f7fa;
  font-weight: bold;
  border-bottom: 1px solid #ebeef5;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 12px 10px;
  border-bottom: 1px solid #ebeef5;
  cursor: move;
}

.ghost {
  opacity: 0.5;
}
</style>
