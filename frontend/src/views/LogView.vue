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
                popper-class="black-select-popper"
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
                popper-class="black-select-popper"
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
            <el-select v-model="displayLines" placeholder="选择行数" style="width: 150px"
                       popper-class="black-select-popper"
                       @change="handleDisplayLinesChange">
              <el-option
                  v-for="line in [
                      {value: '50', label: '50行'},
                      {value: '100', label: '100行'},
                      {value: '200', label: '200行'},
                      {value: '500', label: '500行'},
                      {value: '1000', label: '1000行'},
                      {value: 'all', label: '全部内容'}
                  ]"
                  :key="line.value"
                  :label="line.label"
                  :value="line.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="日志级别">
            <el-select v-model="logLevelFilter" placeholder="全部级别" style="width: 120px" clearable
                       popper-class="black-select-popper"
                       @change="handleLogLevelFilterChange">
              <el-option
                  v-for="line in [
                      {value: '', label: '全部'},
                      {value: ' DEBUG ', label: 'DEBUG'},
                      {value: ' INFO ', label: 'INFO'},
                      {value: ' WARN ', label: 'WARN'},
                      {value: ' ERROR ', label: 'ERROR'},
                  ]"
                  :key="line.value"
                  :label="line.label"
                  :value="line.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="关键字">
            <el-input
                v-model="keywordFilter"
                placeholder="输入过滤关键字"
                style="width: 200px"
                clearable
                @input="handleKeywordFilterChange"
            />
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
        <el-tag v-if="logLevelFilter || keywordFilter" type="warning" style="margin-left: 10px">
          过滤中
        </el-tag>
      </el-card>

      <el-card class="log-content">
        <template #header  class="card-log-header">
          <div class="log-header" >
            <span>日志内容</span>
            <el-tag v-if="currentFileInfo" type="info">
              {{ currentFileInfo.filename }} (共{{ currentFileInfo.totalLines }}行，显示{{ currentFileInfo.sentLines }}行)
            </el-tag>
          </div>
        </template>

        <div class="log-container">
          <div class="log-text" v-if="(logLevelFilter&&logLevelFilter!=='') || (keywordFilter&&keywordFilter!=='')" v-html="coloredLogContentFilter"
               ref="logContainerFilter"></div>

          <div class="log-text" v-else v-html="coloredLogContent" ref="logContainer"></div>
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
const logContainerFilter = ref(null)
const logContainer = ref(null)
const logToken = ref(null)
const autoLoad = ref(true)
const autoLoadInterval = ref(3)
const autoLoadTimer = ref(null)
const lastTimestamp = ref(null)
const autoScroll = ref(true)
const appListFailCount = ref(0)
const wsConnectFailCount = ref(0)
const wsInstanceMismatchCount = ref(0)
const canReset = ref(false)
const contentList = ref(new Set())
const logLevelFilter = ref('')
const keywordFilter = ref('')
const rawContentList = ref([])

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
    contentList.value.clear()
    if (isConnected.value && selectedFile.value && selectedApplication.value) {
      setTimeout(() => {
        loadLogFile()
      }, 100)
    }
  }
}

const handleLogLevelFilterChange = () => {
  applyFilters()
}

const handleKeywordFilterChange = () => {
  applyFilters()
}

const applyFilters = () => {
  let filteredContent = rawContentList.value

  if (logLevelFilter.value) {
    filteredContent = filteredContent.filter(line => line.includes(logLevelFilter.value))
  }

  if (keywordFilter.value) {
    const keyword = keywordFilter.value.toLowerCase()
    filteredContent = filteredContent.filter(line => line.toLowerCase().includes(keyword))
  }
  logContent.value = filteredContent.join('\n') + '\n'
  logContainerFilter.value = filteredContent.join('\n') + '\n'
}

