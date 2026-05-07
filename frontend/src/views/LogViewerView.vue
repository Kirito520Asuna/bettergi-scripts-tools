<template>
  <div class="home">

    <div class="log-viewer">
      <h1 class="title">{{ route.meta.title || '日志' }}</h1>
      <el-card class="control-panel">

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

          <el-form-item label="行数">
            <el-select v-model="displayLines" placeholder="选择行数" style="width: 150px" @change="handleDisplayLinesChange">
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
<!--加一个追踪√ 自动将滚动条拉到最底部          -->
          <el-form-item label="间隔(秒)" v-if="autoLoad">
            <el-input-number v-model="autoLoadInterval" :min="1" :max="60" @change="handleIntervalChange"/>
          </el-form-item>
          <el-form-item label="追踪">
            <el-checkbox v-model="autoScroll"></el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadLogFile" :disabled="!selectedFile || !selectedApplication">
              加载日志
            </el-button>
            <el-button @click="clearLog">清空显示</el-button>
            <el-button type="warning" @click="resetAndReload" :disabled="!canReset">重新加载</el-button>
          </el-form-item>
        </el-form>
        <el-tag :type="isConnected ? 'success' : 'danger'">
          {{ isConnected ? '已连接' : '未连接' }}
        </el-tag>
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

        <div class="log-container" >
          <div class="log-text" v-html="coloredLogContent" ref="logContainer"></div>
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
import {goBack, toHomePage,} from "@api/web/web.js";
import {getApplicationIds} from "@api/sys/sys.js";
import {getFileNames, getLogAuthToken} from "@api/log/log.js";
import {useRoute} from "vue-router";

const route = useRoute()
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
const autoLoad = ref(false)
const autoLoadInterval = ref(10)
const autoLoadTimer = ref(null)
const lastTimestamp = ref(null)
const autoScroll = ref(false)
const appListFailCount = ref(0)
const wsConnectFailCount = ref(0)
const wsInstanceMismatchCount = ref(0)
const canReset = ref(false)
const contentList = ref(new Set())
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

const handleDisplayLinesChange = () => {
  if (displayLines.value === 'all') {
    logContent.value = ''
    lastTimestamp.value = null
    currentFileInfo.value = null
    // if (autoLoad.value && isConnected.value && selectedFile.value && selectedApplication.value) {
    //   loadLogFile()
    // }
  }
}

const loadApplicationList = async () => {
  try {
    const response = await getApplicationIds()
    applicationList.value = response.data || []

    if (applicationList.value.length > 0 && !selectedApplication.value) {
      selectedApplication.value = applicationList.value[0]
    }
  } catch (error) {
    appListFailCount.value++
    ElMessage.error('获取应用列表失败: ' + error.message)
    if (appListFailCount.value >= 3) {
      canReset.value = true
      ElMessage.error('获取应用列表失败次数过多，请点击"重新加载"按钮重试')
    }
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
      const response = await getFileNames(selectedApplication.value)
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
  lastTimestamp.value = null
  loadFileList()

  if (autoLoad.value && isConnected.value && selectedFile.value) {
    loadLogFile()
    startAutoLoad()
  }
}
const parseLogColor = (text) => {
  if (!text) return ''

  let html = text


  const timestampRegex = /^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})/gm
  const matches = text.match(timestampRegex)
  if (matches && matches.length > 0) {
    const lastMatch = matches[matches.length - 1]
    lastTimestamp.value = lastMatch.replace(/^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}).*$/, '$1')
  }

  html = html.replace(
      /^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})\s+\[([^\]]+)\]/gm,
      '<span class="log-white">$1</span><span class="log-blue"> | </span><span class="log-trace-id">[$2]</span>'
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

const resetAndReload = async () => {
  appListFailCount.value = 0
  wsConnectFailCount.value = 0
  wsInstanceMismatchCount.value = 0
  canReset.value = false

  logContent.value = ''
  lastTimestamp.value = null
  currentFileInfo.value = null

  await loadApplicationList()
  await loadFileList()
  contentList.value.clear()

  LogWebSocket.disconnect()
  setTimeout(() => {
    setupWebSocket()
  }, 1000)

  ElMessage.success('已重置所有状态，正在重新加载...')
}

