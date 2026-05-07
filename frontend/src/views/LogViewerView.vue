<template>
  <div class="home">

    <div class="log-viewer">
      <el-card class="control-panel">
        <template #header>
          <div class="card-header">
            <span>日志查看器</span>
            <el-tag :type="isConnected ? 'success' : 'danger'">
              {{ isConnected ? '已连接' : '未连接' }}
            </el-tag>
          </div>
        </template>

        <el-form :inline="true" class="control-form">
          <el-form-item label="应用实例">
            <el-select
                v-model="selectedApplication"
                placeholder="选择应用实例"
                style="width: 300px"
                @change="handleApplicationChange"
            >
              <el-option
                  v-for="app in applicationList"
                  :key="app"
                  :label="app"
                  :value="app"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="日志文件">
            <el-select
                v-model="selectedFile"
                placeholder="选择日志文件"
                style="width: 300px"
                @change="handleFileChange"
                :disabled="!selectedApplication"
            >
              <el-option
                  v-for="file in fileList"
                  :key="file"
                  :label="file"
                  :value="file"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="显示行数">
            <el-select v-model="displayLines" placeholder="选择行数" style="width: 150px">
              <el-option label="50行" value="50"/>
              <el-option label="100行" value="100"/>
              <el-option label="200行" value="200"/>
              <el-option label="500行" value="500"/>
              <el-option label="全部内容" value="all"/>
            </el-select>
          </el-form-item>

          <el-form-item label="自动加载">
            <el-switch v-model="autoLoad" @change="handleAutoLoadChange"/>
          </el-form-item>

          <el-form-item label="间隔(秒)" v-if="autoLoad">
            <el-input-number v-model="autoLoadInterval" :min="1" :max="60" @change="handleIntervalChange"/>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="loadLogFile" :disabled="!selectedFile || !selectedApplication">
              加载日志
            </el-button>
            <el-button @click="clearLog">清空显示</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="log-content">
        <template #header>
          <div class="card-header">
            <span>日志内容</span>
            <el-tag v-if="currentFileInfo" type="info">
              {{ currentFileInfo.filename }} (共{{ currentFileInfo.totalLines }}行，显示{{ currentFileInfo.sentLines }}行)
            </el-tag>
          </div>
        </template>

        <div class="log-container" ref="logContainer">
          <div class="log-text" v-html="coloredLogContent"></div>
        </div>
      </el-card>
    </div>

    <div class="fixed-back">
      <button @click="goToBack" class="btn secondary">返回上一页</button>
    </div>
    <div class="fixed-footer">
      <button @click="goToHome" class="btn secondary">🏠 返回主页</button>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted, onUnmounted, nextTick} from 'vue'
import {ElMessage} from 'element-plus'
import LogWebSocket from '@/utils/LogWebSocket'
import service from '@/utils/request'
import {getLocalToken, goBack, toHomePage} from "@api/web/web.js";
import {getApplicationIds} from "@api/sys/sys.js";

const selectedApplication = ref('')
const selectedFile = ref('')
const displayLines = ref('200')
const fileList = ref([])
const applicationList = ref([])
const logContent = ref('')
const isConnected = ref(false)
const currentFileInfo = ref(null)
const logContainer = ref(null)
const logToken = ref(null)
const autoLoad = ref(true)
const autoLoadInterval = ref(3)
const autoLoadTimer = ref(null)

const goToHome = async () => {
  await toHomePage();
};
const goToBack = async () => {
  await goBack();
};

onMounted(async () => {
  await loadApplicationList()
  await loadFileList()
  await setupWebSocket()
})

onUnmounted(() => {
  LogWebSocket.disconnect()
  if (autoLoadTimer.value) {
    clearInterval(autoLoadTimer.value)
  }
})

const loadApplicationList = async () => {
  try {
    const response = await getApplicationIds()
    applicationList.value = response.data || []

    if (applicationList.value.length > 0 && !selectedApplication.value) {
      selectedApplication.value = applicationList.value[0]
    }
  } catch (error) {
    ElMessage.error('获取应用列表失败: ' + error.message)
  }
}

const loadFileList = async () => {
  if (!selectedApplication.value) {
    fileList.value = []
    return
  }

  const startTime = Date.now()
  const timeout = 3 * 60 * 1000

  while (true) {
    const elapsedTime = Date.now() - startTime

    if (elapsedTime > timeout) {
      ElMessage.error(`获取日志文件超时（超过${timeout / 1000}秒）`)
      throw new Error('获取日志文件超时')
    }

    try {
      const response = await service.get('/jwt/log/file-names', {
        params: { applicationId: selectedApplication.value }
      })

      const data = response.data
      if (data){
        if (data?.fileNames) {
          fileList.value = data.fileNames || []

          if (fileList.value.length > 0 && !selectedFile.value) {
            selectedFile.value = fileList.value[0]
          }

          if (data.applicationId === selectedApplication.value) {
            return
          }
        } else {
          fileList.value = []
        }

        if (data?.applicationId === selectedApplication.value){
          break
        }
      }
    } catch (error) {
      ElMessage.error('获取文件列表失败: ' + error.message)
      fileList.value = []
    }

    await new Promise(resolve => setTimeout(resolve, 2000))
  }
}


const handleApplicationChange = () => {
  selectedFile.value = ''
  loadFileList()

  if (autoLoad.value && isConnected.value && selectedFile.value) {
    loadLogFile()
    startAutoLoad()
  }
}

