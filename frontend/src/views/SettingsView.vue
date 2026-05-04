<script setup>
import {onMounted, onUnmounted, reactive, ref, watch} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {getCurrentUserName, updateUserInfo} from "@api/auth/login.js";
import {getTokenInfo, updateToken} from "@api/auth/token.js";
import {getAllSystemInfo, goBack, removeLocalToken, restart, toHomePage} from "@api/web/web.js";
import {
  backup,
  backupDownload, deleteBatchBackup, deleteBatchBackupLocal,
  downloadBackupById,
  getLocalBackupList,
  getRemoteBackupPage,
  recovery
} from "@api/data/BackupRecover.js";
const dialogVisible = reactive(
    {
      upload:false,
      user: false,
      token: false,
      backup: false,
    }
)
const RestartClick = ref(false)
const info = reactive({
  update: {
    user: false
  },
  // 用户信息表单
  user: {
    username: '',
    password: '',
    confirmPassword: ''
  },
// Token信息表单
  token: {
    tokenName: '',
    tokenValue: ''
  }
})
// 表单验证规则
const userInfoRules = {
  username: [
    {required: true, message: '请输入用户名', trigger: 'blur'},
    {min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur'}
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 6, message: '密码长度至少6位', trigger: 'blur'}
  ],
  confirmPassword: [
    {required: true, message: '请确认密码', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        if (value !== info.user.password) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
}

const tokenRules = {
  tokenName: [
    {required: true, message: '请输入Token名称', trigger: 'blur'}
  ],
  tokenValue: [
    {required: true, message: '请输入Token值', trigger: 'blur'}
  ]
}

// 修改用户信息
const handleUpdateUserInfo = async () => {
  if (info.user.password !== info.user.confirmPassword) {
    ElMessage.error('两次输入的密码不一致');
    return;
  }

  try {
    await ElMessageBox.confirm('确定要修改用户信息吗？修改后需要重启服务才能生效', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const response = await updateUserInfo(info.user.username, info.user.password);
    if (response.code === 200) {
      info.update.user = true;
      ElMessage.success('用户信息修改成功，重启服务后生效');
      // 清空表单
      info.user.username = '';
      info.user.password = '';
      info.user.confirmPassword = '';
      dialogVisible.user = false;
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改用户信息失败:', error);
      ElMessage.error('修改用户信息失败: ' + (error.message || '未知错误'));
    }
  }
}

// 重置用户信息表单
const resetUserInfoForm = async () => {
  info.user.username = await getCurrentUserName();
  info.user.password = '';
  info.user.confirmPassword = '';
}


// 加载Token信息
const loadTokenInfo = async () => {
  try {
    const response = await getTokenInfo();
    if (response.code === 200) {
      info.token.tokenName = response.data.name || '';
      info.token.tokenValue = response.data.value || '';
    }
  } catch (error) {
    console.error('获取Token信息失败:', error);
    ElMessage.error('获取Token信息失败');
  }
}
// 修改Token信息
const handleUpdateToken = async () => {
  try {
    await ElMessageBox.confirm('确定要修改授权Token吗？修改后需要重启服务才能生效', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const response = await updateToken(info.token.tokenName, info.token.tokenValue);
    if (response.code === 200) {
      ElMessage.success('Token信息修改成功，重启服务后生效');
      dialogVisible.token = false;
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改Token信息失败:', error);
      ElMessage.error('修改Token信息失败: ' + (error.message || '未知错误'));
    }
  }
}

// 在 script setup 部分添加重启功能
const handleRestart = async () => {
  await restart(RestartClick)
  if (info.update.user) {
    await removeLocalToken()
    await toHomePage(false)
  }
};
// 数据备份与恢复相关
const dataBackupRecover = reactive({
  // backupFileInput: null,
  isBackingUp: false,
  isRecovering: false,
  isDragging: false,
  backupTab: 'local',
  selectedBackups: [],
  selectedLocalBackups: [],
  page: {
    pageNumber: 1,
    pageSize: 10,
    total: 0
  },
  loadingList: {
    remote: false,
    local: false,
    remoteBackups: [],
    localBackups: []
  }
})

// 加载远程备份列表
const loadRemoteBackups = async () => {
  dataBackupRecover.loadingList.remote = true
  try {
    const res = await getRemoteBackupPage(dataBackupRecover.page.pageNumber, dataBackupRecover.page.pageSize)
    if (res) {
      dataBackupRecover.loadingList.remoteBackups = res?.list || []
      dataBackupRecover.page.total = res?.total || 0
    } else {
      ElMessage.error('获取远程备份列表失败')
    }
  } catch (e) {
    ElMessage.error('获取远程备份列表失败')
    console.error(e)
  } finally {
    dataBackupRecover.loadingList.remote = false
  }
}

// 加载本地备份列表
const loadLocalBackups = async () => {
  dataBackupRecover.loadingList.local = true
  try {
    const list = await getLocalBackupList()
    dataBackupRecover.loadingList.localBackups = list || []
  } catch (e) {
    ElMessage.error('获取本地备份列表失败')
    console.error(e)
  } finally {
    dataBackupRecover.loadingList.local = false
  }
}

// 监听标签切换，自动加载对应数据
// watch(dataBackupRecover.backupTab, (val) => {
//   tabClick( val)
// })
const loadBackups = () =>{
  loadRemoteBackups()
  loadLocalBackups()
}
// 弹窗打开时加载当前标签数据（也可在 @opened 事件中调用）
const onBackupDialogOpen = () => {
  tabClick(dataBackupRecover.backupTab)
}
// 监听标签切换，自动加载对应数据
const handleTabClick = (tab) => {
  tabClick(tab.props.name)
}
const tabClick = (name) => {
  if (name === 'remote') loadRemoteBackups()
  else loadLocalBackups()
}
// 下载指定备份文件
const handleDownloadBackup = async (backup) => {
  try {
    const blob = await downloadBackupById(backup.id)
    if (blob && blob.size > 0) {
      const fileName = backup?.backupName || `backup_${backup.id}.json`
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(link.href)
      ElMessage.success('下载成功')
    }
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

// 从远程或本地备份恢复
const handleRecoveryFromBackup = async (backup, isLocal) => {
  try {
    await ElMessageBox.confirm(
        `确定要恢复备份 "${backup?.backupName || backup.name}" 吗？当前数据将被覆盖！`,
        '警告',
        {type: 'warning'}
    )
    dataBackupRecover.isRecovering = true
    await recovery(null, isLocal, backup.name, backup.id)
    ElMessage.success('恢复成功，如涉及配置需重启后生效')
    if (isLocal) await loadLocalBackups()
    else await loadRemoteBackups()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('恢复失败：' + (error.message || '未知错误'))
    }
  } finally {
    dataBackupRecover.isRecovering = false
  }
}


const handleDeleteBackup = async (backup, isLocal) => {

  await ElMessageBox.confirm(
      `确定要删除${isLocal?'本地':'远程'}备份 "${backup?.backupName}" 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  )

  try {

    if (isLocal) {
      await deleteBatchBackupLocal([backup.backupPath])
      ElMessage.success('删除成功')
      return
    }
    await deleteBatchBackup([backup.id])
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  }finally {
   onBackupDialogOpen()
  }
}
const handleSelectionChange = (selection) => {
  dataBackupRecover.selectedBackups = selection
}
const handleBatchDelete = async () => {

  if (dataBackupRecover.selectedBackups.length === 0) {
    ElMessage.warning('请先选择要删除的备份')
    return
  }

  const isLocal = dataBackupRecover.backupTab === 'local'
  const backups = isLocal
      ? dataBackupRecover.loadingList.localBackups
      : dataBackupRecover.loadingList.remoteBackups

  if (backups.length === 0) {
    ElMessage.warning('暂无备份可删除')
    return
  }

  try {
    await ElMessageBox.confirm(
        `确定要删除选中的 ${dataBackupRecover.selectedBackups.length} 个${isLocal ? '本地' : '远程'}备份吗？此操作不可恢复！`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
    const ids = dataBackupRecover.selectedBackups.map(b => b.id)
    // const ids = backups.map(b => b.id).filter(id => id)
    if (ids.length === 0) {
      ElMessage.warning('没有可删除的远程备份')
      return
    }

    await deleteBatchBackup(ids)
    ElMessage.success(`成功删除 ${ids.length} 个备份`)
    // await loadRemoteBackups()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败：' + (error.message || '未知错误'))
    }
  }finally {
    onBackupDialogOpen()
  }
}

const handleLocalSelectionChange = (selection) => {
  dataBackupRecover.selectedLocalBackups = selection
}

const handleBatchDeleteLocal = async () => {
  if (dataBackupRecover.selectedLocalBackups.length === 0) {
    ElMessage.warning('请先选择要删除的备份')
    return
  }

  try {
    await ElMessageBox.confirm(
        `确定要删除选中的 ${dataBackupRecover.selectedLocalBackups.length} 个本地备份吗？此操作不可恢复！`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    const paths = dataBackupRecover.selectedLocalBackups.map(b => b.backupPath)
    await deleteBatchBackupLocal(paths)
    ElMessage.success(`成功删除 ${paths.length} 个本地备份`)
    await loadLocalBackups()
    dataBackupRecover.selectedLocalBackups = []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败：' + (error.message || '未知错误'))
    }
  }
}


const backupFileInput = ref(null)
// 数据备份
const handleBackup = async () => {
  try {
    // await ElMessageBox.confirm('确定要备份数据吗？系统将生成 JSON 格式备份文件并下载', '提示', {
    //   confirmButtonText: '确定',
    //   cancelButtonText: '取消',
    //   type: 'info'
    // });
    await ElMessageBox.confirm('确定要备份数据吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    });
    dataBackupRecover.isBackingUp = true;
    await backup()
    // const blob = await backupDownload();
    //
    // if (blob && blob.size > 0) {
    //   const fileName = `bgi-tools_backup_${new Date().getTime()}.json`;
    //
    //   const link = document.createElement('a');
    //   link.href = URL.createObjectURL(blob);
    //   link.download = fileName;
    //   document.body.appendChild(link);
    //   link.click();
    //   document.body.removeChild(link);
    //   URL.revokeObjectURL(link.href);
    //
    //   ElMessage.success('数据备份成功，文件已下载');
    // } else {
    //   throw new Error('备份失败：返回数据为空');
    // }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('数据备份失败:', error);
      ElMessage.error('数据备份失败：' + (error.message || '未知错误'));
    }
  } finally {
    dataBackupRecover.isBackingUp = false;
    onBackupDialogOpen();
  }
}


// 触发文件选择
// const triggerFileSelect = () => {
//   if (backupFileInput.value) {
//     backupFileInput.value.click();
//   }
// }
const triggerFileSelect = () => {
  dialogVisible.upload = true
}
// 处理拖拽文件
const handleDrop = (event) => {
  event.preventDefault()

  //提示写这 弹窗提示
  const files = event.dataTransfer.files;
  if (files && files.length > 0) {
    const file = files[0];
    if (file.name.endsWith('.json')) {
      handleFileChange({target: {files: [file]}});
    } else {
      ElMessage.error('请选择 .json 格式的备份文件');
    }
  }
  dataBackupRecover.isDragging = false
  dialogVisible.upload =false
}

const handleDragOver = (event) => {
  event.preventDefault()
  dataBackupRecover.isDragging = true
}

const handleDragLeave = () => {
  dataBackupRecover.isDragging = false
}

const handleFileInputChange = (event) => {
  const file = event.target.files[0];
  if (file) {
    handleRecoveryFile(file);
  }
}

const handleRecoveryFile = async (file) => {
  try {
    await ElMessageBox.confirm(`确定要从备份文件 "${file.name}" 恢复数据吗？此操作将覆盖当前数据，请谨慎操作！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    dialogVisible.upload = false
    dataBackupRecover.isRecovering = true;
    await recovery(file);
    ElMessage.success('数据恢复成功，如存在配置恢复需要重启系统后生效');
    onBackupDialogOpen();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('数据恢复失败:', error);
      ElMessage.error('数据恢复失败：' + (error.message || '未知错误'));
    }
  } finally {
    dataBackupRecover.isRecovering = false;
  }
}

// 处理文件选择和恢复
const handleFileChange = async (event) => {
  const file = event.target.files[0];
  if (!file) {
    return;
  }

  try {
    await ElMessageBox.confirm(`确定要从备份文件 "${file.name}" 恢复数据吗？此操作将覆盖当前数据，请谨慎操作！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    dataBackupRecover.isRecovering = true;
    await recovery(file);
    ElMessage.success('数据恢复成功,如存在配置恢复需要重启系统后生效');
  } catch (error) {
    if (error !== 'cancel') {
      console.error('数据恢复失败:', error);
      ElMessage.error('数据恢复失败：' + (error.message || '未知错误'));
    }
  } finally {
    dataBackupRecover.isRecovering = false;
    event.target.value = '';
  }
}

let refreshTimer = null
const systemInfo = ref(null)
const autoRefresh = ref(false)
const systemInfoList = ref([])
const activeHostIndex = ref(0)

const loadSystemInfo = async () => {
  try {
    // const response = await getSystemInfo();
    const response = await getAllSystemInfo()
    systemInfoList.value = response
    systemInfo.value = systemInfoList.value[activeHostIndex.value];
  } catch (error) {
    console.error('获取系统信息失败:', error);
  }
}

const switchHost = (index) => {
  activeHostIndex.value = index;
  systemInfo.value = systemInfoList.value[index];
}

const toggleAutoRefresh = () => {
  if (autoRefresh.value) {
    loadSystemInfo()
    refreshTimer = setInterval(() => {
      loadSystemInfo()
    }, 5000)
    ElMessage.success('已开启自动刷新')
  } else {
    if (refreshTimer) {
      clearInterval(refreshTimer)
      refreshTimer = null
    }
    ElMessage.info('已关闭自动刷新')
  }
}

const formatUptime = (seconds) => {
  if (!seconds) return '0 秒'
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60

  let result = ''
  if (days > 0) result += `${days}天 `
  if (hours > 0) result += `${hours}小时 `
  if (minutes > 0) result += `${minutes}分 `
  result += `${secs}秒`

  return result
}

const getMemoryColor = (value) => {
  if (value < 60) return '#67c23a'
  if (value < 80) return '#e6a23c'
  return '#f56c6c'
}


// 在 script 中添加跳转逻辑
const goToHome = async () => {
  // router.push('/'); // 假设主页路径是 '/'
  await toHomePage()
}
const goToBack = async () => {
  await goBack();
}
// 组件挂载时加载Token信息
onMounted(async () => {
  await loadTokenInfo();
  await loadSystemInfo();
  info.user.username = await getCurrentUserName()
})
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<template>
  <div class="home">
    <div v-if="RestartClick" class="restart-overlay" @keydown.esc.prevent tabindex="0">
      <div class="restart-modal">
        <div class="warning-header">
          <span class="warning-icon">!</span>
          <h3>系统正在重启</h3>
        </div>

        <div class="spinner"></div>

        <div class="loading-text">正在执行重启</div>

        <p class="hint">
          请勿关闭界面或刷新<br>
          预计需要 1–5 分钟，完成后将自动跳转
        </p>
      </div>
    </div>


    <div class="settings">
      <div class="settings-container">
        <h2 class="settings-title">系统设置</h2>
        <div class="settings-grid">

          <!-- 系统信息卡片 -->
          <div class="setting-card system-info-card">
            <div class="card-header">
              <h3 class="card-title">系统信息</h3>
              <div class="card-actions">
                <el-button
                    type="success"
                    size="small"
                    @click="loadSystemInfo"
                    :loading="!systemInfo"
                    class="refresh-button"
                    round
                >
                  <span class="button-icon" :class="{ 'rotating': !systemInfo }">↻</span>
                  <span class="button-text">刷新</span>
                </el-button>
                <el-switch
                    v-model="autoRefresh"
                    @change="toggleAutoRefresh"
                    active-text="自动刷新"
                    style="margin-right: 12px; color: #ca8a04"
                />
              </div>
              <div class="card-icon">💻</div>
            </div>
            <div class="card-content">
              <div v-if="systemInfoList.length > 0" class="host-tabs">
                <div
                    v-for="(info, index) in systemInfoList"
                    :key="index"
                    class="host-tab"
                    :class="{ 'active': activeHostIndex === index }"
                    @click="switchHost(index)"
                >
                  <span class="host-tab-icon">🖥️</span>
                  <span class="host-tab-name">{{ info.hostName || `主机 ${index + 1}` }}</span>
                </div>
              </div>
              <div v-if="systemInfo" class="system-info-content">
                <div class="info-row">
                  <span class="info-label">主机名:</span>
                  <span class="info-value">{{ systemInfo.hostName }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">IP 地址:</span>
                  <span class="info-value">{{ systemInfo.ipAddress }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">操作系统:</span>
                  <span class="info-value">{{ systemInfo.osName }} {{ systemInfo.osVersion }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">系统架构:</span>
                  <span class="info-value">{{ systemInfo.osArch }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">JVM:</span>
                  <span class="info-value">{{ systemInfo.jvmName }} {{ systemInfo.jvmVersion }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">启动时间:</span>
                  <span class="info-value">{{ systemInfo.jvmStartTime }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">运行时长:</span>
                  <span class="info-value uptime-value">{{ formatUptime(systemInfo.jvmUptimeSeconds) }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">CPU 核心:</span>
                  <span class="info-value">{{ systemInfo.cpuCores }} 核</span>
                </div>
                <div class="info-row">
                  <span class="info-label">内存使用:</span>
                  <span class="info-value memory-usage">
                    {{ systemInfo.heapUsedMB }} MB / {{ systemInfo.heapMaxMB }} MB
                  </span>
                </div>
                <el-progress
                    :percentage="Math.round((systemInfo.heapUsedMB / systemInfo.heapMaxMB) * 100)"
                    :color="getMemoryColor"
                    :stroke-width="8"
                    :show-text="false"
                />
              </div>
              <div v-else class="loading-system-info">
                <div class="spinner-small"></div>
                <p>加载中...</p>
              </div>
            </div>
          </div>


          <!-- 用户账号设置（摘要卡片，点击弹窗） -->
          <div class="setting-card" @click="dialogVisible.user = true">
            <div class="card-header">
              <h3 class="card-title">用户账号设置</h3>
              <div class="card-icon">👤</div>
            </div>
            <div class="card-summary">
              <span>当前用户：{{ info.user.username || '未设置' }}</span>
              <span class="edit-hint">点击修改</span>
            </div>
          </div>

          <!-- Token设置（摘要卡片，点击弹窗） -->
          <div class="setting-card" @click="dialogVisible.token = true">
            <div class="card-header">
              <h3 class="card-title">授权Token设置</h3>
              <div class="card-icon">🔑</div>
            </div>
            <div class="card-summary">
              <span>令牌名称：{{ info.token.tokenName || '未设置' }}</span>
              <span class="edit-hint">点击修改</span>
            </div>
          </div>

          <!-- 数据备份与恢复（摘要卡片，点击弹窗） -->
          <div class="setting-card" @click="dialogVisible.backup = true">
            <div class="card-header">
              <h3 class="card-title">数据备份与恢复</h3>
              <div class="card-icon">💾</div>
            </div>
            <div class="card-summary">
              <span>管理数据备份与恢复</span>
              <span class="edit-hint">点击操作</span>
            </div>
          </div>


        </div>
      </div>
    </div>
    <!-- ==================== 弹窗区 ==================== -->

    <!-- 用户信息修改弹窗 -->
    <el-dialog
        v-model="dialogVisible.user"
        title="修改用户信息"
        width="480px"
        :close-on-click-modal="false"
        @closed="resetUserInfoForm"
    >
      <el-form :model="info.user" :rules="userInfoRules" label-width="100px">
        <el-form-item label="新用户名" prop="username">
          <el-input v-model="info.user.username" placeholder="请输入新用户名" clearable />
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="info.user.password" type="password" placeholder="请输入新密码" show-password clearable />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="info.user.confirmPassword" type="password" placeholder="请再次输入密码" show-password clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible.user = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateUserInfo">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- Token修改弹窗 -->
    <el-dialog
        v-model="dialogVisible.token"
        title="修改Token"
        width="480px"
        :close-on-click-modal="false"
        @closed="loadTokenInfo"
    >
      <el-form :model="info.token" :rules="tokenRules" label-width="100px">
        <el-form-item label="Token名称" prop="tokenName">
          <el-input v-model="info.token.tokenName" placeholder="请输入Token名称" clearable />
        </el-form-item>
        <el-form-item label="Token值" prop="tokenValue">
          <el-input v-model="info.token.tokenValue" type="password" placeholder="请输入Token值" show-password clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible.token = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateToken">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 备份恢复弹窗 -->
    <el-dialog
        v-model="dialogVisible.backup"
        title="数据备份与恢复"
        width="80%"
        :close-on-click-modal="false"
        @opened="onBackupDialogOpen"
    >
      <div class="backup-content">
        <el-alert
            title="提示：备份可下载为 JSON 文件；恢复会覆盖当前数据，请谨慎操作"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 16px"
        />

        <el-tabs class="backup-list" v-model="dataBackupRecover.backupTab" type="border-card" @tab-click="handleTabClick">
          <!-- 远程备份 -->
          <el-tab-pane label="远程备份" name="remote" class="remote-backup-pane">
            <div style="margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center;">
              <span style="color: #606266;">共 {{ dataBackupRecover.page.total }} 条记录</span>
              <div>
                <el-button size="small" type="danger" @click="handleBatchDelete" :disabled="dataBackupRecover.loadingList.remoteBackups.length === 0">
                  批量删除
                </el-button>
                <el-button size="small" @click="loadRemoteBackups" :loading="dataBackupRecover.loadingList.remote">
                  刷新列表
                </el-button>
              </div>
            </div>
            <div class="table-wrapper">
              <el-table
                  :data="dataBackupRecover.loadingList.remoteBackups"
                  v-loading="dataBackupRecover.loadingList.remote"
                  stripe border
                  @selection-change="handleSelectionChange"
              >
                <el-table-column type="selection" width="55"/>
                <el-table-column prop="id" label="ID" width="180"/>
                <el-table-column prop="backupName" label="备份名称" show-overflow-tooltip/>
                <el-table-column prop="backupSize" label="备份大小(字节)" width="180"/>
                <el-table-column prop="backupTime" label="备份时间" width="180">
                  <template #default="{ row }">
                    {{ row.backupTime ? row.backupTime.replace('T', ' ').substring(0, 19) : '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="220" align="center">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleDownloadBackup(row)" size="small">下载</el-button>
                    <el-button link type="warning" @click="handleRecoveryFromBackup(row, false)" size="small"
                               :loading="dataBackupRecover.isRecovering">恢复
                    </el-button>
                    <el-button link type="danger" @click="handleDeleteBackup(row, false)" size="small">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div class="backup-page">
              <el-pagination
                  v-model:current-page="dataBackupRecover.page.pageNumber"
                  v-model:page-size="dataBackupRecover.page.pageSize"
                  :total="dataBackupRecover.page.total"
                  :page-sizes="[5, 10, 20]"
                  layout="total, sizes, prev, pager, next"
                  @current-change="loadRemoteBackups"
                  @size-change="loadRemoteBackups"
                  small
              />
            </div>
          </el-tab-pane>

          <!-- 本地备份 -->
          <el-tab-pane label="本地备份" name="local" class="local-backup-pane">
            <div style="margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center;">
              <span style="color: #606266;">
                本地备份文件
                <span v-if="dataBackupRecover.selectedLocalBackups.length > 0" style="color: #f56c6c; margin-left: 10px;">
                  已选中 {{ dataBackupRecover.selectedLocalBackups.length }} 项
                </span>
              </span>
              <div>
                <el-button size="small" type="danger" @click="handleBatchDeleteLocal" :disabled="dataBackupRecover.selectedLocalBackups.length === 0">
                  批量删除
                </el-button>
                <el-button size="small" @click="loadLocalBackups" :loading="dataBackupRecover.loadingList.local">
                  刷新列表
                </el-button>
              </div>
            </div>
            <div class="table-wrapper">
              <el-table
                  :data="dataBackupRecover.loadingList.localBackups"
                  v-loading="dataBackupRecover.loadingList.local"
                  stripe border
                  @selection-change="handleLocalSelectionChange"
              >
                <el-table-column type="selection" width="55"/>
                <el-table-column prop="backupName" label="备份名称" show-overflow-tooltip/>
                <el-table-column prop="backupSize" label="备份大小(字节)" width="180"/>
                <el-table-column prop="backupTime" label="备份时间" width="180">
                  <template #default="{ row }">
                    {{ row.backupTime ? row.backupTime.replace('T', ' ').substring(0, 19) : '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100" align="center">
                  <template #default="{ row }">
                    <el-button link type="warning" @click="handleRecoveryFromBackup(row, true)" size="small"
                               :loading="dataBackupRecover.isRecovering">恢复
                    </el-button>
                    <el-button link type="danger" @click="handleDeleteBackup(row, true)" size="small">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

        </el-tabs>
      </div>

      <!-- 文件上传弹窗 -->
      <el-dialog
          v-model="dialogVisible.upload"
          title="上传备份文件恢复"
          width="500px"
          :close-on-click-modal="false"
      >
        <input
            ref="backupFileInput"
            type="file"
            accept=".json"            style="display: none;"
            @change="handleFileInputChange"
        />

        <div
            class="upload-drop-zone"
            :class="{ 'is-dragging': dataBackupRecover.isDragging }"
            @click="backupFileInput?.click()"
            @dragover.prevent="handleDragOver"
            @dragleave="handleDragLeave"
            @drop.prevent="handleDrop"
        >
          <div class="drop-icon"></div>
          <p class="drop-text">点击选择文件或拖拽到此处</p>
          <p class="drop-hint">仅支持 .json 格式的备份文件</p>
        </div>

        <template #footer>
          <el-button @click="dialogVisible.upload = false">取消</el-button>
        </template>
      </el-dialog>

      <!-- 底部手动备份 & 本地上传 -->
      <div class="backup-bottom-actions">
        <el-button type="primary" @click="handleBackup" :loading="dataBackupRecover.isBackingUp" plain>
          <!--            {{ dataBackupRecover.isBackingUp ? '备份中...' : '⬇️ 手动备份并下载' }}-->
          {{ dataBackupRecover.isBackingUp ? '备份中...' : '⬇️ 手动备份' }}
        </el-button>

<!--        <input
            ref="backupFileInput"
            type="file"
            accept=".json"
            style="display: none;"
            @change="handleFileChange"
        />
        <el-button type="warning" @click="triggerFileSelect" :loading="dataBackupRecover.isRecovering" plain>
          {{ dataBackupRecover.isRecovering ? '恢复中...' : '📂 从本地上传恢复' }}
        </el-button>-->

        <el-button type="warning" @click="triggerFileSelect" :loading="dataBackupRecover.isRecovering" plain>
          {{ dataBackupRecover.isRecovering ? '恢复中...' : ' 从本地上传恢复' }}
        </el-button>
      </div>
    </el-dialog>

    <!-- 替换第234行的TODO注释 -->
    <div class="fixed-bottom-bar">
      <el-button
          type="danger"
          size="large"
          class="restart-button"
          @click="handleRestart"
      >
        重启系统
      </el-button>
    </div>
    <div class="fixed-back">
      <button @click="goToBack" class="btn secondary">返回上一页</button>
    </div>
    <!-- 在 template 最后添加 -->
    <div class="fixed-footer">
      <button @click="goToHome" class="btn secondary">🏠 返回主页</button>
    </div>
  </div>
</template>

<style scoped>
@import "@css/settings.css";

.card-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-top: 1px solid #ebeef5;
}
.edit-hint {
  color: #409eff;
  font-size: 14px;
}

.backup-bottom-actions {
  margin-top: auto;
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
}


.backup-content {
  height: 60vh;
  display: flex;
  flex-direction: column;
}

.backup-list {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}


.backup-list :deep(.el-tab-pane) {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.remote-backup-pane,
.local-backup-pane {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.table-wrapper {
  flex: 1;
  overflow: auto;
  margin-bottom: 10px;
}

.backup-page {
  flex-shrink: 0;
  display: flex;
  justify-content: flex-end;
  padding-top: 10px;
  padding-bottom: 50px;
  border-top: 1px solid #ebeef5;
}

.upload-drop-zone {
  border: 2px dashed #409eff;
  border-radius: 12px;
  padding: 50px 30px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-drop-zone:hover {
  border-color: #66b1ff;
  background: #ecf5ff;
}

.upload-drop-zone.is-dragging {
  border-color: #409eff;
  background: #e6f7ff;
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.drop-icon {
  font-size: 56px;
  margin-bottom: 20px;
  color: #e6a23c;
}

.drop-text {
  font-size: 18px;
  color: #303133;
  font-weight: 600;
  margin: 0 0 10px 0;
}

.drop-hint {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

</style>