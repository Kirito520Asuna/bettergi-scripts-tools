<script setup>
import {onMounted, onUnmounted, reactive, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {updateUserInfo} from "@api/auth/login.js";
import {getTokenInfo, updateToken} from "@api/auth/token.js";
import {getAllSystemInfo, removeLocalToken, restart, toHomePage} from "@api/web/web.js";
import {backup, recovery} from "@api/data/BackupRecover.js";

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
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改用户信息失败:', error);
      ElMessage.error('修改用户信息失败: ' + (error.message || '未知错误'));
    }
  }
}

// 重置用户信息表单
const resetUserInfoForm = () => {
  info.user.username = '';
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
})
const backupFileInput = ref(null)
// 数据备份
const handleBackup = async () => {
  try {
    await ElMessageBox.confirm('确定要备份数据吗？系统将生成 JSON 格式备份文件并下载', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    });

    dataBackupRecover.isBackingUp = true;

    const blob = await backup();

    if (blob && blob.size > 0) {
      const fileName = `bgi-tools_backup_${new Date().getTime()}.json`;

      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(link.href);

      ElMessage.success('数据备份成功，文件已下载');
    } else {
      throw new Error('备份失败：返回数据为空');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('数据备份失败:', error);
      ElMessage.error('数据备份失败：' + (error.message || '未知错误'));
    }
  } finally {
    dataBackupRecover.isBackingUp = false;
  }
}


// 触发文件选择
const triggerFileSelect = () => {
  if (backupFileInput.value) {
    backupFileInput.value.click();
  }
}
// 处理拖拽文件
const handleDrop = (event) => {
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
    ElMessage.success('数据恢复成功');
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
};

// 组件挂载时加载Token信息
onMounted(async () => {
  await loadTokenInfo();
  await loadSystemInfo();
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


          <!-- 用户信息修改 -->
          <div class="setting-card">
            <div class="card-header">
              <h3 class="card-title">用户账号设置</h3>
              <div class="card-icon">👤</div>
            </div>
            <div class="card-content">
              <el-form :model="info.user" :rules="userInfoRules" label-width="120px">
                <el-form-item label="新用户名" prop="username">
                  <el-input
                      v-model="info.user.username"
                      placeholder="请输入新用户名"
                      clearable
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="password">
                  <el-input
                      v-model="info.user.password"
                      type="password"
                      placeholder="请输入新密码"
                      show-password
                      clearable
                  />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                      v-model="info.user.confirmPassword"
                      type="password"
                      placeholder="请再次输入密码"
                      show-password
                      clearable
                  />
                </el-form-item>
                <!--                <el-form-item class="submit">
                                  <el-button type="primary" @click="handleUpdateUserInfo">
                                    修改用户信息
                                  </el-button>
                                  <el-button @click="resetUserInfoForm">重置</el-button>
                                </el-form-item>-->
              </el-form>

              <div class="submit">
                <el-button type="primary" @click="handleUpdateUserInfo">
                  修改用户信息
                </el-button>
                <el-button @click="resetUserInfoForm">重置</el-button>
              </div>
            </div>
          </div>

          <!-- Token信息修改 -->
          <div class="setting-card">
            <div class="card-header">
              <h3 class="card-title">授权Token设置</h3>
              <div class="card-icon">🔑</div>
            </div>
            <div class="card-content">
              <el-form :model="info.token" :rules="tokenRules" label-width="120px">
                <el-form-item label="Token名称" prop="tokenName">
                  <el-input
                      v-model="info.token.tokenName"
                      placeholder="请输入Token名称"
                      clearable
                  />
                </el-form-item>
                <el-form-item label="Token值" prop="tokenValue">
                  <el-input
                      v-model="info.token.tokenValue"
                      type="password"
                      placeholder="请输入Token值"
                      show-password
                      clearable
                  />
                </el-form-item>
                <!--                <el-form-item>
                                  <el-button type="primary" @click="handleUpdateToken">
                                    修改Token信息
                                  </el-button>
                                  <el-button @click="loadTokenInfo">刷新</el-button>
                                </el-form-item>-->
              </el-form>

              <div class="submit">
                <el-button type="primary" @click="handleUpdateToken">
                  修改Token信息
                </el-button>
                <el-button @click="loadTokenInfo">刷新</el-button>
              </div>
            </div>

          </div>

          <!-- 数据备份与恢复 -->
          <div class="setting-card system-info-card">
            <div class="card-header">
              <h3 class="card-title">数据备份与恢复</h3>
              <div class="card-icon">💾</div>
            </div>
            <div class="card-content">
              <div class="backup-input-container">
                <div class="backup-tips">
                  <p style="color: red;"><strong>提示：</strong></p>
                  <ul>
                    <li>备份数据将生成 JSON 文件并自动下载</li>
                    <li>恢复数据前请确保已保存重要数据</li>
                    <li>恢复操作不可逆，请谨慎操作</li>
                  </ul>
                </div>

                <input
                    ref="backupFileInput"
                    type="file"
                    accept=".json"
                    style="display: none;"
                    @change="handleFileChange"
                />
                <div
                    class="file-drop-zone"
                    @click="triggerFileSelect"
                    @dragover.prevent
                    @drop.prevent="handleDrop"
                >
                  <div class="drop-zone-content">
                    <div class="drop-icon">📁</div>
                    <p class="drop-text">点击选择文件或拖拽到此处</p>
                    <p class="drop-hint">仅支持 .json 格式的备份文件</p>
                    <!--            提示写在这      -->
                  </div>
                </div>
              </div>
              <div class="backup-recover-actions">
                <el-button
                    type="success"
                    @click="handleBackup"
                    :loading="dataBackupRecover.isBackingUp"
                    class="action-button"
                >
                  {{ dataBackupRecover.isBackingUp ? '备份中...' : '⬇️备份数据' }}
                </el-button>

                <el-button
                    type="warning"
                    @click="triggerFileSelect"
                    :loading="dataBackupRecover.isRecovering"
                    class="action-button"
                >
                  {{ dataBackupRecover.isRecovering ? '恢复中...' : '⬆️恢复数据' }}
                </el-button>
              </div>
            </div>
          </div>


        </div>
      </div>
    </div>

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
    <!-- 在 template 最后添加 -->
    <div class="fixed-footer">
      <button @click="goToHome" class="btn secondary">🏠 返回主页</button>
    </div>
  </div>
</template>

<style scoped>
@import "@css/settings.css";
</style>