<template>
  <div style="height: 100%; display: flex; flex-direction: column;">
    <el-card
      class="box-card"
      style="flex: 1; overflow: hidden;"
      :body-style="{ height: '100%', padding: '0', display: 'flex', flexDirection: 'column' }"
    >
      <!-- Header / Search -->
      <div style="padding: 20px;">
        <el-input
          v-model="searchTitle"
          placeholder="搜索标题"
          style="width: 200px; margin-right: 10px;"
          clearable
          @clear="fetchData"
          @keyup.enter="fetchData"
        />
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button type="success" @click="handleAdd">新增</el-button>
      </div>

      <!-- Table -->
      <el-table
        :data="tableData"
        border
        style="width: 100%; flex: 1;"
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="summary" label="摘要" min-width="200" show-overflow-tooltip />
        <el-table-column prop="publishTime" label="发布时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatTime(row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="isOnline" label="状态" width="100" align="center">
           <template #default="{ row }">
             <el-tag :type="row.isOnline === 1 ? 'success' : 'info'">{{ row.isOnline === 1 ? '已上架' : '未上架' }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.isOnline === 1 ? 'warning' : 'success'"
              @click="handleToggleOnline(row)"
            >
              {{ row.isOnline === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div style="padding: 10px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- Dialog -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="1000px" 
      top="5vh"
      destroy-on-close
    >
      <div class="dialog-content">
        <!-- Left Side: Form -->
        <div class="form-section">
          <el-form :model="form" label-width="80px">
            <el-form-item label="标题">
              <el-input v-model="form.title" placeholder="请输入标题" />
            </el-form-item>
            <el-form-item label="摘要">
              <el-input
                type="textarea"
                v-model="form.summary"
                :rows="4"
                placeholder="请输入摘要"
              />
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" />
            </el-form-item>
            <el-form-item label="状态">
              <el-switch
                v-model="form.isOnline"
                :active-value="1"
                :inactive-value="0"
                active-text="上架"
                inactive-text="下架"
              />
            </el-form-item>
          </el-form>

          <!-- Toolbar moved here -->
          <div style="margin-top: 20px; border: 1px solid #ccc; border-radius: 4px; background-color: #f5f5f5;">
             <div style="padding: 10px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center;">
               <span style="font-weight: bold; color: #666;">内容编辑器</span>
               <el-button size="small" type="primary" @click="handleInsertImageUrl">插入网络图片</el-button>
             </div>
             <!-- CKEditor 5 -->
             <div class="ckeditor-wrapper">
               <ckeditor :editor="editor" v-model="form.content" :config="editorConfig" @ready="handleEditorReady"></ckeditor>
             </div>
          </div>
        </div>

        <!-- Right Side: Mobile Simulator -->
        <div class="mobile-section">
          
          <div class="mobile-device">
             <div class="mobile-screen">
                <!-- Mini Program Navigation Bar -->
                <div class="mp-nav-bar">
                   <div class="mp-nav-left"><el-icon><ArrowLeft /></el-icon></div>
                   <div class="mp-nav-title">行情详情</div>
                   <div class="mp-nav-capsule">
                      <div class="mp-capsule-dots">...</div>
                      <div class="mp-capsule-line"></div>
                      <div class="mp-capsule-circle">◎</div>
                   </div>
                </div>
                
                <!-- Scrollable Content Area -->
                <div class="mp-content-area">
                   <div class="mp-article-header">
                      <h1 class="mp-title">{{ form.title || '请输入标题' }}</h1>
                      <div class="mp-meta">
                         <span class="mp-time">{{ form.publishTime ? formatTime(form.publishTime) : formatTime(new Date()) }}</span>
                      </div>
                   </div>
                   
                   <!-- Preview Content -->
                   <div class="editor-wrapper">
                     <div v-html="form.content" class="tiptap-editor-content"></div>
                   </div>
                </div>
             </div>
          </div>
          <div style="text-align: center; margin-top: 10px; color: #666; font-size: 12px;">
            手机预览模式 (所见即所得)
          </div>
        </div>
      </div>
      
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
import { ref, onMounted, shallowRef, onBeforeUnmount, watch } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

// CKEditor 5 Imports
import { Ckeditor } from '@ckeditor/ckeditor5-vue'
import ClassicEditor from '@ckeditor/ckeditor5-build-classic'
import '@ckeditor/ckeditor5-build-classic/build/translations/zh-cn'

const ckeditor = Ckeditor

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchTitle = ref('')

const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({
  id: null,
  title: '',
  summary: '',
  content: '',
  sort: 0,
  isOnline: 1
})

// CKEditor Configuration
const editor = ClassicEditor
const editorInstance = shallowRef(null)

const handleEditorReady = (editor) => {
  editorInstance.value = editor
}

const handleInsertImageUrl = () => {
  if (!editorInstance.value) {
    ElMessage.warning('编辑器尚未加载完成')
    return
  }

  ElMessageBox.prompt('请输入图片URL链接', '插入网络图片', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^(https?:|data:image\/)/,
    inputErrorMessage: '请输入有效的图片URL (http/https 或 base64)'
  }).then(({ value }) => {
    if (value) {
      editorInstance.value.execute('insertImage', { source: value })
    }
  }).catch(() => {
    // Cancelled
  })
}

// Custom Upload Adapter Plugin
class MyUploadAdapter {
    constructor(loader) {
        this.loader = loader;
    }

    upload() {
        return this.loader.file
            .then(file => new Promise((resolve, reject) => {
                const formData = new FormData();
                formData.append('file', file);

                axios.post('/api/images', formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                        'Authorization': `Bearer ${localStorage.getItem('admin_token')}`
                    }
                })
                .then(response => {
                    if (response.data && response.data.url) {
                        resolve({
                            default: response.data.url
                        });
                    } else {
                        reject('上传失败：无效的响应');
                    }
                })
                .catch(error => {
                    reject('上传失败: ' + error.message);
                });
            }));
    }

    abort() {
        // Abort request logic if needed
    }
}

