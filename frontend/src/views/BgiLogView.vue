<script setup>
import {nextTick, onMounted, reactive, ref} from "vue";
import {goBack, toHomePage} from "@api/web/web.js";
import {useRoute} from "vue-router";
import {analysisBgiLog, BgiLog} from "@api/log/log.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {computed, watch} from 'vue'
import {Loading} from '@element-plus/icons-vue'

const route = useRoute()
const TimeUnitsList = [
  {label: '毫秒', value: 1},
  {label: '秒', value: 1000},
  {label: '分钟', value: 1000 * 60},
  {label: '小时', value: 1000 * 60 * 60},
  {label: '天', value: 1000 * 60 * 60 * 24},
]
const fileJsonDefault = {
  path: undefined,
  fileName: undefined,
  lastTimestamp: undefined,
  lines: [],
  load: {
    //自动加载
    auto: true,
    retryCount: 10,
    errorCount: 0,
    //加载间隔
    interval: 200,
    intervalTimeUnits: 1,
    get loadInterval() {
      const number = this.interval * this.intervalTimeUnits;
      //ElMessage.warning(`加载间隔:${this.interval}${TimeUnitsList.find(item => item.value === this.intervalTimeUnits)?.label}==>${number}ms`)
      return number
    },
    //滚动条追踪行底
    scrollBottom: true
  },
  search: {
    keyword: "",
    level: "ALL"//日志级别 INF,DBG,WER,ERR
  },
  showDialog: {
    upload: false,
    load: false
  }
}

