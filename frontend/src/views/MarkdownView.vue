<!-- MarkdownViewer.vue -->
<template>
  <div class="home">
    <div class="markdown-container">
      <!-- 上传区域 -->
      <div
          v-if="!markdownContent"
          class="upload-area"
          :class="{ 'drag-over': isDragging }"
          @dragover.prevent="handleDragOver"
          @dragenter.prevent="handleDragOver"
          @dragleave.prevent="handleDragLeave"
          @drop.prevent="handleDrop"
      >
        <input
            type="file"
            accept=".md,.markdown"
            @change="handleFileChange"
            id="md-upload"
            hidden
        />
        <label for="md-upload" class="upload-btn">
          <div v-if="isDragging">松开鼠标 → 立即上传 Markdown 文件</div>
          <div v-else>点击上传 .md / .markdown 文件<br>或拖拽文件到此处</div>
        </label>
      </div>

      <!-- 渲染结果 -->
      <div v-else class="rendered-content" v-html="renderedHtml"></div>

      <!-- 加载中 / 错误提示 -->
      <div v-if="loading" class="loading">读取文件中...</div>
      <div v-if="error" class="error">{{ error }}</div>
    </div>
    <div class="fixed-footer">
      <button @click="goToHome" class="btn secondary">🏠 返回主页</button>
    </div>
  </div>

</template>

<script setup>
import {ref, computed, watch, onMounted, onUnmounted, nextTick} from 'vue'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.min.css'
import mermaid from 'mermaid'
import router from "@router/router.js";
import {toHomePage} from "@api/web/web.js";
// 在 script 中添加跳转逻辑
const goToHome = async () => {
  // router.push('/'); // 假设主页路径是 '/'
  await toHomePage()
};


// ================== Markdown 配置 ==================
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true,
  // 显式开启表格（其实默认已开）
  table: true,

  highlight: function (str, lang) {
    // Mermaid 处理（保持原样）
    if (lang && lang.toLowerCase() === 'mermaid') {
      return `<div class="mermaid">${md.utils.escapeHtml(str.trim())}</div>`;
    }

    // 其他语言处理
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
            hljs.highlight(str, {language: lang, ignoreIllegals: true}).value +
            '</code></pre>';
      } catch (e) {
      }
    }

    // 默认 fallback：保持原始格式，用 pre code 包裹
    return md.utils.escapeHtml(str.trim());
  }
})

// ================== Mermaid 配置与自动渲染 ==================
const mermaidObserver = ref(null)

const initMermaid = async () => {
  // 只初始化一次
  mermaid.initialize({
    startOnLoad: false,
    theme: 'neutral',  // ← 改成 neutral（中性，最适配自定义 CSS）
    // 或 'default' 如果你想完全靠 CSS 覆盖
    themeVariables: {
      primaryColor: '#2d3748',      // 节点背景深灰
      primaryTextColor: '#f1f5f9',  // 文字浅色
      lineColor: '#94a3b8'
    },
    securityLevel: 'strict',
    flowchart: {useMaxWidth: true},
    sequence: {useMaxWidth: true}
  })

  // 断开旧的 observer
  if (mermaidObserver.value) {
    mermaidObserver.value.disconnect()
  }

  await nextTick()

  const container = document.querySelector('.markdown-container')
  if (!container) return

  // 创建新的 MutationObserver
  mermaidObserver.value = new MutationObserver(async () => {
    const mermaidEls = container.querySelectorAll('.mermaid:not([data-processed])')
    if (mermaidEls.length > 0) {
      try {
        await mermaid.run({nodes: mermaidEls})
        mermaidEls.forEach(el => el.setAttribute('data-processed', 'true'))
      } catch (err) {
        console.warn('Mermaid 渲染失败:', err)
      }
    }
  })

  mermaidObserver.value.observe(container, {
    childList: true,
    subtree: true
  })

  // 立即尝试渲染已存在的 mermaid 块
  const initialEls = container.querySelectorAll('.mermaid:not([data-processed])')
  if (initialEls.length > 0) {
    try {
      await mermaid.run({nodes: initialEls})
      initialEls.forEach(el => el.setAttribute('data-processed', 'true'))
    } catch (err) {
      console.warn('Mermaid 首次渲染失败:', err)
    }
  }
}

// ================== 状态 ==================
const markdownContent = ref('')
const loading = ref(false)
const error = ref('')
const isDragging = ref(false)

