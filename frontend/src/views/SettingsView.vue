<script setup>
import {onMounted, reactive, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {updateUserInfo} from "@api/auth/login.js";
import {getTokenInfo, updateToken} from "@api/auth/token.js";
import {removeLocalToken, restart, toHomePage} from "@api/web/web.js";
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
// 在 script 中添加跳转逻辑
const goToHome = async () => {
  // router.push('/'); // 假设主页路径是 '/'
  await toHomePage()
};

// 组件挂载时加载Token信息
onMounted(async () => {
  await loadTokenInfo();
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
          <div class="setting-card">
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

.settings {
  padding: 30px;
  min-width: 1200px;
  margin: 0 auto;
}

.settings-container {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 15px 35px rgba(102, 126, 234, 0.3);
  backdrop-filter: blur(10px);
}

.settings-title {
  text-align: center;
  color: white;
  margin-bottom: 40px;
  font-size: 32px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}


.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30px;
  margin-top: 20px;
}

.setting-card {
  max-width: 500px;
  background: white;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.setting-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

.card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.card-icon {
  font-size: 24px;
}

.card-content {
  padding: 25px;
}
.submit {
  display: flex;
  justify-content: center;
  padding-top: 24px;
  margin-top: auto;
/*  border-top: 1px solid var(--el-border-color-light);*/
}

.submit .el-button {
  min-width: 120px;
}
@media (max-width: 768px) {
  .settings-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}


.setting-section {
  background: white;
  border-radius: 15px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.setting-section:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

.setting-section:last-child {
  margin-bottom: 0;
}

.section-title {
  color: #2c3e50;
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 3px solid #667eea;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -3px;
  left: 0;
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 2px;
}

.form-container {
  padding: 25px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.form-container .el-form-item {
  margin-bottom: 25px;
}

.form-container .el-form-item:last-child {
  margin-bottom: 0;
  padding-top: 15px;
  border-top: 1px dashed #dee2e6;
}

.form-container .el-button {
  margin-right: 15px;
  border-radius: 8px;
  padding: 12px 24px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.form-container .el-button:first-child {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.form-container .el-button:first-child:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

/* 输入框美化 */
:deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
  border-color: #667eea;
}

/* 在样式部分添加 */
.fixed-bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  /*background: linear-gradient(90deg, #ff6b6b, #ee5a52);*/
  padding: 20px;
  text-align: center;
  box-shadow: 0 -5px 15px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  backdrop-filter: blur(10px);
}

.restart-button {
  width: 100%;
  max-width: 300px;
  height: 50px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 25px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(238, 90, 82, 0.4);
  transition: all 0.3s ease;
}

.restart-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(238, 90, 82, 0.6);
}

.restart-button:active {
  transform: translateY(0);
}

/* 为页面主体添加底部边距，避免被固定按钮遮挡 */
.settings-container {
  margin-bottom: 90px;
}

.action-button {
  flex: 1;
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.2);
}

.file-drop-zone {
  background: #e8e8e8;
  margin-top: 20px;
  padding: 40px 20px;
  border: 2px dashed #667eea;
  border-radius: 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.file-drop-zone:hover {
  border-color: #764ba2;
  background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
  transform: scale(1.02);
}

.file-drop-zone.drag-over {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  transform: scale(1.03);
}
.backup-recover-actions{
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.backup-input-container {
  display: grid;
  grid-template-columns: 4fr 5fr;
  gap: 12px;
  align-items: center;
  box-sizing: border-box;
  padding: 8px 3px;
  /*height: 200px;*/
/*  padding: 40px 20px;*/
}
.backup-input-container > input,
.backup-input-container > div {
  min-width: 0;
  max-width: 100%;
}
.drop-zone-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 80px;
}

.drop-icon {
  font-size: 48px;
  margin-bottom: 15px;
  opacity: 0.7;
}

.drop-text {
  font-size: 16px;
  color: #2c3e50;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.drop-hint {
  font-size: 13px;
  color: #7f8c8d;
  margin: 0;
}

.backup-tips {
  background: #e8e8e8;
  padding: 2px 2px;
  border-radius: 10px;
  border-left: 4px solid #667eea;
}

.backup-tips p {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-weight: 600;
}

.backup-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #555;
  font-size: 14px;
  line-height: 1.8;
}

.backup-tips li {
  margin-bottom: 5px;
}


/* 响应式设计 */
@media (max-width: 768px) {
  .settings {
    padding: 15px;
    min-width: 768px;
  }

  .settings-container {
    padding: 25px;
    border-radius: 15px;
  }

  .settings-title {
    font-size: 26px;
    margin-bottom: 30px;
  }

  .setting-section {
    padding: 20px;
    margin-bottom: 20px;
  }

  .section-title {
    font-size: 18px;
    margin-bottom: 20px;
  }

  .form-container {
    padding: 20px;
  }

  .form-container .el-button {
    width: 100%;
    margin-right: 0;
    margin-bottom: 10px;
  }

  .form-container .el-button:last-child {
    margin-bottom: 0;
  }
}

@media (max-width: 480px) {
  .settings {
    min-width: 480px;
  }

  .settings-container {
    padding: 20px;
  }

  .settings-title {
    font-size: 22px;
  }

  .setting-section {
    padding: 15px;
  }

  .form-container {
    padding: 15px;
  }
}

</style>
