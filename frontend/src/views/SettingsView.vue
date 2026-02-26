<script setup>
import {onMounted, reactive, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {updateUserInfo} from "@api/auth/login.js";
import {getTokenInfo, updateToken} from "@api/auth/token.js";

const RestartClick = ref(false)
const info = reactive({
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
    await ElMessageBox.confirm('确定要修改授权Token吗？', '提示', {
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
          预计需要 10–60 秒，完成後將自動跳轉
        </p>
      </div>
    </div>


    <div class="settings">
      <div class="settings-container">
        <h2 class="settings-title">系统设置</h2>
        <div class="settings-list">
          <!-- 用户信息修改 -->
          <div class="setting-section">
            <h3 class="section-title">用户账号设置</h3>
            <div class="form-container">
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
                <el-form-item>
                  <el-button type="primary" @click="handleUpdateUserInfo">
                    修改用户信息
                  </el-button>
                  <el-button @click="resetUserInfoForm">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>

          <!-- Token信息修改 -->
          <div class="setting-section">
            <h3 class="section-title">授权Token设置</h3>
            <div class="form-container">
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
                <el-form-item>
                  <el-button type="primary" @click="handleUpdateToken">
                    修改Token信息
                  </el-button>
                  <el-button @click="loadTokenInfo">刷新</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.settings {
  padding: 30px;
  max-width: 1200px;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .settings {
    padding: 15px;
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
