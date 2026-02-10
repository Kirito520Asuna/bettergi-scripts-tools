<template xmlns="http://www.w3.org/1999/html">
  <div class="home">
    <div class="container">

      <h1 class="title">BetterGI Script Tools@{{ currentRoute?.meta?.title || '未知标题' }}</h1>

      <!-- Cron 相关功能 -->
      <h2 class="section-title">Cron 功能</h2>

      <div class="section">
        <div class="card">
          <h3 class="section-title">[时区为东八区]解析cron表达式获取俩个时间戳中符合条件的首个时间戳 没有就返回null</h3>
          <div class="one-item">
            <div class="form-group">
              <label class="label">Cron 表达式:</label>
              <input v-model="cronExpression" class="input" placeholder="例如: 0 0 * * * ?"/>
            </div>
            <div class="form-group">
              <label class="label">开始时间戳:</label>
              <!--            <input v-model.number="startTimestamp" class="input" type="number" placeholder="开始时间戳"/>-->
              <el-date-picker
                  v-model="startTime"
                  type="datetime"
                  placeholder="选择开始时间"
              />
            </div>
            <div class="form-group">
              <label class="label">结束时间戳:</label>
              <!--            <input v-model.number="endTimestamp" class="input" type="number" placeholder="结束时间戳"/>-->
              <el-date-picker
                  v-model="endTime"
                  type="datetime"
                  placeholder="选择结束时间"
              />
            </div>
          </div>
          <button @click="getNextTime" class="btn primary">获取下一个时间戳</button>
          <label class="label">返回结果:</label>
          <div class="result-all">
            <pre class="result">{{ cronResult || '暂无返回数据' }}</pre>
            <button @click="copyToClipboard(cronResult)" class="copy-btn">📋 复制</button>
          </div>
        </div>

        <div class="card">
          <h3 class="section-title">[时区为东八区]大批量解析</h3>
          <div class="list-item">
            <div class="list-one-item header">
              <label class="label">id</label>
              <label class="label">任务标识唯一值</label>
              <label class="label">Cron 表达式</label>
              <label class="label">开始时间</label>
              <label class="label">结束时间</label>
              <label class="label">操作按钮 </label>
            </div>
            <br/>
            <div v-for="(item, index) in cronList" :key="index" class="list-one-item">
              <p>{{ index + 1 }}</p>
              <input v-model="item.key" class="input small" placeholder="任务标识唯一值"/>
              <input v-model="item.cronExpression" class="input small" placeholder="Cron 表达式 如: 0 0 * * * ?"/>
              <!--            <input v-model.number="item.startTimestamp" class="input small" type="number" placeholder="开始时间戳"/>-->
              <el-date-picker
                  v-model="item.startTime"
                  type="datetime"
                  placeholder="选择开始时间时间"
              />
              <!--            <input v-model.number="item.endTimestamp" class="input small" type="number" placeholder="结束时间戳"/>-->
              <el-date-picker
                  v-model="item.endTime"
                  type="datetime"
                  placeholder="选择结束时间"
              />
              <button @click="cronListRemoveItem(index)" class="btn danger">删除</button>
            </div>
          </div>
          <div class="actions">
            <button @click="cronListAddItem" class="btn secondary">添加任务</button>
            <button @click="cronListSubmit" class="btn primary">提交</button>
          </div>
          <label class="label">返回结果:</label>
          <div class="result-all">
            <pre class="result">{{ cronListResult || '暂无返回数据' }}</pre>
            <button @click="copyToClipboard(cronListResult)" class="copy-btn">📋 复制</button>
          </div>
        </div>
      </div>
      <h2 class="section-title">OCR 功能</h2>
      <!-- OCR 相关功能 -->
      <div class="section">
        <div class="card">
          <h3 class="section-title">OCR 图片字节组</h3>
          <!--        <input type="file" @change="handleFileUpload" class="file-input"/>-->

          <div class="file-upload-container">
            <div class="file-upload-area" @click="triggerFileInput" @dragover.prevent @drop.prevent="handleDrop">
              <input
                  type="file"
                  ref="fileInput"
                  @change="handleFileUpload"
                  class="file-input"
                  accept=".png,.jpg,.jpeg,.pdf"
              />
              <!-- 限制文件类型 -->
              <div class="upload-icon">📁</div>
              <p class="upload-text">点击选择文件或拖拽到此处</p>
              <p class="file-info" v-if="fileName">已选择文件：{{ fileName }}</p>
            </div>
          </div>

          <button @click="performOcr" class="btn primary">执行 OCR 识别</button>
          <label class="label">返回结果:</label>
          <div class="result-all">
            <pre class="result">{{ ocrResult || '暂无返回数据' }}</pre>
            <button @click="copyToClipboard(ocrResult)" class="copy-btn">📋 复制</button>
          </div>
        </div>
      </div>
    </div>
    <div class="fixed-footer">
      <button @click="goToHome" class="btn secondary">🏠 返回主页</button>
    </div>
  </div>