const fileJson = reactive({...fileJsonDefault})
const initFileJson = async () => {
  await ElMessageBox.confirm(`确定重置日志吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  // 1. 重置基础类型属性（undefined 或 null）
  fileJson.path = undefined
  fileJson.fileName = undefined
  fileJson.lastTimestamp = undefined

  // 2. 重置数组：必须创建新数组，防止修改默认引用
  fileJson.lines = []

  // 3. 重置 load 内部属性（保留 load 对象引用以维持 getter）
  fileJson.load.auto = true
  fileJson.load.retryCount = 10
  fileJson.load.errorCount = 0
  fileJson.load.interval = 200
  fileJson.load.intervalTimeUnits = 1
  fileJson.load.scrollBottom = true
  // loadInterval 作为 getter 无需手动赋值

  // 4. 重置 search 内部属性（search 已是普通对象，直接赋值即可）
  fileJson.search.keyword = ""
  fileJson.search.level = "ALL"

  // 5. 重置 showDialog 内部状态
  fileJson.showDialog.upload = false
  fileJson.showDialog.load = false

  // 6. 清理外部副作用（文件句柄、定时器）
  fileHandle = null
  if (autoLoadTimer) {
    clearInterval(autoLoadTimer)
    autoLoadTimer = null
  }

  // 7. 刷新视图过滤结果
  applyFilter()
  ElMessage.info(`已重置日志`)
}
const logFilter = () => {
  const keyword = fileJson.search.keyword?.trim() || '';
  const level = fileJson.search.level?.trim() || '';
  const levelFilter = level && level !== 'ALL';

  // 1. 将原始日志行转换为结构化对象
  const contentList = fileJson.lines.map(line => lineJson(line));
  // ElMessage.warning(`过滤条件:关键字:${keyword} ,日志级别:${level}`)
  // 2. 无过滤条件时，直接返回全部行
  if (!keyword && !levelFilter) return contentList;
// ElMessage.warning(`过滤条件:关键字:${keyword} ,日志级别:${level}`)
  // 3. 组合过滤（注意接收 filter 的返回值）
  const filtered = contentList.filter(item => {
    const matchKeyword = !keyword
        ? true
        : (item.type && item.type.includes(keyword)) ||
        (item.timestamp && item.timestamp.includes(keyword)) ||
        (item.content && item.content.includes(keyword));

    const matchLevel = !levelFilter
        ? true
        : item.level && item.level.includes(level);

    return matchKeyword && matchLevel;
  });

  function buildKey(item) {
    return `${item.timestamp + '|' + item.level + '|' + item.type + '|' + item.content}`
  }

  // 4. 去重：以 timestamp、level、type、content 组合作为唯一标识
  const arr = []
  filtered.forEach(item => {
    const find = arr.find(i => buildKey(i) === buildKey(item));
    if (!find) arr.push(item)
  });
  arr.sort((a, b) => a.order - b.order)
  return arr;
}
let logList = ref(logFilter())
const applyFilter = () => {
  logList.value = logFilter()
}
const autoLoadLogList = () => {
  if (fileJson.load.auto) {
    applyFilter();
  }
}

// const fileJson = ref(fileJsonDefault)
// 提取时间总毫秒的函数
function parseLogTime(logStr) {
  const match = logStr.match(/^\[(\d{2}):(\d{2}):(\d{2})\.(\d{1,3})\]/);
  if (!match) return null;
  const h = +match[1];
  const m = +match[2];
  const s = +match[3];
  const ms = +match[4].padEnd(3, '0');
  return ((h * 60 + m) * 60 + s) * 1000 + ms;
}

const lineJson = (line) => {
//如:line="[19:45:00.509] [DBG] BetterGenshinImpact.Core.Script.Dependence.Log\n[info][\"已完成\"]\"奇域秘藏\"--\"规定时间内完成可获得限时奖励\"\n"
  const parts = line.split('\n');
  //如:header=[19:45:00.509] [DBG] BetterGenshinImpact.Core.Script.Dependence.Log\n
  const header = parts[0]// 第一行，不含换行
  //如:content=[info][\"已完成\"]\"奇域秘藏\"--\"规定时间内完成可获得限时奖励\"\n
  const content = parts.slice(1).join('\n') || '';         // 剩余内容（可能包含换行）
  const headerRegex = /^\[(\d{2}:\d{2}:\d{2}\.\d{3})\]\s*\[([A-Z]+)\]\s*(.*)/;
  //header解析=> timestamp:[19:45:00.509] ,level:[DBG] ,type:[BetterGenshinImpact.Core.Script.Dependence.Log]
  const headerMatch = header.match(headerRegex);
  const timestamp = headerMatch ? `[${headerMatch[1]}]` : ''; // 保留方括号
  const level = headerMatch ? headerMatch[2] : '';
  const type = headerMatch ? headerMatch[3] : '';
  const order = parseLogTime(timestamp);
  let lineJson = {
    timestamp: timestamp?.trim(),
    level: level?.trim(),
    type: type?.trim(),
    order: order,
    content: content?.trim()
  };
  return lineJson
}

// 【核心替换】持久文件句柄，刷新页面自动清空失效
let fileHandle = null;
// 文件输入框引用
const fileInput = ref(null)
// 当前选中的文件信息
const selectedFile = ref(null)
const selectedFileName = ref('')
const autoLoadFile = ref(null)
/*// 触发文件选择框
const triggerFileInput = () => {
  fileInput.value.click()
}*/
// 替换原有 input file 逻辑，改用文件系统API
const triggerFileInput = async () => {
  try {
    // 弹出本地文件选择，获取持久句柄
    [fileHandle] = await window.showOpenFilePicker({
      types: [
        {
          description: '日志文件',
          accept: {'text/plain': ['.log']}
        }
      ]
    });
    const file = await fileHandle.getFile();
    // 校验文件大小
    const isLt50M = file.size / 1024 / 1024 < 50
    if (!isLt50M) {
      ElMessage.error('日志文件大小不能超过 50MB！')
      fileHandle = null;
      return
    }
    selectedFileName.value = file.name;
    // 首次读取一次文件
    // await readLogFile(fileHandle,fileJson.load.auto);
  } catch (err) {
    ElMessage.warning('未选择文件或浏览器不支持文件读取API');
    console.error(err)
  }
}
// 移除旧的 input/拖拽处理，不再使用 File 对象缓存
const handleFileChange = () => {
}
const handleDrop = () => {
}
// 对话框关闭前的处理：解析中禁止关闭
const handleDialogClose = (done) => {
  if (fileJson.showDialog.load) {
    ElMessage.warning('日志正在解析中，请稍候...');
  } else {
    done(); // 正常关闭
  }
};

// 在 script setup 中新增一个辅助函数，将 File 对象读取为 Uint8Array
const readFileAsBytes = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      const arrayBuffer = reader.result;
      resolve(new Uint8Array(arrayBuffer));  // 返回 Uint8Array，可直接作为字节数组
    };
    reader.onerror = reject;
    reader.readAsArrayBuffer(file);
  });
};

/**
 * 修复：统一参数顺序，句柄放第一位 【核心改造】每次读取都从磁盘拉取最新文件
 * @param handle 文件句柄
 * @param autoLoad 是否自动轮询
 * @param throwError 是否抛出异常
 */
async function readLogFile(handle, autoLoad = false, throwError = false) {
  if (!handle) return;
  if (!autoLoad) fileJson.showDialog.load = true
  try {
    // 每次强制读取磁盘最新文件
    const latestFile = await handle.getFile();
    const bytes = await readFileAsBytes(latestFile);
    let lastTimestamp = fileJson.lastTimestamp
    if (latestFile.name !== fileJson.fileName) {
      lastTimestamp = undefined
    }
    const {fileName, logLines, timestamp} = await BgiLog.analysisBgiLog(latestFile.name, bytes, lastTimestamp)
    // 新增日志去重逻辑，防止无限叠加
    if (fileName !== fileJson.fileName) {
      // 切换文件，清空历史
      fileJson.lines = logLines
    } else {
      // 去重追加新日志
      const existSet = new Set(fileJson.lines);
      logLines.forEach(line => {
        if (!existSet.has(line)) {
          fileJson.lines.push(line)
          existSet.add(line)
        }
      })
    }
    fileJson.fileName = fileName
    fileJson.lastTimestamp = timestamp

    if (!autoLoad) {
      ElMessage.success('文件解析成功')
      fileJson.showDialog.upload = false
    }
    applyFilter()
  } catch (err) {
    if (!autoLoad) ElMessage.error('日志解析失败：' + err.message)
    if (throwError) throw err;
  } finally {
    fileJson.showDialog.load = false
  }
}

// 确认加载按钮
const submitUpload = async () => {
  if (!fileHandle) {
    ElMessage.warning('请先点击选择日志文件')
    return
  }
  await readLogFile(fileHandle, false);
}
// 最小兜底宽度，避免内容太少列太窄
const COL_MIN_WIDTH = {
  time: 100,
  level: 70,
  type: 200
}
// 表头固定兜底宽度（空数据时使用，防止布局错乱）
const EMPTY_COL_WIDTH = {
  time: 140,
  level: 90,
  type: 340
}
// 内边距左右补偿值（padding: 12px 14px → 左右各14 = 28）
const PADDING_OFFSET = 28

// 计算文本像素宽度
function getTextWidth(text, fontSize = '13px', fontFamily = 'system-ui') {
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  ctx.font = `${fontSize} ${fontFamily}`
  return ctx.measureText(String(text ?? '')).width
}

// 动态计算三列最大宽度
const calcColumnMaxWidth = () => {
  // 表头文字宽度
  const headerTimeW = getTextWidth('时间', '14px')
  const headerLevelW = getTextWidth('级别', '14px')
  const headerTypeW = getTextWidth('类别', '14px')

  if (logList.value.length === 0) {
    return {
      colTime: Math.max(EMPTY_COL_WIDTH.time, headerTimeW),
      colLevel: Math.max(EMPTY_COL_WIDTH.level, headerLevelW),
      colType: Math.max(EMPTY_COL_WIDTH.type, headerTypeW)
    }
  }

  let maxTimeW = headerTimeW
  let maxLevelW = headerLevelW
  let maxTypeW = headerTypeW
//限制maxType 的最大宽度
  logList.value.forEach(item => {
    const wTime = getTextWidth(item.timestamp)
    const wLevel = getTextWidth(item.level)
    const wType = getTextWidth(item.type)
    maxTimeW = Math.max(maxTimeW, wTime)
    maxLevelW = Math.max(maxLevelW, wLevel)
    maxTypeW = Math.max(maxTypeW, wType)
  })
  // 限制 maxType 的最大宽度，避免超长类型名称挤压内容区域
  maxTypeW = Math.min(maxTypeW, 240)  // 400px 为示例上限，可根据实际调整
  // 加上内边距 + 下限兜底
  const colTime = Math.max(maxTimeW + PADDING_OFFSET, COL_MIN_WIDTH.time)
  const colLevel = Math.max(maxLevelW + PADDING_OFFSET, COL_MIN_WIDTH.level)
  const colType = Math.max(maxTypeW + PADDING_OFFSET, COL_MIN_WIDTH.type)

  return {colTime, colLevel, colType}
}

// 计算 grid 样式
const gridStyle = computed(() => {
  const {colTime, colLevel, colType} = calcColumnMaxWidth()
  return {
    gridTemplateColumns: `${colTime}px ${colLevel}px ${colType}px 1fr`
  }
})
// 同步表头 + 行 grid 配置（关键：统一布局）
const applyGridLayout = () => {
  const root = document.querySelector('.log-table-wrap')
  if (!root) return
  const cols = root.querySelectorAll('.log-table-header, .log-row')
  cols.forEach(el => {
    el.style.gridTemplateColumns = gridStyle.value.gridTemplateColumns
  })
}

// 用于防止自动加载时重复创建定时器
let autoLoadTimer = null;

// 重启定时器（抽离公共方法，解决切换文件/修改间隔后失效）
function restartTimer() {
  // 销毁旧定时器
  if (autoLoadTimer) {
    clearInterval(autoLoadTimer);
    autoLoadTimer = null;
  }
  // 无句柄/关闭自动加载直接退出
  if (!fileJson.load.auto || !fileHandle) return;

  // fileJson.load.errorCount = 0;
  const intervalMs = fileJson.load.loadInterval
  autoLoadTimer = setInterval(async () => {
    try {
      await readLogFile(fileHandle, true, true);
      fileJson.load.errorCount = 0;
    } catch (err) {
      fileJson.load.errorCount++;
      if (fileJson.load.errorCount >= fileJson.load.retryCount) {
        clearInterval(autoLoadTimer);
        autoLoadTimer = null;
        ElMessage.error('自动加载失败次数达上限，已停止轮询');
      }
    }
  }, intervalMs);
}

// 监听列宽变化，同步布局
watch(gridStyle, () => {
  applyGridLayout();
}, {flush: 'post', immediate: true});
// 监听 logList 的变化（内容更新）
watch(
    logList,
    async (newVal, oldVal) => {
      // 保证 DOM 更新后再操作滚动等
      await nextTick();
      // 如果开启了滚动追踪，则滚动到底部
      if (fileJson.load.scrollBottom) {
        await scrollToBottom();
      }
    },
    {flush: 'post'} // 确保 DOM 更新完成后再执行
);
// 监听自动加载开关、间隔、单位变化 → 重启定时器
watch(
    () => [logList.value, fileJson.load.auto, fileJson.load.interval, fileJson.load.intervalTimeUnits],
    () => {
      if (!fileJson.showDialog.upload)// 上传中不执行
        restartTimer()
    },
    {immediate: false} // 初始化不执行，避免无句柄创建定时器
);
// 级别样式类
const levelClass = (level) => {
  if (!level) return ''
  const map = {DBG: 'level-dbg', INF: 'level-inf', WRN: 'level-war', ERR: 'level-err'}
  return map[level] || ''
}
const scrollToBottom = async () => {
  if (fileJson.load.scrollBottom && logList.value.length > 0) {
    await nextTick()
    //补全
    // 获取滚动容器
    const body = document.querySelector('.log-table-body')
    if (body) {
      // 将滚动条定位到最底部
      body.scrollTop = body.scrollHeight
    }
  }
}
const fileTagInfo = computed(() => {
  const fileName = fileJson.fileName?.trim();
  if (fileName) {
    return {type: 'success', text: fileName};
  } else {
    return {type: 'warning', text: '未解析'};
  }
});
onMounted(() => {
  // 页面销毁清除定时器，防止内存泄漏
  window.addEventListener('beforeunload', () => {
    if (autoLoadTimer) clearInterval(autoLoadTimer)
  })
})
const goToHome = async () => {
  await toHomePage();
};
const goToBack = async () => {
  await goBack();
};
</script>

<template>
  <div class="home">

    <div class="log-viewer">
      <h1 class="title">{{ route.meta.title || 'BGI日志分析' }}</h1>
      <el-card class="control-panel">
        <el-form class="control-form between">
          <el-form-item label="状态">
            <el-tag
                :type="`${(fileJson.search.keyword !=='' || fileJson.search.level !=='ALL')?'warning':((fileJson.fileName?.trim())?'success':'danger')}`"
                style="margin-left: 10px">
              {{
                (fileJson.search.keyword !== '' || fileJson.search.level !== 'ALL') ? '过滤中' : ((fileJson.fileName?.trim()) ? '已连接' : '未连接')
              }}
            </el-tag>
          </el-form-item>
          <el-form-item>
            <el-button class="upload-analysis" @click="fileJson.showDialog.upload=true">点击上传日志解析</el-button>
          </el-form-item>
          <el-form-item>
            <el-button class="upload-analysis" @click="initFileJson">重置日志</el-button>
          </el-form-item>
          <el-form-item label="日志文件">
            <el-tag :type="fileTagInfo.type">{{ fileTagInfo.text }}</el-tag>
          </el-form-item>
        </el-form>
        <el-form class="control-form">
          <el-form-item label="自动加载">
            <el-switch v-model="fileJson.load.auto"/>
          </el-form-item>
          <el-form-item label="间隔时间">
            <el-input-number v-model="fileJson.load.interval" :min="1"/>
          </el-form-item>
          <el-form-item label="间隔时间单位">
            <el-select v-model="fileJson.load.intervalTimeUnits" style="width: 120px"
                       popper-class="black-select-popper"
                       clearable>
              <el-option
                  v-for="time in TimeUnitsList"
                  :key="time.value"
                  :label="time.label"
                  :value="time.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="追踪">
            <el-checkbox v-model="fileJson.load.scrollBottom"/>
          </el-form-item>
        </el-form>
        <el-form class="control-form">
          <el-form-item label="日志级别">
            <el-select v-model="fileJson.search.level" placeholder="全部"
                       style="width: 120px" clearable
                       @change="applyFilter"
                       popper-class="black-select-popper"
            >
              <el-option
                  v-for="line in [
                      {value: 'ALL', label: '全部'},
                      {value: 'DBG', label: 'DBG'},
                      {value: 'INF', label: 'INF'},
                      {value: 'WAR', label: 'WAR'},
                      {value: 'ERR', label: 'ERR'},
                  ]"
                  :key="line.value"
                  :label="line.label"
                  :value="line.value"
              />
            </el-select>

          </el-form-item>
          <el-form-item label="关键字">
            <el-input
                v-model="fileJson.search.keyword"
                placeholder="输入过滤关键字"
                style="width: 200px"
                @input="applyFilter"
                clearable
            />
          </el-form-item>
        </el-form>
      </el-card>
      <el-card class="log-content">
        <template #header class="card-log-header">
          <div class="log-header">
            <span>日志内容</span>
          </div>
        </template>
        <div class="log-container">
          <div class="log-table-wrap" :style="gridStyle">
            <div class="log-table-header" :style="gridStyle">
              <div class="log-col time-col-header">时间</div>
              <div class="log-col level-col-header">级别</div>
              <div class="log-col type-col-header">类别</div>
              <div class="log-col content-col-header">内容</div>
            </div>
            <div class="log-table-body">
              <div v-if="logList.length > 0" class="log-row" v-for="(line, idx) in logList" :key="idx"
                   :style="gridStyle">
                <div class="log-col time-col">{{ line.timestamp }}</div>
                <div class="log-col level-col" :class="levelClass(line.level)">
                  {{ line.level }}
                </div>
                <div class="log-col type-col">{{ line.type }}</div>
                <div class="log-col content-col">{{ line.content }}</div>
              </div>
              <div v-else class="empty-tip">暂无日志数据，请上传日志文件解析</div>
            </div>
          </div>

        </div>
      </el-card>
    </div>


    <el-dialog
        class="upload-dialog"
        v-model="fileJson.showDialog.upload"
        :title="`${fileJson.showDialog.load?'正在解析':'上传'}BGI日志文件`"
        width="500px"
        :close-on-click-modal="!fileJson.showDialog.load"
        :close-on-press-escape="!fileJson.showDialog.load"
        :before-close="handleDialogClose"
    >
      <div v-if="!fileJson.showDialog.load" class="upload-area">
        <div
            class="file-upload-area"
            @click="triggerFileInput"
        >
          <div class="upload-icon">📁</div>
          <p class="upload-text">点击选择本地.log日志文件（支持实时读取更新）</p>
          <p>仅支持 .log 格式，大小不超过 50MB。</p>
          <p class="file-info" v-if="selectedFileName">已选择文件：{{ selectedFileName }}</p>
        </div>
      </div>
      <div v-else class="loading-view">
        <el-icon class="is-loading loading-icon" style="font-size: 40px; color: #409eff;">
          <Loading/>
        </el-icon>
        <p class="loading-text">正在解析日志文件，请稍候...</p>
      </div>
      <template #footer v-if="!fileJson.showDialog.load">
        <div style="text-align: right;">
          <el-button @click="fileJson.showDialog.upload = false">取消</el-button>
          <el-button type="primary" @click="submitUpload" :disabled="!fileHandle">
            确认加载
          </el-button>
        </div>
      </template>
    </el-dialog>

    <div class="fixed-back">
      <button @click="goToBack" class="btn secondary">返回上一页</button>
    </div>
    <div class="fixed-footer">
      <button @click="goToHome" class="btn secondary">🏠 返回主页</button>
    </div>
  </div>
</template>

<style scoped>
@import "@css/bgi_log.css";


.log-table-header :deep(.time-col-header),
.log-table-header :deep(.level-col-header),
.log-table-header :deep(.type-col-header),
.log-table-header :deep(.content-col-header) {
  text-align: center;
  font-weight: bold;
}


/* 弹窗外层整体背景深色 */
:deep(.upload-dialog.el-dialog) {
  background-color: #1e222a !important;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

/* 弹窗标题栏背景+文字白色 */
:deep(.upload-dialog .el-dialog__header) {
  background-color: transparent;
  padding: 16px 20px 8px;
}

:deep(.upload-dialog .el-dialog__title) {
  color: #ffffff !important;
}

/* 关闭叉号变白 */
:deep(.upload-dialog .el-dialog__headerbtn .el-icon) {
  color: #cccccc;
}

/* 弹窗主体内容区域黑色底色 */
:deep(.upload-dialog .el-dialog__body) {
  background-color: #1e222a;
  color: #e5eaf3;
  padding: 12px 20px 16px;
}

/* 弹窗底部按钮栏深色 */
:deep(.upload-dialog .el-dialog__footer) {
  background-color: #1e222a;
  border-top: 1px solid #333945;
  padding: 12px 20px 16px;
}

.upload-dialog .upload-area .file-upload-area {
  background: #232730;
  border: 2px dashed #409eff;
}

.upload-dialog .loading-view {
  text-align: center;
  padding: 40px 20px;
}

.upload-dialog .loading-text {
  color: #e5eaf3;
}

/* 限制上传盒子宽度，自适应弹窗内边距，杜绝横向溢出 */
.upload-area .file-upload-area {
  width: 100% !important;
  box-sizing: border-box !important;
  max-width: 100%;
}

/* 弹窗内容区禁止横向溢出 */
:deep(.upload-dialog .el-dialog__body) {
  overflow-x: hidden;
  box-sizing: border-box;
  padding: 12px 20px 16px;
}

/* ========= upload-dialog 弹窗整体内部纯黑 ========= */
/* 弹窗底部按钮单独深色适配 */
:deep(.upload-dialog .el-dialog__footer .el-button) {
  padding: 8px 22px;
  border-radius: 8px;
}

/* 取消按钮（默认样式改深色） */
:deep(.upload-dialog .el-dialog__footer .el-button--default) {
  background-color: #232730;
  border-color: #383c46;
  color: #ffffff;
}

:deep(.upload-dialog .el-dialog__footer .el-button--default:hover) {
  background-color: rgba(255, 255, 255, 0.1);
  border-color: #409eff;
}

/* 确认加载 主按钮微调匹配深色氛围 */
:deep(.upload-dialog .el-dialog__footer .el-button--primary) {
  background-color: #409eff;
  border-color: #409eff;
  color: #ffffff;
}

:deep(.upload-dialog .el-dialog__footer .el-button--primary:hover) {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

/* 弹窗最外层容器 */
:deep(.upload-dialog.el-dialog) {
  background-color: #000000 !important;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.12);
}

/* 弹窗头部 */
:deep(.upload-dialog .el-dialog__header) {
  background-color: #000000;
  padding: 16px 20px 8px;
}

/* 弹窗标题白色高亮 */
:deep(.upload-dialog .el-dialog__title) {
  color: #ffffff !important;
}

/* 关闭按钮白色 */
:deep(.upload-dialog .el-dialog__headerbtn .el-icon) {
  color: #cccccc;
}

/* 弹窗主体内容区纯黑 */
:deep(.upload-dialog .el-dialog__body) {
  background-color: #000000;
  color: #e5eaf3;
  padding: 12px 20px 16px;
}

/* 弹窗底部按钮栏纯黑 */
:deep(.upload-dialog .el-dialog__footer) {
  background-color: #000000;
  border-top: 1px solid #333945;
  padding: 12px 20px 16px;
}

/* 弹窗内上传区域底色同步页面深色 #232730 */
.upload-dialog .upload-area .file-upload-area {
  background: #232730;
  border: 2px dashed #409eff;
}

/* 加载文字白色 */
.upload-dialog .loading-text {
  color: #ffffff;
}

/*.upload-dialog .load-dialog {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  margin: 10px 0;
  background: linear-gradient(135deg, #f0f4f8 0%, #d9e2ec 100%);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.upload-dialog .load-dialog .loading-icon {
  font-size: 48px;
  color: #409eff;
  animation: iconPulse 1.8s ease-in-out infinite;
}

.upload-dialog .load-dialog .loading-text {
  margin-top: 20px;
  font-size: 15px;
  color: #4a5568;
  letter-spacing: 1px;
  font-weight: 500;
}*/

@keyframes iconPulse {
  0% {
    transform: scale(1);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.15);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 0.7;
  }
}


/*// 加载弹窗美化*/
:deep(.el-dialog.load-dialog) {
  background: #1e222a;
  border-radius: 16px;
}

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

/* ========== el-switch 开关文字 ========== */
.control-form :deep(.el-switch__label) {
  color: #fff;
}

/* ========== el-checkbox 复选框文字 ========== */
.control-form :deep(.el-checkbox__label) {
  color: #ffffff;
}

/* ========== 表单内按钮适配深色 ========== */
.control-form :deep(.upload-analysis) {
  background-color: #232730;
  border-color: #383c46;
  color: #ffffff;
}

.control-form :deep(.upload-analysis:hover) {
  background-color: rgba(255, 255, 255, 0.1);
  border-color: #409eff;
}

</style>

<!-- 全局无作用域样式，解决滚动条、表格全局穿透 -->
<style>
/* 全局滚动条统一美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #1e222a;
}

::-webkit-scrollbar-thumb {
  background: #ff8100;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #1dfbfb;
}

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


/* 仅 upload-dialog 弹窗的遮罩层改为纯黑半透 */
.el-overlay:has(.upload-dialog) {
  background-color: rgba(0, 0, 0, 0.85) !important;
}
</style>
