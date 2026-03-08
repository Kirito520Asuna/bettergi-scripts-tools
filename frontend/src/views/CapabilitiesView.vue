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
import {toHomePage} from "@api/web/web.js";

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
const goToHome = async () => {
  // router.push('/'); // 假设主页路径是 '/'
  await toHomePage()
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
:root {
  --page-bg-light: url("@assets/MHY_XTLL.png");
  --page-bg-dark: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  --container-bg-light: linear-gradient(135deg, #f5f7fa, #e4edf9);
  --container-bg-dark: linear-gradient(135deg, #1e2a3a, #2d3748);
  --card-bg-light: #ffffff;
  --card-bg-dark: rgba(45, 55, 72, 0.8);
  --text-primary-light: #2c3e50;
  --text-primary-dark: #e2e8f0;
  --text-secondary-light: #3498db;
  --text-secondary-dark: #63b3ed;
  --input-bg-light: #ffffff;
  --input-bg-dark: rgba(30, 41, 59, 0.8);
  --input-border-light: #ddd;
  --input-border-dark: rgba(99, 179, 237, 0.3);
  --item-bg-light: #fafafa;
  --item-bg-dark: rgba(50, 60, 80, 0.6);
  --header-bg-light: #ead152;
  --header-bg-dark: rgba(234, 209, 82, 0.3);
  --result-bg-light: linear-gradient(135deg, #ddb568, #ffffff);
  --result-bg-dark: linear-gradient(135deg, #4a5568, #2d3748);
  --list-item-bg-light: linear-gradient(135deg, #c22dd1, #91dcd6);
  --list-item-bg-dark: linear-gradient(135deg, #6b21a3, #3184ce);
}

.home {
  min-height: 100vh;
  background: var(--page-bg-light);
  background-attachment: fixed;
  background-size: cover;
  background-position: center;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .home {
    background: var(--page-bg-dark);
  }
}

.container {
  min-width: 70%;
  margin: 0 auto;
  padding: 20px;
  background: var(--container-bg-light);
  min-height: 100vh;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .container {
    background: var(--container-bg-dark);
  }
}

.title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 30px;
  color: var(--text-primary-light);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .title {
    color: var(--text-primary-dark);
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  }
}

.card {
  background: var(--card-bg-light);
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

@media (prefers-color-scheme: dark) {
  .card {
    background: var(--card-bg-dark);
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3);
    border-color: rgba(255, 255, 255, 0.1);
  }
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}

@media (prefers-color-scheme: dark) {
  .card:hover {
    box-shadow: 0 10px 25px rgba(99, 179, 237, 0.2);
  }
}

.section-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 20px;
  color: var(--text-secondary-light);
  text-align: center;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .section-title {
    color: var(--text-secondary-dark);
  }
}

.form-group {
  margin-bottom: 20px;
}

.label {
  display: block;
  font-size: 1rem;
  font-weight: 500;
  margin-bottom: 8px;
  color: var(--text-primary-light);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .label {
    color: var(--text-primary-dark);
  }
}

.input {
  width: 80%;
  padding: 12px 15px;
  border: 1px solid var(--input-border-light);
  border-radius: 8px;
  font-size: 1rem;
  background: var(--input-bg-light);
  color: var(--text-primary-light);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .input {
    background: var(--input-bg-dark);
    border-color: var(--input-border-dark);
    color: var(--text-primary-dark);
  }
}

.input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 5px rgba(52, 152, 219, 0.3);
}

@media (prefers-color-scheme: dark) {
  .input:focus {
    border-color: #63b3ed;
    box-shadow: 0 0 5px rgba(99, 179, 237, 0.4);
  }
}

.input.small {
  width: 150px;
  display: inline-block;
  margin-right: 10px;
}

.btn.danger {
  width: 80px;
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

.btn.danger {
  background: #e74c3c;
  color: white;
}

.btn.danger:hover {
  background: #c0392b;
  transform: scale(1.05);
}

.file-input {
  margin-bottom: 15px;
  padding: 10px;
  border: 1px dashed var(--text-secondary-light);
  border-radius: 8px;
  width: 100%;
  cursor: pointer;
  background: var(--input-bg-light);
  color: var(--text-primary-light);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .file-input {
    background: var(--input-bg-dark);
    color: var(--text-primary-dark);
    border-color: var(--text-secondary-dark);
  }
}

.file-input:hover {
  background: #f1f8ff;
}

@media (prefers-color-scheme: dark) {
  .file-input:hover {
    background: rgba(50, 60, 80, 0.8);
  }
}

.result-all {
  display: grid;
  grid-template-columns:
  8fr
  auto;
  align-items: center;
}

.result {
  background: var(--result-bg-light);
  padding: 15px;
  border-radius: 8px;
  margin-top: 15px;
  white-space: pre-wrap;
  font-family: monospace;
  font-size: 0.9rem;
  color: rgb(230, 0, 103);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .result {
    background: var(--result-bg-dark);
    color: #fbb6ce;
  }
}

.one-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 10px;
  background: var(--item-bg-light);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .one-item {
    background: var(--item-bg-dark);
    border-color: rgba(255, 255, 255, 0.1);
  }
}

.label {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.list-item {
  background: var(--list-item-bg-light) !important;
  border-radius: 12px !important;
  padding: 10px !important;
  box-sizing: border-box;
}

@media (prefers-color-scheme: dark) {
  .list-item {
    background: var(--list-item-bg-dark) !important;
  }
}

.list-one-item {
  display: grid;
  grid-template-columns:
  1fr
  2fr
  2fr
  2fr
  2fr
  1fr;
  align-items: center;
  gap: 10px;
  padding: 5px 5px;
  border: 1px solid #eee;
  border-radius: 8px;
  background: var(--item-bg-light);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .list-one-item {
    background: var(--item-bg-dark);
    border-color: rgba(255, 255, 255, 0.1);
  }
}

.one-item.header {
  text-align: center;
  background: var(--header-bg-light);
  font-weight: 600;
  color: var(--text-primary-light);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .one-item.header {
    background: var(--header-bg-dark);
    color: var(--text-primary-dark);
  }
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
  height: 24px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.3s ease;
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
  border: 2px dashed var(--text-secondary-light);
  border-radius: 12px;
  text-align: center;
  background-color: var(--input-bg-light);
  transition: all 0.3s ease;
  cursor: pointer;
}

@media (prefers-color-scheme: dark) {
  .file-upload-area {
    background-color: var(--input-bg-dark);
    border-color: var(--text-secondary-dark);
  }
}

.file-upload-area:hover {
  background-color: #e3f2fd;
  border-color: #2980b9;
}

@media (prefers-color-scheme: dark) {
  .file-upload-area:hover {
    background-color: rgba(50, 60, 80, 0.8);
    border-color: #63b3ed;
  }
}

.file-input {
  display: none;
}

.upload-icon {
  font-size: 2rem;
  color: var(--text-secondary-light);
  margin-bottom: 10px;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .upload-icon {
    color: var(--text-secondary-dark);
  }
}

.upload-text {
  font-size: 1rem;
  color: var(--text-primary-light);
  margin: 0;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .upload-text {
    color: var(--text-primary-dark);
  }
}

.file-info {
  font-size: 0.9rem;
  color: #7f8c8d;
  margin-top: 10px;
}

@media (prefers-color-scheme: dark) {
  .file-info {
    color: #a0aec0;
  }
}

.fixed-footer {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
}

.fixed-footer .btn {
  padding: 10px 15px;
  font-size: 1rem;
  background: rgba(52, 152, 219, 0.8);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .fixed-footer .btn {
    background: rgba(99, 179, 237, 0.8);
  }
}

.fixed-footer .btn:hover {
  background: rgba(41, 128, 185, 1);
  transform: scale(1.05);
}

@media (prefers-color-scheme: dark) {
  .fixed-footer .btn:hover {
    background: rgba(9, 132, 227, 1);
  }
}

/* 手机端适配 */
@media (max-width: 768px) {
  .container {
    min-width: 95%;
    padding: 15px;
  }

  .title {
    font-size: 1.8rem;
    margin-bottom: 20px;
  }

  .section-title {
    font-size: 1.4rem;
    margin-bottom: 15px;
  }

  .card {
    padding: 20px 15px;
    margin-bottom: 20px;
  }

  .input {
    width: 100%;
    font-size: 14px;
    padding: 10px 12px;
  }

  .input.small {
    width: 120px;
  }

  .btn {
    padding: 10px 15px;
    font-size: 0.9rem;
    margin-right: 8px;
    margin-bottom: 8px;
  }

  .btn.danger {
    width: auto;
    padding: 10px 15px;
  }

  .form-group {
    margin-bottom: 15px;
  }

  .field {
    margin-right: 10px;
  }

  .list-one-item {
    grid-template-columns: 1fr;
    gap: 8px;
    text-align: left;
  }

  .one-item {
    flex-direction: column;
    align-items: stretch;
    padding: 12px;
  }

  .label {
    white-space: normal;
  }

  .result-all {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .copy-btn {
    width: 100%;
    margin-left: 0;
    height: 32px;
  }

  .file-upload-area {
    padding: 20px;
    max-width: 100%;
  }

  .upload-icon {
    font-size: 1.5rem;
  }

  .upload-text {
    font-size: 0.9rem;
  }

  .fixed-footer {
    bottom: 10px;
    right: 10px;
  }

  .fixed-footer .btn {
    padding: 8px 12px;
    font-size: 0.85rem;
  }
}

@media (max-width: 480px) {
  .container {
    min-width: 98%;
    padding: 10px;
  }

  .title {
    font-size: 1.5rem;
  }

  .section-title {
    font-size: 1.2rem;
  }

  .card {
    padding: 15px 12px;
  }

  .input {
    font-size: 13px;
    padding: 8px 10px;
  }

  .input.small {
    width: 100px;
  }

  .btn {
    padding: 8px 12px;
    font-size: 0.85rem;
    border-radius: 6px;
  }

  .list-one-item {
    padding: 8px;
  }

  .one-item {
    padding: 10px;
  }

  .result {
    font-size: 0.8rem;
    padding: 10px;
  }

  .fixed-footer .btn {
    padding: 6px 10px;
    font-size: 0.8rem;
    border-radius: 6px;
  }
}

/* 横屏手机适配 */
@media (max-width: 768px) and (orientation: landscape) {
  .container {
    max-height: 90vh;
    overflow-y: auto;
    padding-bottom: 60px;
  }

  .title {
    font-size: 1.6rem;
    margin-bottom: 15px;
  }

  .card {
    margin-bottom: 15px;
  }

  .form-group {
    margin-bottom: 10px;
  }
}
</style>