</template>


<script setup>
import {ref} from 'vue'
import service from "@utils/request";
import {ElMessage} from "element-plus";
import router from "@router/router";
import {getNextTimestampAll} from "@api/cron/cron.js";
import {ocrBytes} from "@api/ocr/ocr.js";
import {CopyToClipboard} from "@utils/local.js";

const currentRoute = router.currentRoute
const cronResult = ref('')
const ocrResult = ref('')
const cronExpression = ref('0 0 * * * ?')
// const timeRange = ref([])
// const defaultTime = new Date(2000, 1, 1, 12, 0, 0)

const startTime = ref(new Date(Date.now()))
const endTime = ref(new Date(Date.now() + 86400000))

const cronList = ref([
  {
    key: 'task1',
    startTime: ref(new Date(Date.now())),
    endTime: ref(new Date(Date.now() + 86400000)),
    cronExpression: '0 0 * * * ?',
    startTimestamp: Date.now(),
    endTimestamp: Date.now() + 86400000,
  },
]);
const cronListResult = ref('');

const cronListAddItem = () => {
  cronList.value.push({
    key: `task${cronList.value.length + 1}`,
    startTime: ref(new Date(Date.now())),
    endTime: ref(new Date(Date.now() + 86400000)),
    cronExpression: '0 0 * * * ?',
    startTimestamp: Date.now(),
    endTimestamp: Date.now() + 86400000,
  });
};

const cronListRemoveItem = (index) => {
  cronList.value.splice(index, 1);
};

const cronListSubmit = async () => {
  try {
    const list = []
    let cronListJson = cronList.value
    console.log('cronListJson', JSON.stringify(cronListJson, null, 2))
    cronListJson.forEach(item => {
      item.startTimestamp = new Date(item.startTime).getTime()
      item.endTimestamp = new Date(item.endTime).getTime()
      list.push({
        key: item.key,
        cronExpression: item.cronExpression,
        startTimestamp: item.startTimestamp,
        endTimestamp: item.endTimestamp,
      })
    })
    console.log('cronListJson', JSON.stringify(cronListJson, null, 2))
    const response =  await getNextTimestampAll(list)
    cronListResult.value = JSON.stringify(response, null, 2);
  } catch (error) {
    console.error('请求失败:', error);
  }
};
const goToHome = () => {
  router.push('/'); // 假设主页路径是 '/'
};

// 获取单个 Cron 表达式的下一个时间戳
const getNextTime= async () => {
  try {
    const startTimestamp = new Date(startTime.value).getTime()
    const endTimestamp = new Date(endTime.value).getTime()
    const response =  await getNextTimestamp(cronExpression.value, startTimestamp, endTimestamp)
    cronResult.value = JSON.stringify(response, null, 2)
  } catch (error) {
    console.error('Error fetching next timestamp:', error)
  }
}
const fileInput = ref(null);
const file = ref(null);
const fileName = ref('');

const triggerFileInput = () => {
  if (fileInput.value) {
    fileInput.value.click(); // 触发 input 点击
  } else {
    console.error('fileInput is not available');
  }
};
// 处理文件上传
const handleFileUpload = (event) => {
  const selectedFile = event.target.files[0];
  if (selectedFile) {
    file.value = selectedFile;
    fileName.value = selectedFile.name;
  }
};

const handleDrop = (event) => {
  const droppedFile = event.dataTransfer.files[0];
  if (droppedFile) {
    file.value = droppedFile;
    fileName.value = droppedFile.name;
  }
};

// 执行 OCR 识别
const performOcr = async () => {
  if (!file.value) {
    alert('请先选择一个文件')
    return
  }

  try {
    const reader = new FileReader()
    reader.onload = async (e) => {
      const arrayBuffer = e.target.result
      const bytes = Array.from(new Uint8Array(arrayBuffer))
      const response =await ocrBytes(bytes)
      ocrResult.value = JSON.stringify(response, null, 2)
    }

    reader.readAsArrayBuffer(file.value)
  } catch (error) {
    console.error('Error performing OCR:', error)
  }
}

const copyToClipboard = (text) => {
  CopyToClipboard(text)
};
</script>


<style scoped>
.home {
  min-height: 100vh;
  /*  padding: 20px;*/
  /*margin: 0 auto;*/
  background: url("@assets/MHY_XTLL.png");
  /* 关键：固定背景，不随滚动重复或变形 */
  background-attachment: fixed; /* ← 核心属性 */
  background-size: cover; /* 覆盖整个容器 */
  background-position: center;
}

