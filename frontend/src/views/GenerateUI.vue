<script setup>
import {ref, computed, onMounted} from "vue";
import {goBack, toHomePage} from "@api/web/web.js";
import {CopyToClipboard} from "@utils/local.js";
import {preview1RemoteBat, browseDir} from "@api/generate/generate.js";
import {ElMessage} from "element-plus";
import {useRoute} from "vue-router";

const activeMenuId = ref(0)
const content = ref('')
const loadingPreview = ref(false)
const loadingDownload = ref(false)

const list = ref([
  {
    menu_id: 0,
    menu: '生成1Remote Bat',
    formTitleList: [
      {
        label: '标题',
        prop: 'title',
        isText: true,
        placeholder: '例如: 自启动本地1Remote远程', // 通过映射加入占位符
      },
      {
        label: '起始目录',
        prop: 'startDir',
        isText: true,
        isDir: true,
        placeholder: '例如: D:\\Apps\\1Remote', // 通过映射加入占位符
      },
      {
        label: '执行文件',
        prop: 'exeName',
        isText: true,
        placeholder: '例如: 1Remote.exe', // 通过映射加入占位符
      },
      {
        label: '起始Ulid',
        isText: true,
        prop: 'startUlid',
        placeholder: '请输入ULID',
      },
      {
        label: '等待时间',
        isNumber: true,
        prop: 'seconds',
        placeholder: '请输入等待时间',
      },
      {
        label: '文件名Bat',
        isText: true,
        prop: 'fileName',
        placeholder: '例如: startup.bat', // 按需补充
      },
    ],
    form: {
      title: '自启动本地1Remote远程',
      exeName: '1Remote.exe',
      seconds: 5,
      startDir: '',
      startUlid: '',
      fileName: 'startup.bat',
    },
    Business_Method: {
      async handleDownload(activeItem) {
        await activeItem.method.handleDownload(content.value, activeItem.form.fileName)
      },
      async handlePreview(activeItem) {
        const form = activeItem.form;
        try {
          const data = await activeItem.method.handlePreview(form.title, form.startDir, form.exeName, form.startUlid, form.seconds, form.fileName)
          content.value = data
        } catch (e) {
          content.value = '获取预览失败'
        }
      }
    },
    method: {
      async handleDownload(context, fileName) {
        await downloadBatFile(context, fileName)
      },
      async handlePreview(title, startDir, exeName, startUlid, seconds, fileName) {
        const data = await preview1RemoteBat(title, startDir, exeName, startUlid, seconds, fileName)
        return data
      }
    }
  }
])

const activeItem = computed(() => {
  return list.value.find(item => item.menu_id === activeMenuId.value) || list.value[0]
})

const selectMenu = (id) => {
  activeMenuId.value = id
  content.value = ''
}


/** 前端下载bat文件 */
const downloadBatFile = async (content, fileName) => {
  const blob = new Blob([content], {type: 'text/plain;charset=utf-8'})
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName
  a.click()
  URL.revokeObjectURL(url)
}


const handleDownloadMenu = async () => {
  loadingDownload.value = true
  try {
    const find = activeItem.value
    await find.Business_Method.handleDownload(find)
  } finally {
    loadingDownload.value = false
  }
}

/** 预览bat脚本内容 */
const handlePreviewMenu = async () => {
  loadingPreview.value = true
  try {
    const find = activeItem.value
    await find.Business_Method.handlePreview(find)
  } catch (e) {
    content.value = '获取预览失败'
  } finally {
    loadingPreview.value = false
  }
}

/** 选择本地文件夹 - 弹窗浏览 */
const selectFolder = async (prop) => {
  folderDialogProp.value = prop
  folderCurrentPath.value = ''
  folderDirs.value = []
  showDialogFolder.value = true
  await loadDir('')
}

const showDialogFolder = ref(false)
const folderDialogProp = ref('')
const folderCurrentPath = ref('')
const folderDirs = ref([])


/** 加载指定路径下的子目录 */
const loadDir = async (path) => {
  try {
    const data = await browseDir(path || undefined)
    folderCurrentPath.value = data.currentPath || ''
    folderDirs.value = data.dirs || []
  } catch (e) {
    ElMessage.error('获取文件夹失败')
  }
}

