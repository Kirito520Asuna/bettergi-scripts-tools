<script setup>
import {ref, computed, onMounted} from "vue";
import {goBack, toHomePage} from "@api/web/web.js";
import {CopyToClipboard} from "@utils/local.js";
import {preview1RemoteBat, browseDir} from "@api/generate/generate.js";
import {ElMessage} from "element-plus";
import {useRoute} from "vue-router";
import {getGithub1RemoteTags} from "@api/sys/sys.js";

const activeMenuId = ref(0)
const content = ref('')
const loadingPreview = ref(false)
const loadingDownload = ref(false)


const loadingCustomButton = ref({
  prop: undefined,
  button_key: undefined,
})

const remoteTags = ref([])
const loadingTags = ref(false)
const selectedTag = ref(null)

/** 打开自定义按钮弹窗 */
const handleCustomButton = async (menuKey) => {
  loadingCustomButton.value.button_key = menuKey
  selectedTag.value = null
  loadingTags.value = true
  try {
    remoteTags.value = await getGithub1RemoteTags()
    if (remoteTags.value.length > 0) selectedTag.value = remoteTags.value[0]
  } catch (e) {
    ElMessage.error('获取版本信息失败')
    remoteTags.value = []
  } finally {
    loadingTags.value = false
  }
}