// 安全渲染后的 HTML
const renderedHtml = computed(() => {
  if (!markdownContent.value) return ''
  const rawHtml = md.render(markdownContent.value)
  return DOMPurify.sanitize(rawHtml)
})

// ================== 文件上传处理 ==================
function handleFileChange(e) {
  const file = e.target?.files?.[0]
  if (!file) return
  processFile(file)
}

function handleDrop(e) {
  e.preventDefault()
  isDragging.value = false
  const file = e.dataTransfer?.files?.[0]
  if (file) {
    processFile(file)
  }
}

function handleDragOver() {
  isDragging.value = true
}

function handleDragLeave() {
  isDragging.value = false
}

function processFile(file) {
  if (!/\.(md|markdown)$/i.test(file.name)) {
    error.value = '仅支持 .md 或 .markdown 文件'
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    error.value = '文件太大（上限 5MB）'
    return
  }

  error.value = ''
  loading.value = true

  const reader = new FileReader()
  reader.onload = (ev) => {
    markdownContent.value = ev.target?.result || ''
    loading.value = false
  }
  reader.onerror = () => {
    error.value = '文件读取失败，请重试'
    loading.value = false
  }

  reader.readAsText(file)
}

// ================== 生命周期钩子 ==================
onMounted(() => {
  nextTick(() => {
    initMermaid()
  })
})

watch(markdownContent, () => {
  nextTick(() => {
    initMermaid()
  })
})

onUnmounted(() => {
  if (mermaidObserver.value) {
    mermaidObserver.value.disconnect()
    mermaidObserver.value = null
  }
})
</script>

<style scoped>
:root {
  --page-bg-light: url("@assets/MHY_XTLL.png");
  --page-bg-dark: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  --container-bg-light: #fadbd8;
  --container-bg-dark: rgba(45, 55, 72, 0.8);
  --upload-bg-light: #fafafa;
  --upload-bg-dark: rgba(30, 41, 59, 0.6);
  --upload-border-light: #bbb;
  --upload-border-dark: rgba(99, 179, 237, 0.5);
  --code-bg-light: #ececea;
  --code-bg-dark: rgba(30, 41, 59, 0.8);
  --text-primary-light: #2c3e50;
  --text-primary-dark: #e2e8f0;
  --table-header-light: rgb(177, 182, 189);
  --table-header-dark: rgba(75, 85, 99, 0.9);
}

