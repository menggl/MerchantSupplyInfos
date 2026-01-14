<template>
  <div style="height: 100%; display: flex; flex-direction: column;">
    <!-- 工具栏 -->
    <div style="padding-bottom: 15px; display: flex; align-items: center;">
      <div style="margin-left: auto; display: flex; align-items: center; gap: 8px;">
        <span style="color: #909399; font-size: 13px;">请上传包含品牌、系列、型号、配置的标准JSON文件</span>
        <el-upload
          action=""
          :http-request="handleImportDict"
          :show-file-list="false"
          accept=".json"
        >
          <el-button type="success" :icon="Upload">导入全量字典</el-button>
        </el-upload>
      </div>
    </div>

    <el-row :gutter="20" style="flex: 1; overflow: hidden;">
    <!-- 品牌列表 -->
    <el-col :span="6" style="height: 100%;">
      <el-card class="box-card" style="height: 100%; display: flex; flex-direction: column;">
        <template #header>
          <div class="card-header">
            <span>品牌列表</span>
            <el-button type="primary" size="small" @click="createNewBrand">添加</el-button>
          </div>
        </template>
        
        <draggable 
          v-model="brands" 
          item-key="id" 
          @end="onDragEnd('brand')"
          class="list-container"
        >
          <template #item="{ element }">
            <div 
              class="list-item" 
              :class="{ 'active': selectedBrand?.id === element.id }"
              @click="selectBrand(element)"
            >
              <div class="item-content">
                <div class="name-wrapper" @dblclick="enableEdit(element, 'brand')">
                  <el-input 
                    v-if="editingId === element.id && editingType === 'brand'"
                    v-model="editingName"
                    ref="editInputRef"
                    size="small"
                    @blur="saveEdit(element, 'brand')"
                    @keyup.enter="saveEdit(element, 'brand')"
                  />
                  <span v-else>{{ element.name }}</span>
                </div>
                <el-button type="danger" circle size="small" :icon="Minus" @click.stop="handleDeleteBrand(element)" class="delete-btn" />
              </div>
            </div>
          </template>
        </draggable>
      </el-card>
    </el-col>

    <!-- 系列列表 -->
    <el-col :span="6" style="height: 100%;">
      <el-card class="box-card" style="height: 100%; display: flex; flex-direction: column;">
        <template #header>
          <div class="card-header">
            <span>系列列表</span>
            <el-button type="primary" size="small" :disabled="!selectedBrand" @click="createNewSeries">添加</el-button>
          </div>
        </template>
        <draggable 
          v-if="selectedBrand"
          v-model="seriesList" 
          item-key="id" 
          @end="onDragEnd('series')"
          class="list-container"
        >
          <template #item="{ element }">
            <div 
              class="list-item"
              :class="{ 'active': selectedSeries?.id === element.id }"
              @click="selectSeries(element)"
            >
              <div class="item-content">
                <div class="name-wrapper" @dblclick="enableEdit(element, 'series')">
                  <el-input 
                    v-if="editingId === element.id && editingType === 'series'"
                    v-model="editingName"
                    ref="editInputRef"
                    size="small"
                    @blur="saveEdit(element, 'series')"
                    @keyup.enter="saveEdit(element, 'series')"
                  />
                  <span v-else>{{ element.seriesName }}</span>
                </div>
                <el-button type="danger" circle size="small" :icon="Minus" @click.stop="handleDeleteSeries(element)" class="delete-btn" />
              </div>
            </div>
          </template>
        </draggable>
        <div v-if="selectedBrand && seriesList.length === 0" class="empty-text">暂无数据</div>
        <div v-if="!selectedBrand" class="empty-text">请先选择品牌</div>
      </el-card>
    </el-col>

    <!-- 型号列表 -->
    <el-col :span="6" style="height: 100%;">
      <el-card class="box-card" style="height: 100%; display: flex; flex-direction: column;">
        <template #header>
          <div class="card-header">
            <span>型号列表</span>
            <el-button type="primary" size="small" :disabled="!selectedSeries" @click="createNewModel">添加</el-button>
          </div>
        </template>
        <draggable 
          v-if="selectedSeries"
          v-model="modelList" 
          item-key="id" 
          @end="onDragEnd('model')"
          class="list-container"
        >
          <template #item="{ element }">
            <div 
              class="list-item"
              :class="{ 'active': selectedModel?.id === element.id }"
              @click="selectModel(element)"
            >
              <div class="item-content">
                <div class="name-wrapper" @dblclick="enableEdit(element, 'model')">
                  <el-input 
                    v-if="editingId === element.id && editingType === 'model'"
                    v-model="editingName"
                    ref="editInputRef"
                    size="small"
                    @blur="saveEdit(element, 'model')"
                    @keyup.enter="saveEdit(element, 'model')"
                  />
                  <span v-else>{{ element.modelName }}</span>
                </div>
                <el-button type="danger" circle size="small" :icon="Minus" @click.stop="handleDeleteModel(element)" class="delete-btn" />
              </div>
            </div>
          </template>
        </draggable>
        <div v-if="selectedSeries && modelList.length === 0" class="empty-text">暂无数据</div>
        <div v-if="!selectedSeries" class="empty-text">请先选择系列</div>
      </el-card>
    </el-col>

    <!-- 配置列表 -->
    <el-col :span="6" style="height: 100%;">
      <el-card class="box-card" style="height: 100%; display: flex; flex-direction: column;">
        <template #header>
          <div class="card-header">
            <span>配置列表</span>
            <el-button type="primary" size="small" :disabled="!selectedModel" @click="createNewSpec">添加</el-button>
          </div>
        </template>
        <draggable 
          v-if="selectedModel"
          v-model="specList" 
          item-key="id" 
          @end="onDragEnd('spec')"
          class="list-container"
        >
          <template #item="{ element }">
            <div 
              class="list-item"
            >
              <div class="item-content">
                <div class="name-wrapper" @dblclick="enableEdit(element, 'spec')">
                  <el-input 
                    v-if="editingId === element.id && editingType === 'spec'"
                    v-model="editingName"
                    ref="editInputRef"
                    size="small"
                    @blur="saveEdit(element, 'spec')"
                    @keyup.enter="saveEdit(element, 'spec')"
                  />
                  <span v-else>{{ element.specName }}</span>
                </div>
                <el-button type="danger" circle size="small" :icon="Minus" @click.stop="handleDeleteSpec(element)" class="delete-btn" />
              </div>
            </div>
          </template>
        </draggable>
        <div v-if="selectedModel && specList.length === 0" class="empty-text">暂无数据</div>
        <div v-if="!selectedModel" class="empty-text">请先选择型号</div>
      </el-card>
    </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { Minus, Upload } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'

