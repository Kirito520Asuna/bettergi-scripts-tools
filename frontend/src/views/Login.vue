<template>
  <div class="login-container">
    <el-card class="login-card glass-card">
      <img class="logo" src="@assets/logo.svg" alt="Logo"/>
      <h2 class="login-title">登录 BetterGI 工具集</h2>
      <el-form :model="form" ref="formRef" @submit.prevent="handleLogin">
        <el-form-item label="账号">
          <el-input
              v-model="form.username"
              placeholder="请输入用户名/账号"
              class="glow-input"
          />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
              class="glow-input"
          />
        </el-form-item>
        <el-button
            type="primary"
            native-type="submit"
            class="login-button"
            :loading="isLoading"
            :disabled="isLoading"
        >
          {{ isLoading ? '登录中...' : '登录' }} <!-- 动态显示按钮文本 -->
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {login} from '@api/auth/login'
import {setLocalToken, toHomePage} from "@api/web/web.js";

const router = useRouter()
const form = ref({
  username: '',
  password: ''
})

// 新增登录状态变量
const isLoading = ref(false)
const handleLogin = async () => {
  isLoading.value = true // 开始登录时设置为 true
  try {
    const res = await login(form.value.username, form.value.password)
    const token = res
    if (!token){
      throw new Error('登录异常')
    }
    await setLocalToken(token)
    ElMessage.success('登录成功')
    await toHomePage(false)
  } catch (err) {
    ElMessage.error(err.response?.data || '登录失败')
  }finally {
    isLoading.value = false // 登录完成或失败后设置为 false
  }
}
</script>

<style scoped>:root {
  --login-bg-light: linear-gradient(135deg, #6a89cc 0%, #3498db 100%);
  --login-bg-dark: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  --card-bg-light: rgba(255, 255, 255, 0.73);
  --card-bg-dark: rgba(30, 30, 46, 0.85);
  --card-border-light: rgba(255, 255, 255, 0.25);
  --card-border-dark: rgba(255, 255, 255, 0.1);
  --title-bg-light: linear-gradient(90deg, #6a89cc, #3498db);
  --title-bg-dark: linear-gradient(90deg, #74b9ff, #0984e3);
  --text-primary-light: #2c3e50;
  --text-primary-dark: #dfe6e9;
  --text-secondary-light: #4a5568;
  --text-secondary-dark: #b2bec3;
  --input-bg-light: #ffffff;
  --input-bg-dark: rgba(50, 50, 70, 0.6);
  --input-border-light: rgba(106, 137, 204, 0.3);
  --input-border-dark: rgba(116, 185, 255, 0.3);
  --button-bg-light: linear-gradient(90deg, #6a89cc 0%, #3498db 100%);
  --button-bg-dark: linear-gradient(90deg, #0984e3 0%, #74b9ff 100%);
}

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--login-bg-light);
  background: url("@assets/MHY_XTLL.png");
  background-attachment: fixed;
  background-size: cover;
  background-position: center;
  padding: 20px;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .login-container {
    background: var(--login-bg-dark);
  }
}

.glass-card {
  width: 420px;
  max-width: 100%;
  padding: 50px 45px;
  border-radius: 25px;
  background: var(--card-bg-light);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid var(--card-border-light);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.18);
  transition: all 0.3s ease;
  text-align: center;
}

@media (prefers-color-scheme: dark) {
  .glass-card {
    background: var(--card-bg-dark);
    border-color: var(--card-border-dark);
    box-shadow: 0 15px 35px rgba(0, 0, 0, 0.35);
  }
}

.glass-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 45px rgba(0, 0, 0, 0.25);
}

@media (prefers-color-scheme: dark) {
  .glass-card:hover {
    box-shadow: 0 20px 45px rgba(0, 0, 0, 0.45);
  }
}

.logo {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 50%;
  margin-bottom: 20px;
  border: 3px solid #6a89cc;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .logo {
    border-color: #74b9ff;
  }
}

.login-title {
  font-size: 32px;
  font-weight: 800;
  margin-bottom: 32px;
  color: transparent;
  background: var(--title-bg-light);
  -webkit-background-clip: text;
  background-clip: text;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .login-title {
    background: var(--title-bg-dark);
    text-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  }
}