.home {
  min-height: 100vh;
  align-items: center;
  justify-content: center;
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

.markdown-container {
  padding: 20px;
  max-width: 60%;
  margin: 0 auto;
  background: var(--container-bg-light);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .markdown-container {
    background: var(--container-bg-dark);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  }
}

.upload-area {
  border: 2px dashed var(--upload-border-light);
  border-radius: 12px;
  padding: 80px 24px;
  text-align: center;
  background: var(--upload-bg-light);
  transition: all 0.25s ease;
  min-height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (prefers-color-scheme: dark) {
  .upload-area {
    background: var(--upload-bg-dark);
    border-color: var(--upload-border-dark);
  }
}

.upload-area.drag-over {
  border-color: #409eff;
  background: #ecf5ff;
  border-width: 3px;
  box-shadow: 0 0 12px rgba(64, 158, 255, 0.3);
}

@media (prefers-color-scheme: dark) {
  .upload-area.drag-over {
    background: rgba(50, 60, 80, 0.8);
  }
}

.upload-btn {
  cursor: pointer;
  font-size: 1.15rem;
  color: #409eff;
  line-height: 1.6;
  user-select: none;
}

.upload-btn div {
  pointer-events: none;
}

.rendered-content {
  line-height: 1.8;
  color: var(--text-primary-light);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .rendered-content {
    color: var(--text-primary-dark);
  }
}

.rendered-content :deep(pre),
.rendered-content :deep(pre code),
.rendered-content :deep(code) {
  white-space: pre-wrap !important;
  margin: 0.4em 0 !important;
  padding: 0.1em 0.5em !important;
  background: var(--code-bg-light);
  border-radius: 10px;
  font-size: 0.94em;
  color: rgb(255, 0, 107);
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace !important;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .rendered-content :deep(pre),
  .rendered-content :deep(pre code),
  .rendered-content :deep(code) {
    background: var(--code-bg-dark);
    color: #fbb6ce;
  }
}

.rendered-content :deep(.hljs) {
  color: rgb(186, 112, 7);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .rendered-content :deep(.hljs) {
    color: #f6ad55;
  }
}

.loading,
.error {
  text-align: center;
  padding: 60px 20px;
  font-size: 1.1rem;
}

.loading {
  color: #909399;
}

@media (prefers-color-scheme: dark) {
  .loading {
    color: #a0aec0;
  }
}

.error {
  color: #f56c6c;
}

@media (prefers-color-scheme: dark) {
  .error {
    color: #fc8181;
  }
}

/* Mermaid 相关样式 */
.rendered-content :deep(.mermaid) {
  margin: 2rem 0;
  padding: 1rem;
  background: transparent;
  border-radius: 8px;
  overflow: auto;
}

.rendered-content :deep(.mermaid svg) {
  background: transparent !important;
  max-width: 100%;
  height: auto;
  display: block;
}

.rendered-content :deep(.mermaid .actor rect),
.rendered-content :deep(.mermaid g rect.actor) {
  fill: #f1f5f9 !important;
  stroke: #cbd5e1 !important;
  stroke-width: 1.8px !important;
}

@media (prefers-color-scheme: dark) {
  .rendered-content :deep(.mermaid .actor rect),
  .rendered-content :deep(.mermaid g rect.actor) {
    fill: #374151 !important;
    stroke: #6b7280 !important;
  }
}

.rendered-content :deep(.mermaid .messageText tspan),
.rendered-content :deep(.mermaid .messageText text),
.rendered-content :deep(.mermaid .signalText) {
  fill: #111827 !important;
  font-weight: normal !important;
  stroke: none !important;
  font-size: 14px !important;
}

@media (prefers-color-scheme: dark) {
  .rendered-content :deep(.mermaid .messageText tspan),
  .rendered-content :deep(.mermaid .messageText text),
  .rendered-content :deep(.mermaid .signalText) {
    fill: #f3f4f6 !important;
  }
}

.rendered-content :deep(.mermaid .actor text) {
  fill: #0f172a !important;
  font-weight: normal !important;
  stroke: none !important;
}

@media (prefers-color-scheme: dark) {
  .rendered-content :deep(.mermaid .actor text) {
    fill: #f9fafb !important;
  }
}

.rendered-content :deep(.mermaid .note rect) {
  fill: #fefce8 !important;
  stroke: #ca8a04 !important;
  stroke-width: 1.5px !important;
}

@media (prefers-color-scheme: dark) {
  .rendered-content :deep(.mermaid .note rect) {
    fill: #422006 !important;
    stroke: #fbbf24 !important;
  }
}

.rendered-content :deep(.mermaid .note text) {
  fill: #713f12 !important;
  font-weight: normal !important;
}

@media (prefers-color-scheme: dark) {
  .rendered-content :deep(.mermaid .note text) {
    fill: #fcd34d !important;
  }
}

.rendered-content :deep(.mermaid .messageLine0 path),
.rendered-content :deep(.mermaid .messageLine1 path),
.rendered-content :deep(.mermaid .edgePath path) {
  stroke: #64748b !important;
  stroke-width: 1.4px !important;
}

@media (prefers-color-scheme: dark) {
  .rendered-content :deep(.mermaid .messageLine0 path),
  .rendered-content :deep(.mermaid .messageLine1 path),
  .rendered-content :deep(.mermaid .edgePath path) {
    stroke: #9ca3af !important;
  }
}

.rendered-content :deep(.mermaid marker path) {
  fill: #64748b !important;
}

@media (prefers-color-scheme: dark) {
  .rendered-content :deep(.mermaid marker path) {
    fill: #9ca3af !important;
  }
}

.rendered-content :deep(.mermaid .sequence-number) {
  fill: #6b7280 !important;
  font-weight: normal !important;
}

@media (prefers-color-scheme: dark) {
  .rendered-content :deep(.mermaid .sequence-number) {
    fill: #d1d5db !important;
  }
}

/* Markdown 表格样式 */
.rendered-content :deep(table) {
  max-width: 80%;
  border-collapse: separate;
  border-spacing: 0;
  margin: 1.5em 0;
  font-size: 0.95em;
  overflow-x: auto;
  display: block;
  border-radius: 7px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .rendered-content :deep(table) {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  }
}

.rendered-content :deep(table thead th),
.rendered-content :deep(table thead td) {
  background: var(--table-header-light);
  color: #1e293b;
  text-align: left;
  padding: 0.75em 1em;
  border-bottom: 1px solid #000000;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .rendered-content :deep(table thead th),
  .rendered-content :deep(table thead td) {
    background: var(--table-header-dark);
    color: #e2e8f0;
    border-bottom-color: #9ca3af;
  }
}

.rendered-content :deep(table tbody tr) {
  border-bottom: 1px solid #000000;
  transition: background 0.2s;
  background: linear-gradient(135deg, rgb(198, 51, 159), rgb(255, 255, 255)) !important;
}

@media (prefers-color-scheme: dark) {
  .rendered-content :deep(table tbody tr) {
    border-bottom-color: #9ca3af;
    background: linear-gradient(135deg, rgba(198, 51, 159, 0.4), rgba(45, 55, 72, 0.6)) !important;
  }
}

.rendered-content :deep(table tbody tr:nth-child(odd)) {
  background: linear-gradient(135deg, rgb(193, 154, 57), rgb(255, 255, 255)) !important;
}

@media (prefers-color-scheme: dark) {
  .rendered-content :deep(table tbody tr:nth-child(odd)) {
    background: linear-gradient(135deg, rgba(193, 154, 57, 0.3), rgba(45, 55, 72, 0.6)) !important;
  }
}

.rendered-content :deep(table tbody tr:hover) {
  background: linear-gradient(135deg, rgb(0, 248, 255), rgb(255, 255, 255)) !important;
}

@media (prefers-color-scheme: dark) {
  .rendered-content :deep(table tbody tr:hover) {
    background: linear-gradient(135deg, rgba(0, 248, 255, 0.3), rgba(45, 55, 72, 0.8)) !important;
  }
}

.rendered-content :deep(table td),
.rendered-content :deep(table th) {
  padding: 0.75em 1em;
  vertical-align: top;
  border: 1px solid #e2e8f0;
  color: var(--text-primary-light);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .rendered-content :deep(table td),
  .rendered-content :deep(table th) {
    border-color: #4a5568;
    color: var(--text-primary-dark);
  }
}

.rendered-content :deep(table th.align-center),
.rendered-content :deep(table td.align-center) {
  text-align: center;
}

.rendered-content :deep(table th.align-right),
.rendered-content :deep(table td.align-right) {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .markdown-container {
    max-width: 95%;
    padding: 15px;
  }

  .upload-area {
    padding: 50px 20px;
    min-height: 180px;
  }

  .upload-btn {
    font-size: 1rem;
  }

  .rendered-content :deep(table) {
    max-width: 100%;
    font-size: 0.9em;
  }

  .rendered-content :deep(table td),
  .rendered-content :deep(table th) {
    padding: 0.6em 0.8em;
    font-size: 0.85rem;
  }

  .rendered-content :deep(pre),
  .rendered-content :deep(pre code),
  .rendered-content :deep(code) {
    font-size: 0.85em;
    padding: 0.1em 0.4em;
  }

  .rendered-content :deep(.mermaid) {
    padding: 0.5rem;
    margin: 1rem 0;
  }

  .rendered-content :deep(.mermaid .messageText tspan),
  .rendered-content :deep(.mermaid .messageText text),
  .rendered-content :deep(.mermaid .signalText),
  .rendered-content :deep(.mermaid .actor text) {
    font-size: 12px !important;
  }
}

@media (max-width: 480px) {
  .markdown-container {
    max-width: 98%;
    padding: 12px;
    border-radius: 8px;
  }

  .upload-area {
    padding: 40px 15px;
    min-height: 150px;
    border-radius: 8px;
  }

  .upload-btn {
    font-size: 0.95rem;
  }

  .rendered-content :deep(table) {
    font-size: 0.85em;
  }

  .rendered-content :deep(table td),
  .rendered-content :deep(table th) {
    padding: 0.5em 0.6em;
    font-size: 0.8rem;
  }

  .rendered-content :deep(pre),
  .rendered-content :deep(pre code),
  .rendered-content :deep(code) {
    font-size: 0.8em;
    border-radius: 6px;
  }

  .rendered-content {
    line-height: 1.6;
  }

  .rendered-content :deep(h1) {
    font-size: 1.5rem;
  }

  .rendered-content :deep(h2) {
    font-size: 1.3rem;
  }

  .rendered-content :deep(h3) {
    font-size: 1.1rem;
  }
}

/* 横屏手机适配 */
@media (max-width: 768px) and (orientation: landscape) {
  .home {
    background-position: center top;
  }

  .markdown-container {
    max-height: 90vh;
    overflow-y: auto;
    margin: 20px auto;
  }

  .upload-area {
    min-height: 120px;
    padding: 30px 15px;
  }

  .rendered-content :deep(.mermaid) {
    max-height: 60vh;
  }
}
</style>