export default {
  name: 'ProductManager',
  components: {
    draggable,
    Minus,
    Upload
  },
  setup() {
    // Data Lists
    const brands = ref([])
    const seriesList = ref([])
    const modelList = ref([])
    const specList = ref([])

    // Selected Items
    const selectedBrand = ref(null)
    const selectedSeries = ref(null)
    const selectedModel = ref(null)

    // Editing State
    const editingId = ref(null)
    const editingType = ref(null) // 'brand', 'series', 'model', 'spec'
    const editingName = ref('')
    const editInputRef = ref(null)

    // --- Data Loading ---

    const loadBrands = async () => {
      try {
        const response = await axios.get('/admin/brands/all')
        brands.value = response.data
        
        // Auto select first if none selected or selection invalid
        if (brands.value.length > 0) {
            if (!selectedBrand.value || !brands.value.find(b => b.id === selectedBrand.value.id)) {
                selectBrand(brands.value[0])
            }
        } else {
             selectedBrand.value = null
             seriesList.value = []
             selectedSeries.value = null
             modelList.value = []
             selectedModel.value = null
             specList.value = []
        }
      } catch (error) {
        ElMessage.error('加载品牌列表失败')
        console.error(error)
      }
    }

    const loadSeries = async (brand) => {
      if (!brand) return
      try {
        const response = await axios.get(`/admin/series/by-brand/${brand.id}`)
        if (response.data && response.data.success) {
            seriesList.value = response.data.seriesArr.map(item => ({
                ...item,
                id: item.seriesId // Map seriesId to id for frontend compatibility
            }))
        } else {
            seriesList.value = []
        }
        
        // Auto select first if none selected or selection invalid
        if (seriesList.value.length > 0) {
             if (!selectedSeries.value || !seriesList.value.find(s => s.id === selectedSeries.value.id)) {
                 selectSeries(seriesList.value[0])
             }
        } else {
             selectedSeries.value = null
             modelList.value = []
             selectedModel.value = null
             specList.value = []
        }
      } catch (error) {
        ElMessage.error('加载系列列表失败')
        console.error(error)
      }
    }

    const loadModels = async (brand, series) => {
      if (!brand || !series) return
      try {
        const response = await axios.get(`/admin/models/by-series/${series.id}`)
        if (response.data && response.data.success) {
            modelList.value = response.data.modelArr.map(item => ({
                ...item,
                id: item.modelId // Map modelId to id
            }))
        } else {
            modelList.value = []
        }
        
        // Auto select first if none selected or selection invalid
        if (modelList.value.length > 0) {
             if (!selectedModel.value || !modelList.value.find(m => m.id === selectedModel.value.id)) {
                 selectModel(modelList.value[0])
             }
        } else {
             selectedModel.value = null
             specList.value = []
        }
      } catch (error) {
        ElMessage.error('加载型号列表失败')
        console.error(error)
      }
    }

    const loadSpecs = async (model) => {
      if (!model) return
      try {
        const response = await axios.get(`/admin/specs/by-model/${model.id}`)
        if (response.data && response.data.success) {
            specList.value = response.data.specArr.map(item => ({
                ...item,
                id: item.specId // Map specId to id
            }))
        } else {
            specList.value = []
        }
      } catch (error) {
        ElMessage.error('加载配置列表失败')
        console.error(error)
      }
    }

    // --- Selection Handlers ---

    const selectBrand = (brand) => {
      if (selectedBrand.value?.id === brand.id) return
      selectedBrand.value = brand
      selectedSeries.value = null
      selectedModel.value = null
      seriesList.value = []
      modelList.value = []
      specList.value = []
      loadSeries(brand)
    }

    const selectSeries = (series) => {
      if (selectedSeries.value?.id === series.id) return
      selectedSeries.value = series
      selectedModel.value = null
      modelList.value = []
      specList.value = []
      loadModels(selectedBrand.value, series)
    }

    const selectModel = (model) => {
      if (selectedModel.value?.id === model.id) return
      selectedModel.value = model
      specList.value = []
      loadSpecs(model)
    }

    // --- Creation Handlers ---

    const createNewBrand = async () => {
      try {
        const { value: newName } = await ElMessageBox.prompt('请输入品牌名称', '添加品牌', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /\S/,
          inputErrorMessage: '名称不能为空'
        })
        
        await axios.post('/admin/brands', { name: newName })
        await loadBrands()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('创建失败')
        }
      }
    }

    const createNewSeries = async () => {
      if (!selectedBrand.value) return
      try {
        const { value: newName } = await ElMessageBox.prompt('请输入系列名称', '添加系列', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /\S/,
          inputErrorMessage: '名称不能为空'
        })

        // Changed to use query param for brandId as expected by backend
        await axios.post(`/admin/series?brandId=${selectedBrand.value.id}`, { 
          seriesName: newName 
        })
        await loadSeries(selectedBrand.value)
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('创建失败')
        }
      }
    }

    const createNewModel = async () => {
      if (!selectedBrand.value || !selectedSeries.value) return
      try {
        const { value: newName } = await ElMessageBox.prompt('请输入型号名称', '添加型号', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /\S/,
          inputErrorMessage: '名称不能为空'
        })

        // Changed to use query params for brandId and seriesId
        await axios.post(`/admin/models?brandId=${selectedBrand.value.id}&seriesId=${selectedSeries.value.id}`, { 
          modelName: newName 
        })
        await loadModels(selectedBrand.value, selectedSeries.value)
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('创建失败')
        }
      }
    }

    const createNewSpec = async () => {
      if (!selectedModel.value) return
      try {
        const { value: newName } = await ElMessageBox.prompt('请输入配置名称', '添加配置', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /\S/,
          inputErrorMessage: '名称不能为空'
        })

        // Changed to use query param for modelId
        await axios.post(`/admin/specs?modelId=${selectedModel.value.id}`, { 
          specName: newName 
        })
        await loadSpecs(selectedModel.value)
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('创建失败')
        }
      }
    }

    // --- Edit Handlers ---

    const enableEdit = (item, type) => {
      editingId.value = item.id
      editingType.value = type
      editingName.value = getName(item, type)
      nextTick(() => {
        if (editInputRef.value && editInputRef.value.length > 0) {
            editInputRef.value[0].focus()
        } else if (editInputRef.value) {
            editInputRef.value.focus()
        }
      })
    }

    const getName = (item, type) => {
      switch(type) {
        case 'brand': return item.name
        case 'series': return item.seriesName
        case 'model': return item.modelName
        case 'spec': return item.specName
        default: return ''
      }
    }

    const saveEdit = async (item, type) => {
      if (editingId.value !== item.id) return 
      
      const newName = editingName.value.trim()
      if (!newName) {
        ElMessage.warning('名称不能为空')
        return
      }
      
      if (newName === getName(item, type)) {
        editingId.value = null
        editingType.value = null
        return
      }

      try {
        switch(type) {
          case 'brand':
            await axios.put(`/admin/brands/${item.id}`, { brandName: newName })
            break
          case 'series':
            await axios.put(`/admin/series/${item.id}`, { seriesName: newName })
            break
          case 'model':
            await axios.put(`/admin/models/${item.id}`, { modelName: newName })
            break
          case 'spec':
            await axios.put(`/admin/specs/${item.id}`, { specName: newName })
            break
        }
        
        ElMessage.success('更新成功')
        editingId.value = null
        editingType.value = null
        
        // Reloading might disturb drag order if not saved, but edit is separate.
        // If we want to be safe, we reload.
        if (type === 'brand') loadBrands()
        else if (type === 'series') loadSeries(selectedBrand.value)
        else if (type === 'model') loadModels(selectedBrand.value, selectedSeries.value)
        else if (type === 'spec') loadSpecs(selectedModel.value)

      } catch (error) {
        ElMessage.error('更新失败')
        console.error(error)
      }
    }

    // --- Delete Handlers ---

    const confirmDelete = async (message, action) => {
       try {
         await ElMessageBox.confirm(message, '提示', {
           confirmButtonText: '确定',
           cancelButtonText: '取消',
           type: 'warning'
         })
         await action()
       } catch (e) {
         // Cancelled
       }
    }

    const handleDeleteBrand = (item) => {
      confirmDelete(`确定删除品牌 "${item.name}" 吗?`, async () => {
        await axios.delete(`/admin/brands/${item.id}`)
        if (selectedBrand.value?.id === item.id) {
          selectedBrand.value = null
          seriesList.value = []
          modelList.value = []
          specList.value = []
        }
        loadBrands()
        ElMessage.success('删除成功')
      })
    }

    const handleDeleteSeries = (item) => {
      confirmDelete(`确定删除系列 "${item.seriesName}" 吗?`, async () => {
        await axios.delete(`/admin/series/${item.id}`)
        if (selectedSeries.value?.id === item.id) {
          selectedSeries.value = null
          modelList.value = []
          specList.value = []
        }
        loadSeries(selectedBrand.value)
        ElMessage.success('删除成功')
      })
    }

    const handleDeleteModel = (item) => {
      confirmDelete(`确定删除型号 "${item.modelName}" 吗?`, async () => {
        await axios.delete(`/admin/models/${item.id}`)
        if (selectedModel.value?.id === item.id) {
          selectedModel.value = null
          specList.value = []
        }
        loadModels(selectedBrand.value, selectedSeries.value)
        ElMessage.success('删除成功')
      })
    }

    const handleDeleteSpec = (item) => {
      confirmDelete(`确定删除配置 "${item.specName}" 吗?`, async () => {
        await axios.delete(`/admin/specs/${item.id}`)
        loadSpecs(selectedModel.value)
        ElMessage.success('删除成功')
      })
    }

    // --- Drag and Sort Handler ---
    
    const onDragEnd = async (type) => {
      let list = []
      if (type === 'brand') list = brands.value
      else if (type === 'series') list = seriesList.value
      else if (type === 'model') list = modelList.value
      else if (type === 'spec') list = specList.value

      // Update sort index locally first
      list.forEach((item, index) => {
        item.sort = index
      })
      
      try {
        if (type === 'brand') {
            await axios.put('/admin/brands/sort', list)
        } else if (type === 'series') {
            if (selectedBrand.value) {
                await axios.put(`/admin/series/sort?brandId=${selectedBrand.value.id}`, list)
            }
        } else if (type === 'model') {
            if (selectedSeries.value) {
                await axios.put(`/admin/models/sort?seriesId=${selectedSeries.value.id}`, list)
            }
        } else if (type === 'spec') {
            if (selectedModel.value) {
                await axios.put(`/admin/specs/sort?modelId=${selectedModel.value.id}`, list)
            }
        }
      } catch (e) {
        ElMessage.error('排序保存失败')
        console.error(e)
        // Reload to revert UI to server state
        if (type === 'brand') loadBrands()
        else if (type === 'series') loadSeries(selectedBrand.value)
        else if (type === 'model') loadModels(selectedBrand.value, selectedSeries.value)
        else if (type === 'spec') loadSpecs(selectedModel.value)
      }
    }

    // --- Import Handler ---
    const handleImportDict = async (param) => {
      const formData = new FormData()
      formData.append('file', param.file)
      
      try {
        const response = await axios.post('/admin/dict/import/file', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        
        if (response.status === 200) {
           ElMessage.success(response.data || '导入成功')
           // Refresh brands to reflect changes
           loadBrands()
        }
      } catch (error) {
        console.error(error)
        const msg = error.response?.data || error.message || '导入失败'
        ElMessage.error(typeof msg === 'string' ? msg : '导入失败')
      }
    }

    onMounted(() => {
      loadBrands()
    })

    return {
      // Data
      brands, seriesList, modelList, specList,
      selectedBrand, selectedSeries, selectedModel,
      editingId, editingType, editingName, editInputRef,
      // Methods
      handleImportDict,
      loadBrands, loadSeries, loadModels, loadSpecs,
      selectBrand, selectSeries, selectModel,
      createNewBrand, createNewSeries, createNewModel, createNewSpec,
      enableEdit, saveEdit,
      handleDeleteBrand, handleDeleteSeries, handleDeleteModel, handleDeleteSpec,
      onDragEnd,
      // Icons
      Minus,
      Upload
    }
  }
}
</script>

<style scoped>
.box-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.list-container {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
}
.list-item {
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}
.list-item:hover {
  background-color: #f5f7fa;
}
.list-item.active {
  background-color: #e6f7ff;
  border-left: 3px solid #1890ff;
}
.item-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.name-wrapper {
  flex: 1;
  margin-right: 10px;
  min-height: 24px;
  display: flex;
  align-items: center;
}
.delete-btn {
  opacity: 0;
  transition: opacity 0.2s;
}
.list-item:hover .delete-btn {
  opacity: 1;
}
.empty-text {
  color: #909399;
  text-align: center;
  margin-top: 20px;
}
</style>