/* 容器布局 */
.container {
  min-width: 70%;
  margin: 0 auto;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa, #e4edf9);
  min-height: 100vh;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 30px;
  color: #2c3e50;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 卡片样式 */
.card {
  background: #ffffff;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}

.section-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 20px;
  color: #3498db;
  text-align: center;
}

/* 表单项 */
.form-group {
  margin-bottom: 20px;
}

.label {
  display: block;
  font-size: 1rem;
  font-weight: 500;
  margin-bottom: 8px;
  color: #2c3e50;
}

.input {
  width: 80%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 5px rgba(52, 152, 219, 0.3);
}

.input.small {
  width: 150px; /* 固定宽度 */
  display: inline-block;
  margin-right: 10px;
}

.btn.danger {
  width: 80px; /* 固定宽度 */
  padding: 8px 12px;
}

.field {
  display: flex;
  flex-direction: column;
  margin-right: 15px;
}

.field .label {
  font-size: 0.9rem;
  margin-bottom: 4px;
}

/* 按钮样式 */
.btn {
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-right: 10px;
}

.btn.primary {
  background: #3498db;
  color: white;
}

.btn.primary:hover {
  background: #2980b9;
  transform: scale(1.05);
}

.btn.secondary {
  background: #95a5a6;
  color: white;
}

.btn.secondary:hover {
  background: #7f8c8d;
  transform: scale(1.05);
}

.btn.danger:hover {
  background: #c0392b;
  transform: scale(1.05);
}

/* 文件上传 */
.file-input {
  margin-bottom: 15px;
  padding: 10px;
  border: 1px dashed #3498db;
  border-radius: 8px;
  width: 100%;
  cursor: pointer;
}

.file-input:hover {
  background: #f1f8ff;
}

.result-all {
  display: grid;
  grid-template-columns:
  8fr        /* 输出值 */
  auto; /* 复制按钮 */
  align-items: center;
}

/* 结果展示 */
.result {
  background: linear-gradient(135deg, #ddb568, #ffffff); /* 添加渐变背景 */
  padding: 15px;
  border-radius: 8px;
  margin-top: 15px;
  white-space: pre-wrap;
  font-family: monospace;
  font-size: 0.9rem;
  color: rgb(230, 0, 103); /* 修改为你想要的颜色 */
}

/* 列表项 */
.one-item {
  display: flex;
  align-items: center; /* 垂直居中对齐 */
  gap: 10px; /* 子元素之间的间距 */
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 10px;
  background: #fafafa;
}

.label {
  /* text-align: center;*/
  white-space: nowrap; /* 防止换行 */
  overflow: hidden;
  text-overflow: ellipsis; /* 超出部分用省略号表示 */
  max-width: 100%;
}

.list-item {
  /*background: #91dcd6 !important;*/
  background: linear-gradient(135deg, #c22dd1, #91dcd6) !important; /* 添加渐变背景 */
  border-radius: 12px !important; /* 添加圆角 */
  padding: 10px !important; /* 可选：增加内边距以提升视觉效果 */
  box-sizing: border-box;
}

.list-one-item {
  display: grid;
  grid-template-columns:
  1fr        /* 序号 */
  2fr        /* 任务 key */
  2fr        /* cron 表达式 */
  2fr        /* 开始时间戳 */
  2fr        /* 结束时间戳 */
  1fr; /* 操作按钮 */

  /*text-align: center;*/
  align-items: center;
  gap: 10px;

  padding: 5px 5px; /* 减少垂直内边距 */
  border: 1px solid #eee;
  border-radius: 8px;
  background: #fafafa;
}

.one-item.header {
  text-align: center;
  background: #ead152;
  font-weight: 600;
  color: #2c3e50;
}

.actions {
  text-align: center;
  margin-top: 20px;
}

.copy-btn {
  margin-left: 10px;
  padding: 3px 8px;
  background-color: #3498db;
  color: white;
  border: none;
  height: 24px; /* 明确设置按钮高度 */
  border-radius: 5px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: background-color 0.3s ease;
}

.copy-btn:hover {
  background-color: #2980b9;
}

.file-upload-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 20px 0;
}

.file-upload-area {
  width: 100%;
  max-width: 400px;
  padding: 30px;
  border: 2px dashed #3498db;
  border-radius: 12px;
  text-align: center;
  background-color: #f9f9f9;
  transition: all 0.3s ease;
  cursor: pointer;
}

.file-upload-area:hover {
  background-color: #e3f2fd;
  border-color: #2980b9;
}

.file-input {
  display: none; /* 隐藏默认 input */
}

.upload-icon {
  font-size: 2rem;
  color: #3498db;
  margin-bottom: 10px;
}

.upload-text {
  font-size: 1rem;
  color: #2c3e50;
  margin: 0;
}

.file-info {
  font-size: 0.9rem;
  color: #7f8c8d;
  margin-top: 10px;
}
.fixed-footer {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000; /* 确保按钮在其他元素之上 */
}

.fixed-footer .btn {
  padding: 10px 15px;
  font-size: 1rem;
  background: rgba(52, 152, 219, 0.8); /* 半透明背景 */
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.fixed-footer .btn:hover {
  background: rgba(41, 128, 185, 1); /* 悬停时加深背景 */
  transform: scale(1.05);
}


</style>