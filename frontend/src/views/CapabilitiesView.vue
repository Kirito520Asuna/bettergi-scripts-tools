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
    <div class="fixed-back">
      <button @click="goToBack" class="btn secondary">返回上一页</button>
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
import {getNextTimestamp, getNextTimestampAll} from "@api/cron/cron.js";
import {ocrBytes} from "@api/ocr/ocr.js";
import {CopyToClipboard} from "@utils/local.js";
import {goBack, toHomePage} from "@api/web/web.js";

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
const goToBack = async () => {
  await goBack();
}
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
@import "@css/capabilities.css";
</style>
