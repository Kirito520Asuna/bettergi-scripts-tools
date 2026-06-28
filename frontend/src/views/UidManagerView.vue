<script setup>
import {onMounted, reactive, ref} from "vue"
import {ElMessage, ElMessageBox} from "element-plus"
import {getAllUid, saveUid, removeUidList, getUid} from "@api/uid/uid.js"
import {goBack, toHomePage} from "@api/web/web.js"
import router from "@router/router.js";
import { CopyDocument } from '@element-plus/icons-vue'
import {CopyToClipboard} from "@utils/local.js";
const currentRoute = ref(router.currentRoute)
// 表单数据
const formData = reactive({
  show: false,
  edit: false,
  uid: '',
  as: '',
  username: undefined,
  password: undefined,
  password1: undefined,
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
    Object.keys(passwordMap).forEach(key => delete passwordMap[key]);
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
  formData.username = undefined
  formData.password = undefined
  formData.password1 = undefined
  formData.show = true
}

// 打开编辑对话框
const handleEdit = (row) => {
  formData.edit = true
  formData.uid = row.uid
  formData.as = row.as
  formData.username = row.username
  formData.password = row.password
  formData.password1 = row.password1
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
    // console.log("formData:", JSON.stringify(formData))
    if (formData.password !== formData.password1) {
      ElMessage.error('密码不一致')
      return
    }
    const uidInfo = {
      uid: formData.uid,
      as: formData.as,
      username: formData.username,
      password: formData.password,
    }

    await saveUid(uidInfo)
    // ElMessage.success(`${action}成功`)
    formData.show = false
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
  await loadData()
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
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
  await loadData()
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
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
  await loadData()
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  multipleSelection.value.clear()
  selection.forEach(item => {
    multipleSelection.value.add(item.uid)
  })
}


// 密码显示状态
const passwordMap = reactive({})
const passwordLoading = reactive({})

// 获取密码（使用已有的 getUid）
const handleFetchPassword = async (row) => {
  //清空所有passwordMap
  Object.keys(passwordMap).forEach(key => delete passwordMap[key]);
  if (passwordMap[row.uid]) return
  if (passwordLoading[row.uid]) return
  passwordLoading[row.uid] = true
  try {
    const data = await getUid(row.uid)
    // getUid 返回的是整个 data 对象，里面包含 password
    if (data && data.password !== undefined) {
      passwordMap[row.uid] = data.password
    }
  } catch (error) {
    ElMessage.error('获取密码失败')
  } finally {
    passwordLoading[row.uid] = false
  }
}

// 复制密码
const copyPassword = async (uid) => {
  const pwd = passwordMap[uid]
  if (!pwd) {
    ElMessage.warning('请先获取密码')
    return
  }
  await CopyToClipboard(pwd)
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
        <h2 class="manager-title">{{ currentRoute.meta.title }}</h2>

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

        <div class="manager-context">
          <div class="table-container" v-if="tableData.length > 0">
            <el-table
                v-loading="loading"
                :data="tableData"
                @selection-change="handleSelectionChange"
                style="width: 100%"
            >
              <el-table-column type="selection"/>
              <el-table-column prop="uid" label="UID"/>
              <el-table-column prop="as" label="别称"/>
              <el-table-column prop="username" label="用户名"/>

              <el-table-column label="密码" width="200">
                <template #default="{ row }">
                  <div v-if="passwordMap[row?.uid]" class="password-cell">
                    <span class="password-text">{{ passwordMap[row?.uid] }}</span>
                    <el-button
                        type="success"
                        size="small"
                        :icon="CopyDocument"
                        circle
                        @click="copyPassword(row?.uid)"
                        title="复制密码"
                    />
                  </div>
                  <el-button
                      v-else
                      type="warning"
                      size="small"
                      :loading="passwordLoading[row?.uid]"
                      @click="handleFetchPassword(row)"
                  >
                    获取密码
                  </el-button>
                </template>
              </el-table-column>
              <el-table-column label="操作" fixed="right">
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
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
        v-model="formData.show"
        :title="formData.edit ? '编辑 UID 映射' : '新增 UID 映射'"
        style="max-width: 300px"
        :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="rules">
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

        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="formData.username"
              placeholder="请输入用户名"
              clearable
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
              v-model="formData.password"
              type="password"
              placeholder="请输入密码"
              show-password
              clearable
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="password1">
          <el-input
              v-model="formData.password1"
              type="password"
              placeholder="请再次输入密码"
              show-password
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
  width: 80vw;
  height: 100vh;
  margin: 0 auto;
}

.manager-container {
  padding: 20px;
}

.manager-context {
  text-align: center;
  margin-top: 20px; /*设置与上一个元素的间隔*/
  height: 58vh;
  background: #ffffff;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.manager-title {
  text-align: center;
  margin-bottom: 10px;
  font-size: 32px;
  color: transparent;
  background: linear-gradient(90deg, #ff6b6b, #ef006a);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;

  box-shadow: 0 15px 35px rgba(102, 126, 234, 0.3);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
}

.toolbar {
  display: flex;
  gap: 15px;
  justify-content: flex-start;
  align-items: center; /* 垂直居中 */

  padding: 20px;
  border-radius: 15px;

  background: white;
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
}

.table-button {
  margin-right: 8px;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 13px;
}

.empty-tip {
  text-align: center;
  /*  background: #da7c7c;*/
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
.password-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.password-text {
  font-family: monospace;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
  user-select: all;
}
@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}


@media (max-width: 768px) {
  .uid-manager {
    padding: 20px;
    width: 100vw;
    height: auto;
    min-height: 100vh;
  }

  .manager-container {
    padding: 15px;
  }

  .manager-context {
    height: auto;
    min-height: 50vh;
    margin-top: 15px;
    padding: 15px;
    border-radius: 12px;
  }

  .manager-title {
    font-size: 1.8rem;
    margin-bottom: 15px;
    padding: 20px;
    border-radius: 12px;
  }

  .toolbar {
    flex-wrap: wrap;
    gap: 12px;
    justify-content: center;
    padding: 15px;
    margin-bottom: 15px;
    border-radius: 10px;
  }

  .action-button {
    min-width: 110px;
    font-size: 14px;
    padding: 10px 16px;
  }

  .action-button:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  .table-container {
    padding: 15px;
  }

  .table-button {
    padding: 6px 10px;
    font-size: 12px;
    margin-right: 6px;
    border-radius: 5px;
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
    margin-bottom: 20px;
  }

  .button-icon {
    margin-right: 5px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .uid-manager {
    padding: 10px;
    width: 100vw;
  }

  .manager-container {
    padding: 10px;
  }

  .manager-context {
    margin-top: 10px;
    padding: 10px;
    border-radius: 8px;
    min-height: 45vh;
  }

  .manager-title {
    font-size: 1.4rem;
    margin-bottom: 10px;
    padding: 15px;
    border-radius: 10px;
  }

  .toolbar {
    flex-direction: column;
    gap: 10px;
    padding: 12px;
    margin-bottom: 12px;
    border-radius: 8px;
  }

  .action-button {
    min-width: auto;
    width: 100%;
    font-size: 13px;
    padding: 10px 14px;
  }

  .action-button:hover {
    transform: translateY(-1px);
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.12);
  }

  .table-container {
    padding: 10px;
  }

  .table-button {
    padding: 5px 8px;
    font-size: 11px;
    margin-right: 4px;
    border-radius: 4px;
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
    margin-bottom: 15px;
  }

  .button-icon {
    margin-right: 4px;
    font-size: 13px;
  }

  .button-text {
    letter-spacing: 0.5px;
  }
}
</style>
