<script setup>
import {onMounted, reactive, ref} from "vue"
import {ElMessage, ElMessageBox} from "element-plus"
import {getAllUid, saveUid, removeUidList} from "@api/uid/uid.js"
import {goBack, toHomePage} from "@api/web/web.js"

// 表单数据
const formData = reactive({
  show: false,
  edit: false,
  uid: '',
  as: '',
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const multipleSelection = ref(new Set())

// 表单验证规则
const rules = {
  uid: [
    {required: true, message: '请输入 UID', trigger: 'blur'},
    {min: 9, max: 20, message: 'UID 长度应在 9-20 个字符之间', trigger: 'blur'}
  ],
  as: [
    {required: true, message: '请输入别称', trigger: 'blur'}
  ]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const response = await getAllUid()
    tableData.value = response || []
  } catch (error) {
    console.error('获取 UID 列表失败:', error)
    ElMessage.error('获取 UID 列表失败')
  } finally {
    loading.value = false
  }
}

// 打开新增对话框
const handleAdd = () => {
  formData.edit = false
  formData.uid = ''
  formData.as = ''
  formData.show = true
}

// 打开编辑对话框
const handleEdit = (row) => {
  formData.edit = true
  formData.uid = row.uid
  formData.as = row.as
  formData.show = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    const action = formData.edit ? '修改' : '新增'
    await ElMessageBox.confirm(`确定要${action}该 UID 映射吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const uidInfo = {
      uid: formData.uid,
      as: formData.as,
    }

    await saveUid(uidInfo)
    // ElMessage.success(`${action}成功`)
    formData.show = false
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 删除单条
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要移除 UID "${row.uid}" 的映射吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await removeUidList(row.uid)
    // ElMessage.success('删除成功')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (multipleSelection.value.size === 0) {
    ElMessage.warning('请至少选择一项')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要移除选中的 ${multipleSelection.value.size} 条记录吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await removeUidList(Array.from(multipleSelection.value))
    // ElMessage.success('批量删除成功')
    multipleSelection.value.clear()
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  multipleSelection.value.clear()
  selection.forEach(item => {
    multipleSelection.value.add(item.uid)
  })
}

// 跳转主页
const goToHome = async () => {
  await toHomePage()
}

// 返回上一页
const goToBack = async () => {
  await goBack()
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="home">
    <div class="uid-manager">
      <div class="manager-container">
        <h2 class="manager-title">UID 映射管理</h2>

        <div class="toolbar">
          <el-button type="primary" @click="handleAdd" class="action-button">
            ➕ 新增映射
          </el-button>
          <el-button 
            type="danger" 
            @click="handleBatchDelete" 
            :disabled="multipleSelection.size === 0"
            class="action-button"
          >
            🗑️ 批量删除
          </el-button>
          <el-button @click="loadData" :loading="loading" class="action-button" round>
            <span class="button-icon" :class="{ 'rotating': loading }">↻</span>
            <span class="button-text">刷新</span>
          </el-button>
        </div>

        <div class="table-container" v-if="tableData.length > 0">
          <el-table
            v-loading="loading"
            :data="tableData"
            @selection-change="handleSelectionChange"
            style="width: 100%"
          >
            <el-table-column type="selection" />
            <el-table-column prop="uid" label="UID" />
            <el-table-column prop="as" label="别称" />
            <el-table-column label="操作"  fixed="right">
              <template #default="{ row }">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="handleEdit(row)"
                  class="table-button"
                >
                  ✏️ 编辑
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="handleDelete(row)"
                  class="table-button"
                >
                  🗑️ 删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="empty-tip" v-else-if="!loading && tableData.length === 0">
          <div class="empty-icon">📭</div>
          <p class="empty-text">暂无 UID 映射数据</p>
          <el-button type="primary" @click="handleAdd">立即添加</el-button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="formData.show"
      :title="formData.edit ? '编辑 UID 映射' : '新增 UID 映射'"
      style="max-width: 300px"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="rules" >
        <el-form-item label="UID" prop="uid">
          <el-input
            v-model="formData.uid"
            placeholder="请输入 UID"
            clearable
            :disabled="formData.edit"
          />
        </el-form-item>
        <el-form-item label="别称" prop="as">
          <el-input
            v-model="formData.as"
            placeholder="请输入别称"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="formData.show = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 底部按钮 -->
    <div class="fixed-back">
      <button @click="goToBack" class="btn secondary">返回上一页</button>
    </div>
    <div class="fixed-footer">
      <button @click="goToHome" class="btn secondary">🏠 返回主页</button>
    </div>
  </div>
</template>

<style scoped>
.uid-manager {
  padding: 30px;
  min-width: 1200px;
  margin: 0 auto;
}

.manager-container {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 80%);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 15px 35px rgba(102, 126, 234, 0.3);
  backdrop-filter: blur(10px);
}

.manager-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 32px;
  font-weight: 600;
  color: transparent;
  background: linear-gradient(90deg, #ff6b6b, #ef006a);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.toolbar {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
  justify-content: flex-start;
}

.action-button {
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.3s ease;
  min-width: 120px;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.2);
}

.table-container {
  height: 70vh;
  background: white;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.table-button {
  margin-right: 8px;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 13px;
}

.empty-tip {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 15px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
  opacity: 0.6;
}

.empty-text {
  font-size: 18px;
  color: #7f8c8d;
  margin-bottom: 25px;
}

.button-icon {
  display: inline-block;
  margin-right: 6px;
  font-size: 16px;
  transition: transform 0.3s ease;
}

.button-icon.rotating {
  animation: rotate 1s linear infinite;
}

.button-text {
  letter-spacing: 1px;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/*.fixed-back {
  position: fixed;
  bottom: 80px;
  left: 30px;
  z-index: 1000;
}

.fixed-footer {
  position: fixed;
  bottom: 80px;
  right: 30px;
  z-index: 1000;
}

.btn {
  padding: 12px 24px;
  border-radius: 25px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.btn.secondary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn.secondary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}*/

/*@media (max-width: 768px) {
  .uid-manager {
    padding: 20px;
    min-width: 50vw;
  }
}

@media (max-width: 480px) {
  .uid-manager {
    padding: 15px;
    min-width: 50vw;
  }
}*/


@media (max-width: 768px) {
    .uid-manager {
        padding: 20px;
        min-width: 50vw;
    }

    .manager-container {
        padding: 30px;
        border-radius: 15px;
    }

    .manager-title {
        font-size: 24px;
        margin-bottom: 20px;
    }

    .toolbar {
        flex-wrap: wrap;
        gap: 10px;
        justify-content: center;
    }

    .action-button {
        min-width: 100px;
        font-size: 14px;
    }

    .table-container {
        height: 50vh;
        padding: 15px;
        border-radius: 10px;
    }

    .table-button {
        padding: 5px 10px;
        font-size: 12px;
    }

    .empty-tip {
        padding: 40px 15px;
        border-radius: 10px;
    }

    .empty-icon {
        font-size: 60px;
        margin-bottom: 15px;
    }

    .empty-text {
        font-size: 16px;
    }
}

@media (max-width: 480px) {
    .uid-manager {
        padding: 15px;
    }

    .manager-container {
        padding: 20px;
        border-radius: 10px;
    }

    .manager-title {
        font-size: 20px;
        margin-bottom: 15px;
    }

    .toolbar {
        flex-direction: column;
        gap: 8px;
    }

    .action-button {
        min-width: auto;
        width: 100%;
        font-size: 13px;
    }

    .table-container {
        height: 45vh;
        padding: 10px;
        border-radius: 8px;
    }

    .table-button {
        padding: 4px 8px;
        font-size: 11px;
        margin-right: 5px;
    }

    .empty-tip {
        padding: 30px 10px;
        border-radius: 8px;
    }

    .empty-icon {
        font-size: 50px;
        margin-bottom: 10px;
    }

    .empty-text {
        font-size: 14px;
    }

    .button-icon {
        margin-right: 4px;
        font-size: 14px;
    }
}

</style>
