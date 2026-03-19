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

<style scoped>
@import '@css/login.css';
</style>