const filteredLineCount = computed(() => {
  if (!logContainerFilter.value) return 0
  return logContainerFilter.value.trim().split('\n').filter(line => line.trim()).length
})


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
      if (data) {
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

        if (data?.applicationId === selectedApplication.value) {
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
const coloredLogContentFilter = computed(() => {
  return parseLogColor(logContainerFilter.value)
})
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
      if (autoLoad.value && selectedFile.value && selectedApplication.value) {
        loadLogFile()
        startAutoLoad()
      }
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
    // LogWebSocket.on('file_content', (data) => {
    //   let contentToAdd = ''
    //   if (Array.isArray(data.data)) {
    //     // logContent.value = data.data.join('\n') + '\n'
    //     // for (const item of data.data) {
    //     //   if (!contentList.value.has(item)) {
    //     //     contentList.value.add(item)
    //     //   }
    //     // }
    //
    //     let array = new Array();
    //     for (const item of data.data) {
    //       if (!contentList.value.has(item)) {
    //         //去重
    //         contentList.value.add(item)
    //         array.push(item);
    //       }
    //     }
    //
    //     if (array.length > 0) {
    //       contentToAdd = array.join('\n') + '\n'
    //     }
    //
    //   } else if (data.data && data.data.trim()) {
    //     if (!contentList.value.has(data.data)) {
    //       contentToAdd = data.data
    //       contentList.value.add(data.data)
    //     }
    //   }
    //
    //   if (contentToAdd !== '') {
    //     logContent.value = contentToAdd
    //     currentFileInfo.value = {
    //       filename: data.filename,
    //       totalLines: data.totalLines,
    //       sentLines: data.sentLines
    //     }
    //   }
    //
    //   if (autoScroll.value) {
    //     scrollToBottom()
    //   }
    // })
    // ... existing code ...
    LogWebSocket.on('file_content', (data) => {
      rawContentList.value = []
      contentList.value.clear()

      if (Array.isArray(data.data)) {
        for (const item of data.data) {
          if (!contentList.value.has(item)) {
            contentList.value.add(item)
            rawContentList.value.push(item)
          }
        }
      } else if (data.data && data.data.trim()) {
        if (!contentList.value.has(data.data)) {
          contentList.value.add(data.data)
          rawContentList.value.push(data.data)
        }
      }

      currentFileInfo.value = {
        filename: data.filename,
        totalLines: data.totalLines,
        sentLines: data.sentLines
      }

      applyFilters()

      if (autoScroll.value) {
        scrollToBottom()
      }
    })
    //追加可能存在重复情况
    // LogWebSocket.on('file_content_add', (data) => {
    //   let contentToAdd = ''
    //   if (Array.isArray(data.data)) {
    //     let array = new Array();
    //     for (const item of data.data) {
    //       if (!contentList.value.has(item)) {
    //         //去重
    //         contentList.value.add(item)
    //         array.push(item);
    //       }
    //     }
    //
    //     if (array.length > 0) {
    //       contentToAdd = array.join('\n') + '\n'
    //     }
    //   } else if (data.data && data.data.trim()) {
    //     if (!contentList.value.has(data.data)) {
    //       contentList.value.add(data.data)
    //       contentToAdd = data.data
    //     }
    //   }
    //
    //
    //   if (contentToAdd !== '') {
    //     logContent.value += contentToAdd
    //     currentFileInfo.value = {
    //       filename: data.filename,
    //       totalLines: data.totalLines,
    //       sentLines: (currentFileInfo.value?.sentLines || 0) + data.sentLines
    //     }
    //     if (autoScroll.value) {
    //       scrollToBottom()
    //     }
    //   }
    // })
    LogWebSocket.on('file_content_add', (data) => {
      if (Array.isArray(data.data)) {
        for (const item of data.data) {
          if (!contentList.value.has(item)) {
            contentList.value.add(item)
            rawContentList.value.push(item)
          }
        }
      } else if (data.data && data.data.trim()) {
        if (!contentList.value.has(data.data)) {
          contentList.value.add(data.data)
          rawContentList.value.push(data.data)
        }
      }

      currentFileInfo.value = {
        filename: data.filename,
        totalLines: data.totalLines,
        sentLines: (currentFileInfo.value?.sentLines || 0) + data.sentLines
      }

      applyFilters()

      if (autoScroll.value) {
        scrollToBottom()
      }
    })

    LogWebSocket.on('error', (data) => {
      ElMessage.error('错误: ' + data.message)
    })

    // LogWebSocket.on('log', (data) => {
    //
    //   let contentToAdd = ''
    //   if (Array.isArray(data.data)) {
    //     let array = new Array();
    //     for (const item of data.data) {
    //       if (!contentList.value.has(item)) {
    //         //去重
    //         contentList.value.add(item)
    //         array.push(item);
    //       }
    //     }
    //     contentToAdd = array.join('\n') + '\n'
    //   } else if (data.data && data.data.trim()) {
    //     contentToAdd = data.data + '\n'
    //     contentList.value.add(data.data)
    //   }
    //
    //   if (contentToAdd) {
    //     logContent.value += contentToAdd
    //   }
    //
    //   if (autoScroll.value) {
    //     scrollToBottom()
    //   }
    // })
    LogWebSocket.on('log', (data) => {
      if (Array.isArray(data.data)) {
        for (const item of data.data) {
          if (!contentList.value.has(item)) {
            contentList.value.add(item)
            rawContentList.value.push(item)
          }
        }
      } else if (data.data && data.data.trim()) {
        if (!contentList.value.has(data.data)) {
          contentList.value.add(data.data)
          rawContentList.value.push(data.data)
        }
      }

      applyFilters()

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
  rawContentList.value = []
  currentFileInfo.value = null
  lastTimestamp.value = null
  contentList.value.clear()
}

const scrollToBottom = async () => {
  await nextTick()

  const container = ((logLevelFilter.value !== "" && logLevelFilter.value) || keywordFilter.value) ? logContainerFilter.value : logContainer.value
  if (container) {
    container.scrollTop = container.scrollHeight
  }

  // if (logContainer.value) {
  //   logContainer.value.scrollTop = logContainer.value.scrollHeight
  // }
}
</script>

<style scoped>
@import "@css/log.css";

/*// 卡片全局深色覆盖*/
:deep(.el-card) {
  background: rgba(30, 34, 42, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.35);
}

:deep(.el-card__header) {
  background: transparent;
  border-bottom: 1px solid #333945;
  color: #d4d4d4;
}


/* 输入框占位文字也改成白色半透，更协调 */
.control-form :deep(.el-select__placeholder) {
  color: rgba(255, 255, 255, 0.7) !important;
}


/* 1. 表单 label 白色高亮 */
.control-form :deep(.el-form-item__label) {
  color: #ffffff !important;
  font-weight: 500;
}

/* ========== el-input 普通输入框 ========== */
.control-form :deep(.el-input__wrapper) {
  background-color: #232730;
  box-shadow: none;
}

.control-form :deep(.el-input__inner) {
  color: #ffffff;
}

.control-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.65);
}

/* 清除按钮 */
.control-form :deep(.el-input__suffix .el-icon-circle-close) {
  color: rgba(255, 255, 255, 0.7);
}

/* ========== el-input-number 数字输入框 ========== */
/* ========== el-input-number 数字输入框 完整修复（仅.control-form内生效） ========== */
.control-form :deep(.el-input-number) {
  --el-input-bg-color: #232730;
}

/* 输入框主体背景 */
.control-form :deep(.el-input-number__inner-wrapper .el-input__wrapper) {
  background-color: #232730 !important;
  box-shadow: none;
}

/* 减号按钮 */
.control-form :deep(.el-input-number__decrease) {
  background-color: #232730 !important;
  color: #ffffff !important;
  border-color: #383c46;
}

/* 加号按钮 */
.control-form :deep(.el-input-number__increase) {
  background-color: #232730 !important;
  color: #ffffff !important;
  border-color: #383c46;
}

/* 按钮hover深色适配 */
.control-form :deep(.el-input-number__increase:hover,
.control-form :deep(.el-input-number__decrease:hover)) {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

/* 输入框内部文字白色 */
.control-form :deep(.el-input-number__inner-wrapper .el-input__inner) {
  color: #ffffff;
}

/* ========== el-select 选择框本体 ========== */
.control-form :deep(.el-select__wrapper) {
  background: #232730;
  /*box-shadow: none;*/
}

.control-form :deep(.el-select__placeholder, .el-select__selected-item) {
  color: #ffffff;
}

.control-form :deep(.el-select__suffix) {
  color: rgba(255, 255, 255, 0.75);
}

</style>
<style>
.black-select-popper {
  background-color: #232730 !important;
  /* 清除默认下拉内边距、圆角、边框阴影，和输入框对齐 */
  padding: 0 !important;
  border: none !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.35) !important;
}

.black-select-popper .el-select-dropdown__item {
  color: #ffffff;
  background-color: transparent;
}

.black-select-popper .el-select-dropdown__item:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.black-select-popper .el-select-dropdown__item.selected {
  background-color: rgba(255, 255, 255, 0.15);
  color: #ffffff;
}
</style>