.login-title:hover {
  transform: scale(1.04);
  text-shadow: 0 4px 10px rgba(0, 0, 0, 0.18);
}

@media (prefers-color-scheme: dark) {
  .login-title:hover {
    text-shadow: 0 4px 10px rgba(0, 0, 0, 0.4);
  }
}

.glow-input :deep(.el-input__wrapper) {
  background: var(--input-bg-light);
  border: 1px solid var(--input-border-light);
  border-radius: 12px;
  transition: all 0.25s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

@media (prefers-color-scheme: dark) {
  .glow-input :deep(.el-input__wrapper) {
    background: var(--input-bg-dark);
    border-color: var(--input-border-dark);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  }
}

.glow-input :deep(.el-input__wrapper):hover {
  border-color: #6a89cc;
  box-shadow: 0 4px 14px rgba(106, 137, 204, 0.18);
}

@media (prefers-color-scheme: dark) {
  .glow-input :deep(.el-input__wrapper):hover {
    border-color: #74b9ff;
    box-shadow: 0 4px 14px rgba(116, 185, 255, 0.25);
  }
}

.glow-input :deep(.el-input__wrapper.is-focus) {
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.22);
}

@media (prefers-color-scheme: dark) {
  .glow-input :deep(.el-input__wrapper.is-focus) {
    border-color: #0984e3;
    box-shadow: 0 0 0 3px rgba(9, 132, 227, 0.3);
  }
}

.glow-input :deep(.el-input__inner) {
  color: var(--text-primary-light);
}

@media (prefers-color-scheme: dark) {
  .glow-input :deep(.el-input__inner) {
    color: var(--text-primary-dark);
  }
}

.glow-input :deep(.el-input__inner::placeholder) {
  color: #a0aec0;
}

@media (prefers-color-scheme: dark) {
  .glow-input :deep(.el-input__inner::placeholder) {
    color: #718096;
  }
}

:deep(.el-form-item__label) {
  color: var(--text-secondary-light) !important;
  font-weight: 600;
  margin-bottom: 8px;
  text-align: left;
}

@media (prefers-color-scheme: dark) {
  :deep(.el-form-item__label) {
    color: var(--text-secondary-dark) !important;
  }
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 17px;
  font-weight: 600;
  border: none;
  border-radius: 12px;
  background: var(--button-bg-light);
  color: white;
  margin-top: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 6px 18px rgba(106, 137, 204, 0.35);
}

@media (prefers-color-scheme: dark) {
  .login-button {
    background: var(--button-bg-dark);
    box-shadow: 0 6px 18px rgba(9, 132, 227, 0.45);
  }
}

.login-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(52, 152, 219, 0.45);
}

@media (prefers-color-scheme: dark) {
  .login-button:hover {
    box-shadow: 0 12px 28px rgba(9, 132, 227, 0.55);
  }
}

.login-button:active {
  transform: translateY(0);
  box-shadow: 0 4px 12px rgba(106, 137, 204, 0.3);
}

@media (prefers-color-scheme: dark) {
  .login-button:active {
    box-shadow: 0 4px 12px rgba(9, 132, 227, 0.4);
  }
}

/* 手机端适配 */
@media (max-width: 768px) {
  .login-container {
    padding: 15px;
  }

  .glass-card {
    width: 100%;
    max-width: 380px;
    padding: 40px 30px;
  }

  .logo {
    width: 60px;
    height: 60px;
    margin-bottom: 15px;
  }

  .login-title {
    font-size: 24px;
    margin-bottom: 25px;
  }

  .glow-input :deep(.el-input__wrapper) {
    border-radius: 10px;
  }

  .login-button {
    height: 45px;
    font-size: 16px;
    margin-top: 15px;
  }
}

@media (max-width: 480px) {
  .glass-card {
    padding: 30px 25px;
    border-radius: 20px;
  }

  .logo {
    width: 50px;
    height: 50px;
    border-width: 2px;
  }

  .login-title {
    font-size: 20px;
    margin-bottom: 20px;
  }

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-form-item__label) {
    font-size: 14px;
  }

  .glow-input :deep(.el-input__inner) {
    font-size: 14px;
  }

  .login-button {
    height: 42px;
    font-size: 15px;
    border-radius: 10px;
  }
}
</style>