const list = ref([
  {
    menu_id: 0,
    menu: '生成1Remote Bat',
    menu_key: '1Remote',
    formTitleList: [
      {
        label: '标题',
        prop: 'title',
        isText: true,
        placeholder: '例如: 自启动本地1Remote远程', // 通过映射加入占位符
      },
      {
        label: '目录',
        prop: 'startDir',
        isText: true,
        isDir: true,
        selectFile: true,
        accept:'.exe',/*'.exe,.bat,.cmd,.lnk'*/
        placeholder: '例如: D:\\Apps\\1Remote', // 通过映射加入占位符
        help: {
          title: '1Remote目录',
          desc: '',
          body: [
            {
              content: `请选择<code style="color: #4e61b2;">1Remote.exe</code>文件`,
              replaces:[]
            },
            {
              content: `当环境不支持获取完整路径时，请手动输入目录`,
              replaces:[]
            }
          ]
        },
      },
      {
        label: '执行文件',
        prop: 'exeName',
        isText: true,
        placeholder: '例如: 1Remote.exe', // 通过映射加入占位符
      },
      {
        label: 'ULID',
        isText: true,
        prop: 'startUlid',
        placeholder: '请输入ULID 例如:01Jxxxxxxxxxxxxx ',
        help: {
          title: 'ULID 获取步骤',
          desc: '无需额外操作，直接从已有的 1Remote 快捷方式中提取 ULID 及启动参数：',
          body: [
            {
              content: `在 1Remote 会话中创建桌面快捷方式；`,
              replaces:[
              ]
            },
            {
              content: `找到你已创建的 1Remote 快捷方式（桌面或文件夹中）；`,
              replaces:[
              ]
            },
            {
              content: `右键该快捷方式 → 点击「属性」；`,
              replaces:[
              ]
            },
            {
              content: `在弹出的属性窗口中，切换到「快捷方式」选项卡，找到「目标(T)」输入框；`,
              replaces:[
              ]
            },
            {
              content: `「目标」内容格式示例：<code>D:\\1Remote\\1.2.1\\net9\\x64\\1Remote.exe ULID:01Jxxxxxxxxxxxxx \\--start\\--minimized</code>；`,
              replaces:[
              ]
            },
            {
              content: `提取「目标」中 <code>ULID:xxx \\--start\\--minimized</code> 这一段（含 ULID 和最小化参数），复制备用。`,
              replaces:[]
            },
          ]
        },
      },
      {
        label: '等待时间',
        isNumber: true,
        prop: 'seconds',
        placeholder: '请输入等待时间',
      },
      {
        label: '文件名(.bat)',
        isText: true,
        prop: 'fileName',
        placeholder: '例如: startup.bat', // 按需补充
        help: {
          title: '自启动步骤',
          desc: '',
          body: [
            {
              content: `按下快捷键 <kbd>Win</kbd> + <kbd>R</kbd>，在弹出的运行窗口中输入 <code>shell:startup</code>，回车；`,
              replaces:[
              ]
            },
            {
              content: `将 <code>{fileName}</code> 移动/复制到上一步打开的文件夹中`,
              replaces:[
                {from: `/\\{fileName\\}/g`, toActiveItemProp: 'form.fileName'}
              ]
            }
          ]
        },
      },
    ],
    form: {
      title: '自启动本地1Remote远程',
      exeName: '1Remote.exe',
      seconds: 5,
      startDir: 'D:\\Apps\\1Remote',
      startUlid: '',
      fileName: 'startup.bat',
    },
    Business_Method: {
      async handleDownload(activeItem) {
        if (!content.value) {
         await this.handlePreview(activeItem)
        }
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

// 文件选择器引用
const fileInput = ref(null)

/** 点击“选择文件”按钮时触发隐藏 input 的 click，支持限制文件类型
 * @param {string} [accept] 可选，允许的文件类型，如 '.exe,.bat' 或 'image/*'，不传则无限制
 */
const handleSelectFile = (accept) => {
  if (fileInput.value) {
    // 动态设置 accept 属性
    if (accept !== undefined) {
      fileInput.value.accept = accept
    } else {
      fileInput.value.removeAttribute('accept') // 移除限制，允许所有文件
    }
    fileInput.value.click()
  }
}

/** 文件选择后的处理：提取绝对路径，拆分为目录和文件名 */
const onFileChange = (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  // Electron 环境：file.path 为完整绝对路径
  if (file.path) {
    const fullPath = file.path
    const lastBackslash = fullPath.lastIndexOf('\\')
    const lastSlash = fullPath.lastIndexOf('/')
    const splitIndex = Math.max(lastBackslash, lastSlash)

    if (splitIndex > 0) {
      activeItem.value.form.startDir = fullPath.substring(0, splitIndex)
      activeItem.value.form.exeName = fullPath.substring(splitIndex + 1)
    } else {
      activeItem.value.form.startDir = ''
      activeItem.value.form.exeName = fullPath
    }
  } else {
    // 非 Electron 环境：仅能拿到文件名，提示用户手动输入起始目录
    activeItem.value.form.exeName = file.name
    activeItem.value.form.startDir = ''
    ElMessage.warning('当前环境不支持获取完整路径，请手动输入目录')
  }

  // 清空 input，允许重复选择同一文件
  event.target.value = ''
}

/** 选择本地文件夹 - 弹窗浏览 */
const selectFolder = async (prop) => {
  folderDialogProp.value = prop
  folderCurrentPath.value = ''
  folderDirs.value = []
  showDialogFolder.value = true
  await loadDir('')
}
const showHelp = ref(false)

const HelpJson=ref({
  title: '',
  desc:'',
  body:[
  ]
})
const openGenericHelp = (helpData) => {
  // 深拷贝
  const processed = JSON.parse(JSON.stringify(helpData))
  // 将 body 中每一项都处理成最终字符串
  processed.body = processed.body.map(item => {
    if (typeof item === 'string') return item
    if (item.content) {
      return applyReplaces(item.content, item.replaces || [], activeItem.value)
    }
    return item
  })
  HelpJson.value = {
    title: processed.title,   // 注意顶层 title
    desc: processed.desc || '',
    body: processed.body
  }
  showHelp.value = true
}
/**
 * 根据 replaces 配置替换模板中的占位符
 * @param {string} template - 原始内容（包含占位符）
 * @param {Array} replaces - 替换规则数组
 * @param {Object} activeItem - 当前激活的菜单项
 * @returns {string} 替换后的内容
 */
function applyReplaces(template, replaces, activeItem) {
  if (!replaces || replaces.length === 0) return template

  let result = template
  replaces.forEach(rule => {
    // 解析正则字符串 "/pattern/flags"
    const match = rule.from.match(/^\/(.*)\/([gimsuy]*)$/)
    if (!match) {
      console.warn('无效的正则字符串:', rule.from)
      return
    }
    const [, pattern, flags] = match
    const regex = new RegExp(pattern, flags)

    // 从 activeItem 中取替换值
    const value = rule.toActiveItemProp
        .split('.')
        .reduce((obj, key) => obj?.[key], activeItem) ?? ''

    // 执行替换
    result = result.replace(regex, value)
  })
  return result
}

const showDialogFolder = ref(false)
const folderDialogProp = ref('')
const folderCurrentPath = ref('')
const folderDirs = ref([])
const folderDefault = {
  showDialog:false,
  dialogProp:'',
  currentPath:'',
  dirs:[]
}

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
  try {
    activeItem.value.form[folderDialogProp.value] = folderCurrentPath.value
    // showDialogFolder.value = false
  }finally {
    showDialogFolder.value = folderDefault.showDialog
    folderDialogProp.value = folderDefault.dialogProp
    folderCurrentPath.value = folderDefault.currentPath
    folderDirs.value = folderDefault.dirs
  }

}

// 格式化文件大小（字节 -> KB/MB）
const formatFileSize = (bytes) => {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
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
                  <!--todo:特制化 menu_key=='1Remote' 时加一个按键跳出弹窗, 弹窗加载 获取1Remote所有版本信息 接口 -->
                <button v-if="activeItem.menu_key === '1Remote'" class="action-btn" @click="handleCustomButton(activeItem.menu_key)">
                  1Remote 版本
                </button>
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
                <label class="form-label">{{ item.label }}
                  <span v-if="item.help" class="help-icon" @click="openGenericHelp(item.help)">?</span>
                </label>
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
                  <!-- 新增：仅为“起始目录”项显示“选择文件”按钮 -->
                  <el-button
                      v-if="item.isDir&&item.selectFile&&item.prop === 'startDir'"
                      @click="handleSelectFile(item.accept)"
                      class="btn-folder"
                      style="margin-left:4px;"
                  >选择文件
                  </el-button>
<!--                  <el-button
                      v-else-if="item.isDir"
                      @click="selectFolder(item.prop)"
                      class="btn-folder"
                  >选择
                  </el-button>-->
                </div>
              </div>
            </div>

          </div>
          <pre class="code-preview"><code>{{ content || '点击预览按钮加载内容' }}</code></pre>
        </div>
      </div>
    </div>

    <!-- 隐藏的文件选择器，用于 startDir 路径提取 -->
    <input
        ref="fileInput"
        type="file"
        style="display: none;"
        @change="onFileChange"
    />

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

    <div v-if="loadingCustomButton.button_key"
         class="dialog-overlay" @click.self="loadingCustomButton.button_key = ''">
      <div class="dialog-box dialog-split">
        <div class="dialog-header">
          <span>{{ loadingCustomButton.button_key }} 版本列表</span>
          <button class="dialog-close" @click="loadingCustomButton.button_key = ''">×</button>
        </div>
        <div class="dialog-split-body">
          <div class="dialog-split-left">
            <div v-if="loadingTags" class="dir-empty">加载中...</div>
            <div v-else-if="remoteTags.length === 0" class="dir-empty">暂无版本</div>
            <div v-else v-for="tag in remoteTags" :key="tag.name"
                 class="tag-item" :class="{ active: selectedTag?.name === tag.name }"
                 @click="selectedTag = tag">
              {{ tag.name }}
            </div>
          </div>
          <div class="dialog-split-right">
            <div v-if="!selectedTag" class="dir-empty">请选择左侧版本</div>
            <div v-else-if="!selectedTag.gitHubFileList || selectedTag.gitHubFileList.length === 0" class="dir-empty">该版本无文件</div>
            <div v-else v-for="file in selectedTag.gitHubFileList" :key="file.name" class="file-item">
              <div class="file-icon">📦</div>
              <div class="file-detail">
                <div class="file-name" :title="file.name">{{ file.name }}</div>
                <div class="file-actions">
                  <span class="file-size">{{ formatFileSize(file.size) }}</span>
                  <button class="file-btn" @click="window.open(file.downloadUrl, '_blank')">直接下载</button>
                  <button class="file-btn file-btn-proxy" @click="window.open(file.proxyDownloadUrl, '_blank')">代理下载</button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="action-btn" @click="loadingCustomButton.button_key = ''">关闭</button>
        </div>
      </div>
    </div>
    <!-- 通用帮助弹窗 -->
    <div v-if="showHelp" class="dialog-overlay" @click.self="showHelp = false">
      <div class="dialog-box help-dialog">
        <div class="dialog-header">
          <span>{{ HelpJson.title }}</span>
          <button class="dialog-close" @click="showHelp = false">×</button>
        </div>
        <div class="dialog-body">
          <div class="help-steps">
            <p v-if="HelpJson.desc&&HelpJson.desc!==''" style="margin:0 0 8px;color:rgba(255,255,255,0.6);font-size:13px;">
              {{ HelpJson.desc }}</p>
            <ol v-if="HelpJson.body&&HelpJson.body.length>0">
<!--              <li v-for="(item,index) in HelpJson.body" :key="index">{{ applyReplaces(item.content, item.replaces, activeItem) }}</li>-->
              <li v-for="(htmlStr, index) in HelpJson.body" :key="index">
                <span v-html="htmlStr"></span>
              </li>
            </ol>
          </div>
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
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
}

.help-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.3);
  color: rgba(102, 126, 234, 0.9);
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.help-icon:hover {
  background: rgba(102, 126, 234, 0.5);
  color: #fff;
}


.help-dialog {
  width: 520px;
}

.help-steps {
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
  line-height: 2;
}

.help-steps ol {
  margin: 0;
  padding-left: 20px;
}

.help-steps li {
  margin-bottom: 8px;
}

.help-steps kbd {
  display: inline-block;
  padding: 1px 7px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-family: inherit;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.95);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.help-steps code {
  padding: 2px 7px;
  border-radius: 4px;
  background: rgba(102, 126, 234, 0.15);
  color: rgba(130, 150, 255, 0.95);
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
}

.help-steps :deep(kbd) {
  display: inline-block;
  padding: 1px 7px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-family: inherit;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.95);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.help-steps :deep(code) {
  padding: 2px 7px;
  border-radius: 4px;
  background: rgba(102, 126, 234, 0.15);
  color: rgba(130, 150, 255, 0.95);
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
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

.dialog-split {
  width: 700px;
  height: 500px;
  max-height: 80vh;
}

.dialog-split-body {
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.dialog-split-left {
  width: 220px;
  min-width: 220px;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  overflow-y: auto;
  padding: 8px 0;
}

.dialog-split-right {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  padding: 8px 16px;
}

.tag-item {
  padding: 8px 16px;
  font-size: 13px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.7);
  transition: all 0.2s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tag-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.9);
}

.tag-item.active {
  background: rgba(102, 126, 234, 0.2);
  color: #fff;
  font-weight: 600;
  border-right: 2px solid rgba(102, 126, 234, 0.8);
}

.file-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  margin-bottom: 6px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  transition: background 0.2s;
}

.file-item:hover {
  background: rgba(255, 255, 255, 0.08);
}

.file-icon {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.file-detail {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
}

.file-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.file-size {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  flex-shrink: 0;
}

.file-btn {
  padding: 3px 10px;
  border-radius: 5px;
  font-size: 11px;
  text-decoration: none;
  flex-shrink: 0;
  transition: all 0.2s;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.7);
}

.file-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

.file-btn-proxy {
  background: rgba(102, 126, 234, 0.2);
  color: rgba(102, 126, 234, 0.9);
}

.file-btn-proxy:hover {
  background: rgba(102, 126, 234, 0.35);
  color: #fff;
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