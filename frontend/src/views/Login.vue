<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>登录 BetterGI 工具</h2>
      <el-form :model="form" ref="formRef" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="admin / user1" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-button type="primary" native-type="submit" style="width:100%">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {login} from "@api/auth/login";

const router = useRouter()
const form = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  try {
    const res = await login(form.value.username, form.value.password)  // 注意 context-path 是 /bgi
    const token = res.token
    localStorage.setItem('token', token)
    ElMessage.success('登录成功')
    router.push('/')  // 或你原来的首页路径
  } catch (err) {
    ElMessage.error(err.response?.data || '登录失败')
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.login-card {
  width: 400px;
}
</style>