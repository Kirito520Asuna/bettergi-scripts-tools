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
    const token_name= import.meta.env.VITE_BASE_TOKEN_NAME|| 'bgi_tools_token'
    console.log("login=>name:",token_name)
    console.log("login=>token:",token)
    localStorage.setItem(token_name, token)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (err) {
    ElMessage.error(err.response?.data || '登录失败')
  }finally {
    isLoading.value = false // 登录完成或失败后设置为 false
  }
}
</script>

<style scoped>

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #6a89cc 0%, #3498db 100%);
  /* 如果想用图片背景，和首页一致，可替换为： */
  background: url("@assets/MHY_XTLL.png");
  background-attachment: fixed;
  background-size: cover;
  background-position: center;
  /*padding: 20px;*/
  /*box-sizing: border-box;*/
}

.glass-card {
  width: 420px;
  max-width: 100%;
  padding: 50px 45px;
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.73);
  /*background: linear-gradient(135deg, #b807fd 0%, #b8cbd8 100%);*/
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.18);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  text-align: center;
}

.glass-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 45px rgba(0, 0, 0, 0.25);
}

.login-title {
  font-size: 32px;
  font-weight: 800;
  margin-bottom: 32px;
  color: transparent;
  background: linear-gradient(90deg, #6a89cc, #3498db);
  -webkit-background-clip: text;
  background-clip: text;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
  transition: all 0.3s ease;
}

.login-title:hover {
  transform: scale(1.04);
  text-shadow: 0 4px 10px rgba(0, 0, 0, 0.18);
}

/* 输入框统一美化，与首页功能项风格呼应 */
.glow-input :deep(.el-input__wrapper) {
  background: #ffffff;
  border: 1px solid rgba(106, 137, 204, 0.3);
  border-radius: 12px;
  transition: all 0.25s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.glow-input :deep(.el-input__wrapper):hover {
  border-color: #6a89cc;
  box-shadow: 0 4px 14px rgba(106, 137, 204, 0.18);
}

.glow-input :deep(.el-input__wrapper.is-focus) {
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.22);
}

.glow-input :deep(.el-input__inner) {
  color: #2c3e50;
}

.glow-input :deep(.el-input__inner::placeholder) {
  color: #a0aec0;
}

:deep(.el-form-item__label) {
  color: #4a5568 !important;
  font-weight: 600;
  margin-bottom: 8px;
  text-align: left;
}

/* 按钮 - 与首页渐变标题色系一致 */
.login-button {
  width: 100%;
  height: 50px;
  font-size: 17px;
  font-weight: 600;
  border: none;
  border-radius: 12px;
  background: linear-gradient(90deg, #6a89cc 0%, #3498db 100%);
  color: white;
  margin-top: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 6px 18px rgba(106, 137, 204, 0.35);
}

.login-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(52, 152, 219, 0.45);
}

.login-button:active {
  transform: translateY(0);
  box-shadow: 0 4px 12px rgba(106, 137, 204, 0.3);
}




</style>