const parseLogColor = (text) => {
  if (!text) return ''

  let html = text

  html = html.replace(
      /(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})\s+\[([^\]]+)\]/g,
      '<span class="log-white">$1</span> <span class="log-trace-id">[$2]</span>'
  )

  html = html.replace(
      /\b(DEBUG)\b/g,
      '<span class="log-green">$1</span>'
  )
  html = html.replace(
      /\b(INFO)\b/g,
      '<span class="log-blue">$1</span>'
  )
  html = html.replace(
      /\b(WARN)\b/g,
      '<span class="log-yellow">$1</span>'
  )
  html = html.replace(
      /\b(ERROR)\b/g,
      '<span class="log-red">$1</span>'
  )

  html = html.replace(
      /(\d{1,6})\s+(---)/g,
      '<span class="log-magenta">$1</span> $2'
  )

  html = html.replace(
      /(---\s*)\[([^\]]+)\]/g,
      '<span class="log-white">$1</span><span class="log-thread">[$2]</span>'
  )

  html = html.replace(
      /([a-zA-Z0-9._-]{10,}(?=\s*:))/g,
      '<span class="log-cyan">$1</span>'
  )

  return html
}

const coloredLogContent = computed(() => {
  return parseLogColor(logContent.value)
})

const handleAutoLoadChange = (value) => {
  if (value) {
    startAutoLoad()
  } else {
    stopAutoLoad()
  }
}

const handleIntervalChange = () => {
  if (autoLoad.value) {
    startAutoLoad()
  }
}

const startAutoLoad = () => {
  stopAutoLoad()
  if (autoLoad.value && selectedFile.value && isConnected.value && selectedApplication.value) {
    autoLoadTimer.value = setInterval(() => {
      loadLogFile()
    }, autoLoadInterval.value * 1000)
  }
}

const stopAutoLoad = () => {
  if (autoLoadTimer.value) {
    clearInterval(autoLoadTimer.value)
    autoLoadTimer.value = null
  }
}

const setupWebSocket = async () => {
  let token = await getLocalToken()
  logToken.value = token
  if (!token || token === 'undefined' || token === 'null') {
    ElMessage.warning('未找到登录令牌，请先登录')
    return
  }

  console.log('token: ' + token)

  LogWebSocket.on('connected', () => {
    isConnected.value = true
    ElMessage.success('WebSocket连接成功')
    if (autoLoad.value && selectedFile.value && selectedApplication.value) {
      loadLogFile()
      startAutoLoad()
    }
  })

  LogWebSocket.on('disconnected', () => {
    isConnected.value = false
    ElMessage.warning('WebSocket连接断开')
    stopAutoLoad()
  })

  LogWebSocket.on('file_content', (data) => {
    logContent.value = data.data
    currentFileInfo.value = {
      filename: data.filename,
      totalLines: data.totalLines,
      sentLines: data.sentLines
    }
    scrollToBottom()
  })

  LogWebSocket.on('error', (data) => {
    ElMessage.error('错误: ' + data.message)
  })

  LogWebSocket.on('log', (data) => {
    logContent.value += data.data + '\n'
    scrollToBottom()
  })

  LogWebSocket.connect(token, selectedApplication.value, selectedFile.value, displayLines.value)
}

const handleFileChange = () => {
  if (autoLoad.value && isConnected.value && selectedFile.value && selectedApplication.value) {
    loadLogFile()
    startAutoLoad()
  }
}

const loadLogFile = () => {
  if (!selectedApplication.value) {
    ElMessage.warning('请先选择应用实例')
    return
  }
  if (!selectedFile.value) {
    ElMessage.warning('请先选择日志文件')
    return
  }

  LogWebSocket.loadFile(selectedApplication.value, selectedFile.value, displayLines.value)
}

const clearLog = () => {
  logContent.value = ''
  currentFileInfo.value = null
}

const scrollToBottom = async () => {
  await nextTick()
  if (logContainer.value) {
    logContainer.value.scrollTop = logContainer.value.scrollHeight
  }
}
</script>

<style scoped>
.log-viewer {
  width: 80vw;
  padding: 20px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 20px;

  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3),
  inset 0 0 0 1px rgba(255, 255, 255, 0.1);
  overflow-y: auto;
  position: relative;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.control-panel {
  flex-shrink: 0;
  border-radius: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.control-form {
  margin-bottom: 0;
}

.log-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  border-radius: 24px;
}

.log-container {
  height: calc(100vh - 300px);
  overflow-y: auto;
  background-color: #1e1e1e;
  border-radius: 4px;
  padding: 15px;
}

.log-container::-webkit-scrollbar {
  width: 8px !important;
  display: block !important;
}

.log-container::-webkit-scrollbar-track {
  background: #4e8dbd !important;
  border-radius: 4px;
}

.log-container::-webkit-scrollbar-thumb {
  background: #ff8100 !important;
  border-radius: 4px;
}

.log-container::-webkit-scrollbar-thumb:hover {
  background: #1dfbfb !important;
}

.log-text {
  margin: 0;
  color: #d4d4d4;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.log-text :deep(.log-white) {
  color: #ffffff;
}

.log-text :deep(.log-cyan) {
  color: #00bcd4;
}

.log-text :deep(.log-green) {
  color: #4caf50;
}

.log-text :deep(.log-blue) {
  color: #2196f3;
}

.log-text :deep(.log-yellow) {
  color: #ffeb3b;
  font-weight: bold;
}

.log-text :deep(.log-red) {
  color: #f44336;
}

.log-text :deep(.log-magenta) {
  color: #952ded;
}

.log-text :deep(.log-thread) {
  color: #c8fd00;
}

.log-text :deep(.log-trace-id) {
  color: #d11594;
}
</style>