/** 返回上级目录 */
const folderGoUp = () => {
  const p = folderCurrentPath.value
  if (!p) return
  const lastSep = p.lastIndexOf('\\')
  const parent = lastSep <= 0 ? '' : p.substring(0, lastSep)
  loadDir(parent)
}

/** 确认选择当前目录 */
const folderConfirm = () => {
  if (!folderCurrentPath.value) {
    ElMessage.warning('请选择一个文件夹')
    return
  }
  activeItem.value.form[folderDialogProp.value] = folderCurrentPath.value
  showDialogFolder.value = false
}

const route = useRoute()

/** 复制到剪贴板 */
const handleCopy = () => {
  if (content.value) {
    CopyToClipboard(content.value)
  } else {
    ElMessage.warning('暂无可复制的内容')
  }
}

const goToHome = async () => {
  await toHomePage()
}
const goToBack = async () => {
  await goBack()
}

onMounted(async () => {
  for (let i = 0; i < list.value.length; i++) {
    list.value[i].menu_id = i
  }
})
</script>

<template>
  <div class="home">
    <div class="container">
      <h2 class="title">{{ route.meta.title || '生成器' }}</h2>
      <div class="main-container generate-layout">
        <div class="generate-sidebar">
          <div
              v-for="item in list"
              :key="item.menu_id"
              class="sidebar-menu-item"
              :class="{ active: activeMenuId === item.menu_id }"
              @click="selectMenu(item.menu_id)"
          >
            {{ item.menu }}
          </div>
        </div>
        <div class="generate-content">
          <div class="content-panel">
            <div class="content-header">
              <h3>{{ activeItem?.menu }}</h3>
              <div class="content-actions">
                <button class="action-btn" @click="handlePreviewMenu" :disabled="loadingPreview">
                  {{ loadingPreview ? '加载中...' : '预览' }}
                </button>
                <button class="action-btn" @click="handleCopy">复制</button>
                <button class="action-btn" @click="handleDownloadMenu" :disabled="loadingDownload">
                  {{ loadingDownload ? '下载中...' : '下载 ' + (activeItem?.form?.fileName || '') }}
                </button>
              </div>
            </div>
            <!-- 使用 v-for 动态生成表单行，一行两个 -->
            <div class="form-grid">
              <div
                  class="form-row"
                  v-for="item in activeItem.formTitleList"
                  :key="item.prop"
              >
                <label class="form-label">{{ item.label }}</label>
                <div style="display: flex; gap: 8px; flex: 1;">
                  <input v-if="item.isNumber"
                         class="form-input"
                         type="number"
                         v-model="activeItem.form[item.prop]"
                         :placeholder="item.placeholder || ''"
                  />
                  <input v-if="item.isText"
                         class="form-input"
                         v-model="activeItem.form[item.prop]"
                         :placeholder="item.placeholder || ''"
                  />
                  <el-button
                      v-if="item.isDir"
                      @click="selectFolder(item.prop)"
                      class="btn-folder"
                  >选择
                  </el-button>
                </div>
              </div>
            </div>

          </div>
          <pre class="code-preview"><code>{{ content || '点击预览按钮加载内容' }}</code></pre>
        </div>
      </div>
    </div>


    <!-- 文件夹选择弹窗 -->
    <div v-if="showDialogFolder" class="dialog-overlay" @click.self="showDialogFolder = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <span>选择文件夹</span>
          <button class="dialog-close" @click="showDialogFolder = false">×</button>
        </div>
        <div class="dialog-toolbar">
          <button class="action-btn" @click="folderGoUp">⬆ 上级</button>
          <span class="dialog-path">{{ folderCurrentPath || '根目录（盘符）' }}</span>
        </div>
        <div class="dialog-body">
          <div
              v-for="dir in folderDirs"
              :key="dir.path"
              class="dir-item"
              @dblclick="loadDir(dir.path)"
          >
            📁 {{ dir.name }}
          </div>
          <div v-if="folderDirs.length === 0" class="dir-empty">此目录为空</div>
        </div>
        <div class="dialog-footer">
          <button class="action-btn" @click="folderConfirm">选择此目录</button>
          <button class="action-btn" @click="showDialogFolder = false">取消</button>
        </div>
      </div>
    </div>


    <div class="fixed-back">
      <button class="btn secondary" @click="goToBack">返回上一页</button>
    </div>
    <div class="fixed-footer">
      <button class="btn secondary" @click="goToHome">🏠 返回主页</button>
    </div>
  </div>