function MyCustomUploadAdapterPlugin(editor) {
    editor.plugins.get('FileRepository').createUploadAdapter = (loader) => {
        return new MyUploadAdapter(loader);
    };
}

const editorConfig = {
    placeholder: '请输入内容...',
    language: 'zh-cn', // Classic build might not include zh-cn by default, will check
    extraPlugins: [MyCustomUploadAdapterPlugin],
    toolbar: [
        'heading', '|',
        'bold', 'italic', 'link', 'bulletedList', 'numberedList', 'blockQuote', '|',
        'insertTable', 'uploadImage', '|',
        'undo', 'redo'
    ]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/admin/market-infos', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        title: searchTitle.value || undefined
      }
    })
    if (res.data) {
      tableData.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增行情资讯'
  form.value = { id: null, title: '', summary: '', content: '', sort: 0, isOnline: 1 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑行情资讯'
  
  // Clone the row
  const rowData = { ...row }
  
  // Check if content is wrapped and unwrap it for the editor
  // This prevents the editor from stripping or mishandling the outer div, 
  // and ensures "what you see is what you get" inside the editor.
  if (rowData.content && rowData.content.includes('market-info-wrapper')) {
     // Use a more robust regex that handles potential attribute issues
     // Matches <div class="market-info-wrapper" ... > CONTENT </div>
     const wrapperRegex = /<div class="market-info-wrapper"[\s\S]*?>([\s\S]*)<\/div>$/;
     const match = rowData.content.match(wrapperRegex);
     if (match && match[1]) {
        rowData.content = match[1];
     }
  }
  
  form.value = rowData
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该条资讯吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await axios.delete(`/admin/market-infos/${row.id}`)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleToggleOnline = async (row) => {
  const newStatus = row.isOnline === 1 ? 0 : 1
  try {
    await axios.put(`/admin/market-infos/${row.id}`, { ...row, isOnline: newStatus })
    ElMessage.success(newStatus === 1 ? '上架成功' : '下架成功')
    fetchData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const submitForm = async () => {
  if (!form.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  
  // We will create a new form object to submit, preserving the original form for the editor
  const submissionForm = { ...form.value };
  
  // Define the consistent style wrapper
  // Using single quotes for font names inside the style attribute string to avoid HTML parsing issues
  // Added background-color, padding, and box-sizing for more robust self-contained styling
  // Increased line-height to 2.0 per user request for better readability
  const baseStyle = "font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif; font-size: 14px; line-height: 2.0; color: #333333; background-color: #ffffff; text-align: justify; word-wrap: break-word; padding: 15px; box-sizing: border-box;";
  
  // Always wrap the content in the wrapper div if it's not already wrapped (it shouldn't be, because we unwrapped it on load)
  // Even if it was wrapped (e.g. user pasted HTML), we ensure our standard wrapper is the outermost.
  // To avoid double wrapping if something went wrong, we can check.
  if (!submissionForm.content.includes('market-info-wrapper')) {
     submissionForm.content = `<div class="market-info-wrapper" style="${baseStyle}">${submissionForm.content}</div>`;
  } else {
     // If it is already wrapped (e.g. from previous edit), update the style attribute to the latest version
     // This ensures old content gets the new robust styles (like background-color) upon re-saving
     submissionForm.content = submissionForm.content.replace(/<div class="market-info-wrapper" style="[^"]+">/, `<div class="market-info-wrapper" style="${baseStyle}">`);
  }

   // Force inline styles for Paragraphs and List Items to ensure Mini Program respects the spacing
   // Helper function to inject or update style attribute
   const injectStyle = (html, tag, styleString) => {
     // Regex to match the tag, capturing attributes
     const regex = new RegExp(`<${tag}(\\s+[^>]*)?>`, 'gi');
     return html.replace(regex, (match, attrs) => {
        // If no attributes, just add style
        if (!attrs) return `<${tag} style="${styleString}">`;
        
        // Check if style attribute exists
        if (attrs.match(/style=["']/i)) {
           // Update existing style
           return match.replace(/style=(["'])(.*?)\1/i, (m, quote, content) => {
              // Remove existing line-height to ensure we enforce ours, but KEEP margin-bottom to respect user choice
              let newContent = content.replace(/line-height:\s*[^;]+;?/gi, '');
              // Append new styles
              if (!newContent.trim().endsWith(';')) newContent += '; ';
              return `style=${quote}${newContent}${styleString}${quote}`;
           });
        } else {
           // Add style attribute to existing attributes
           return `<${tag}${attrs} style="${styleString}">`;
        }
     });
   };
 
   // 1. Inject style into <p> tags: only line-height: 2.0. Remove forced margin-bottom to let user adjust or use defaults.
   submissionForm.content = injectStyle(submissionForm.content, 'p', 'line-height: 2.0;');
   
   // 2. Inject style into <li> tags: only line-height: 2.0.
   submissionForm.content = injectStyle(submissionForm.content, 'li', 'line-height: 2.0;');

  // Ensure images have max-width: 100% to prevent overflow
  // This is a simple regex replacement to add style if it doesn't have max-width
  // Note: This is a basic safeguard. Complex HTML might need a parser, but regex is usually fine for editor output.
  submissionForm.content = submissionForm.content.replace(/<img(?![^>]*style=["'][^"']*max-width)/g, '<img style="max-width: 100%; height: auto; display: block; margin: 10px 0;"');
  
  try {
    if (submissionForm.id) {
      await axios.put(`/admin/market-infos/${submissionForm.id}`, submissionForm)
      ElMessage.success('更新成功')
    } else {
      await axios.post('/admin/market-infos', submissionForm)

      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const formatTime = (val) => {
  if (!val) return ''
  if (Array.isArray(val)) {
    const [year, month, day, hour, minute, second] = val
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second || 0).padStart(2, '0')}`
  }
  if (typeof val === 'string' && val.includes('T')) {
    return val.replace('T', ' ').substring(0, 19)
  }
  return val
}

onMounted(() => {
  fetchData()
})

</script>

<style scoped>
/* CKEditor Overrides */
:deep(.ck-editor__editable) {
  min-height: 400px;
  max-height: 600px;
}

.dialog-content {
  display: flex;
  gap: 20px;
  height: 700px;
  align-items: flex-start;
}

.form-section {
  flex: 1;
  padding-right: 20px;
  border-right: 1px solid #eee;
  height: 100%;
  overflow-y: auto;
}

.mobile-section {
  width: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.mobile-device {
  width: 375px;
  height: 667px;
  border: 12px solid #333;
  border-radius: 30px;
  background: #fff;
  position: relative;
  overflow: hidden;
  box-shadow: 0 0 20px rgba(0,0,0,0.2);
}

.mobile-screen {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #fff;
}

.mp-nav-bar {
  height: 44px;
  background-color: #1989fa;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 10px;
  position: relative;
  flex-shrink: 0;
}
.mp-nav-left {
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
}
.mp-nav-title {
  font-size: 16px;
  font-weight: 500;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}
.mp-nav-capsule {
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 0 8px;
  height: 30px;
  box-sizing: border-box;
}
.mp-capsule-dots {
  font-size: 16px;
  line-height: 1;
  margin-right: 6px;
  font-weight: bold;
}
.mp-capsule-line {
  width: 1px;
  height: 14px;
  background: rgba(255, 255, 255, 0.3);
  margin: 0 6px;
}
.mp-capsule-circle {
  font-size: 16px;
  line-height: 1;
  font-weight: bold;
}

.mp-content-area {
  flex: 1;
  overflow-y: auto;
  background-color: #fff;
  padding-bottom: 30px;
  display: flex;
  flex-direction: column;
}

.mp-article-header {
  padding: 20px 15px 10px 15px;
  background-color: #fff;
}
.mp-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px 0;
  line-height: 1.4;
  text-align: left;
}
.mp-meta {
  font-size: 13px;
  color: #999;
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.mp-time {
  margin-left: 10px;
}

/* TipTap Editor Wrapper */
.editor-wrapper {
  flex: 1;
  /* Ensure styles match Mini Program Output */
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 14px;
  line-height: 2.0;
  color: #333;
  padding: 0 15px; /* Match padding in baseStyle */
  cursor: text; /* Show text cursor to indicate editable */
  min-height: 300px; /* Ensure click area is large */
}

/* TipTap Specific Styles to Match Mini Program */
.editor-wrapper :deep(.ProseMirror) {
  outline: none;
  min-height: 300px;
}

.editor-wrapper :deep(p) {
  margin: 0;
  padding: 0;
  line-height: 2.0;
}

.editor-wrapper :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 10px 0;
}

.editor-wrapper :deep(ul),
.editor-wrapper :deep(ol) {
  padding-left: 20px;
  margin: 10px 0;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  padding: 5px;
}
</style>