const setupWebSocket = async () => {
  try {
    const authResponse = await getLogAuthToken()
    if (!authResponse.data || !authResponse.data.token) {
      ElMessage.error('获取日志授权Token失败')
      return
    }

    logToken.value = authResponse.data.token
    console.log('[LogViewer] 获取到日志Token:', logToken.value)

    LogWebSocket.on('connected', () => {
      isConnected.value = true
      wsConnectFailCount.value = 0
      wsInstanceMismatchCount.value = 0
      ElMessage.success('WebSocket连接成功')
      // if (autoLoad.value && selectedFile.value && selectedApplication.value) {
      //   loadLogFile()
      //   startAutoLoad()
      // }
    })

    LogWebSocket.on('no-connected', () => {
      wsInstanceMismatchCount.value++
      ElMessage.warning(`实例未匹配 (${wsInstanceMismatchCount.value}/5)`)
      if (wsInstanceMismatchCount.value >= 5) {
        canReset.value = true
        ElMessage.error('实例不匹配次数过多，请点击"重新加载"按钮重试')
      }
    })

    LogWebSocket.on('disconnected', () => {
      isConnected.value = false
      wsConnectFailCount.value++;
      ElMessage.warning(`WebSocket连接断开 (${wsConnectFailCount.value}/5)`)

      stopAutoLoad()
      contentList.value.clear()

      if (wsConnectFailCount.value >= 5) {
        canReset.value = true
        ElMessage.error('WebSocket连接失败次数过多，请点击"重新加载"按钮重试')
        LogWebSocket.shouldReconnect = false
      }
    })
    //完整加载无重复情况
    LogWebSocket.on('file_content', (data) => {
      let contentToAdd = ''
      if (Array.isArray(data.data)) {
        // logContent.value = data.data.join('\n') + '\n'
        // for (const item of data.data) {
        //   if (!contentList.value.has(item)) {
        //     contentList.value.add(item)
        //   }
        // }

        let array = new Array();
        for (const item of data.data) {
          if (!contentList.value.has(item)) {
            //去重
            contentList.value.add(item)
            array.push(item);
          }
        }

        if (array.length > 0) {
          contentToAdd = array.join('\n') + '\n'
        }

      } else if (data.data && data.data.trim()) {
        if (!contentList.value.has(data.data)) {
          contentToAdd = data.data
          contentList.value.add(data.data)
        }
      }

      if (contentToAdd !== ''){
        logContent.value = contentToAdd
        currentFileInfo.value = {
          filename: data.filename,
          totalLines: data.totalLines,
          sentLines: data.sentLines
        }
      }

      if (autoScroll.value) {
        scrollToBottom()
      }
    })
    //追加可能存在重复情况
    LogWebSocket.on('file_content_add', (data) => {
      let contentToAdd = ''
      if (Array.isArray(data.data)) {
        let array = new Array();
        for (const item of data.data) {
          if (!contentList.value.has(item)) {
            //去重
            contentList.value.add(item)
            array.push(item);
          }
        }

        if (array.length > 0) {
          contentToAdd = array.join('\n') + '\n'
        }
      } else if (data.data && data.data.trim()) {
        if (!contentList.value.has(data.data)) {
          contentList.value.add(data.data)
          contentToAdd = data.data
        }
      }


      if (contentToAdd !== '') {
        logContent.value += contentToAdd
        currentFileInfo.value = {
          filename: data.filename,
          totalLines: data.totalLines,
          sentLines: (currentFileInfo.value?.sentLines || 0) + data.sentLines
        }
        if (autoScroll.value) {
          scrollToBottom()
        }
      }
    })

    LogWebSocket.on('error', (data) => {
      ElMessage.error('错误: ' + data.message)
    })

    LogWebSocket.on('log', (data) => {

      let contentToAdd = ''
      if (Array.isArray(data.data)) {
        let array = new Array();
        for (const item of data.data) {
          if (!contentList.value.has(item)) {
            //去重
            contentList.value.add(item)
            array.push(item);
          }
        }
        contentToAdd = array.join('\n') + '\n'
      } else if (data.data && data.data.trim()) {
        contentToAdd = data.data + '\n'
        contentList.value.add(data.data)
      }

      if (contentToAdd) {
        logContent.value += contentToAdd
      }

      if (autoScroll.value) {
        scrollToBottom()
      }
    })

    LogWebSocket.on('token_refreshed', (data) => {
      logToken.value = data.newToken
    })

    LogWebSocket.connect(logToken.value, selectedApplication.value, selectedFile.value, displayLines.value, lastTimestamp.value)
  } catch (error) {
    ElMessage.error('初始化WebSocket失败: ' + error.message)
  }
}

const handleFileChange = () => {
  logContent.value = ''
  lastTimestamp.value = null
  contentList.value.clear()
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

  LogWebSocket.loadFile(selectedApplication.value, selectedFile.value, displayLines.value, lastTimestamp.value)
}

const clearLog = () => {
  logContent.value = ''
  currentFileInfo.value = null
  lastTimestamp.value = null
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
  height: 96vh;
  padding: 20px;
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

.title {
  text-align: center;
  margin-bottom: 10px;
  font-size: 32px;
  color: transparent;
  background: linear-gradient(90deg, #ff6b6b, #ef006a);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
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
  padding: 10px;
}
.log-content::-webkit-scrollbar {
  display: none !important;
}
.log-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  background-color: #1e1e1e;
  border-radius: 4px;
  flex: 1;
}

.log-container::-webkit-scrollbar {
  display: none !important;
}

.log-text {
  margin: 0;
  color: #d4d4d4;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
  height: 100%;
  overflow-y: auto;
}

.log-text::-webkit-scrollbar {
  width: 8px !important;
  display: block !important;
}

.log-text::-webkit-scrollbar-track {
  background: #4e8dbd !important;
  border-radius: 4px;
}

.log-text::-webkit-scrollbar-thumb {
  background: #ff8100 !important;
  border-radius: 4px;
}

.log-text::-webkit-scrollbar-thumb:hover {
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