</template>

<style scoped>
.title {
  flex: 1;
  min-height: 0;
  text-align: center;
  margin-bottom: 10px;
  font-size: 32px;
  color: transparent;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;

  box-shadow: 0 15px 35px rgba(102, 126, 234, 0.3);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
}

.main-container {
  flex: 200;
  min-height: 0;
}

.generate-layout {
  display: flex;
  flex-direction: row;
  gap: 16px;
  min-height: 60vh;
  max-height: 80vh;
  max-width: 90vw;
  width: 1000px;
}

.generate-sidebar {
  width: 200px;
  min-width: 200px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  overflow-y: auto;
}

.sidebar-menu-item {
  padding: 10px 14px;
  border-radius: 10px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  transition: all 0.25s ease;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-menu-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.9);
}

.sidebar-menu-item.active {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  font-weight: 600;
}

.generate-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.content-panel {
  color: rgba(255, 255, 255, 0.85);
  flex-shrink: 0;
  min-height: 0;
  overflow-y: auto;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 4px 0 12px;
}

.content-header h3 {
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
}

.content-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 14px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.action-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.35);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 16px;
  margin-bottom: 10px;
}

.form-label {
  min-width: 64px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  text-align: right;
}

.form-input {
  flex: 1;
  padding: 7px 12px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(0, 0, 0, 0.25);
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
  outline: none;
  transition: border-color 0.25s ease;
}

.form-input:focus {
  border-color: rgba(255, 255, 255, 0.4);
}

.form-input::placeholder {
  color: rgba(255, 255, 255, 0.3);
}

.code-preview {
  margin: 0;
  padding: 16px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.08);
  overflow: auto;
  flex: 1;
  min-height: 0;
  font-size: 13px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.8);
  white-space: pre-wrap;
  word-break: break-all;
}

.code-preview code {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}


.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-box {
  background: #1e1e2e;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  width: 500px;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
  color: rgba(255, 255, 255, 0.9);
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  font-weight: 600;
}

.dialog-close {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.6);
  font-size: 20px;
  cursor: pointer;
}

.dialog-close:hover {
  color: #fff;
}

.dialog-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.dialog-path {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.dialog-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px 16px;
  max-height: 400px;
}

.dir-item {
  padding: 6px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s;
}

.dir-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.dir-empty {
  text-align: center;
  color: rgba(255, 255, 255, 0.3);
  padding: 20px;
  font-size: 13px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}


@media (max-width: 1024px) {
  .generate-layout {
    max-width: 95vw;
    width: auto;
  }
}

@media (max-width: 768px) {
  .home {
    height: auto;
    min-height: 100vh;
  }

  .container {
    padding: 8px;
  }

  .title {
    font-size: 22px;
    padding: 20px;
  }

  .generate-layout {
    flex-direction: column;
    max-width: 100vw;
    width: 100%;
    height: auto;
  }

  .generate-sidebar {
    width: 100%;
    min-width: unset;
    flex-direction: row;
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    overflow-x: auto;
    padding: 8px;
  }

  .sidebar-menu-item {
    flex-shrink: 0;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .content-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .content-actions {
    flex-wrap: wrap;
  }

  .dialog-box {
    width: 90vw;
    max-height: 80vh;
  }
}

@media (max-width: 480px) {
  .title {
    font-size: 18px;
    padding: 12px;
  }

  .form-label {
    min-width: 48px;
    font-size: 12px;
  }

  .form-input {
    font-size: 12px;
    padding: 6px 8px;
  }

  .action-btn {
    padding: 5px 10px;
    font-size: 12px;
  }

  .code-preview {
    font-size: 11px;
    padding: 10px;
  }
}
</style>