<!-- MarkdownViewer.vue -->
<template>
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
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.min.css'
import mermaid from 'mermaid'

// ================== Markdown 配置 ==================
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true,
/*  highlight: function (str, lang) {
    // 特殊处理 Mermaid 代码块
    if (lang && lang.toLowerCase() === 'mermaid') {
      return `<div class="mermaid">${md.utils.escapeHtml(str.trim())}</div>`
    }
    // 普通代码高亮
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
            hljs.highlight(str, {language: lang, ignoreIllegals: true}).value +
            '</code></pre>';
      } catch (__) {
      }
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>';
  }*/

  highlight: function (str, lang) {
    // Mermaid 处理（保持原样）
    if (lang && lang.toLowerCase() === 'mermaid') {
      return `<div class="mermaid">${md.utils.escapeHtml(str.trim())}</div>`;
    }

    // 其他语言处理
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
            hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
            '</code></pre>';
      } catch (e) {}
    }

    // 默认 fallback：保持原始格式，用 pre code 包裹
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str.trim()) + '</code></pre>';
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
    flowchart: { useMaxWidth: true },
    sequence: { useMaxWidth: true }
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
        await mermaid.run({ nodes: mermaidEls })
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
      await mermaid.run({ nodes: initialEls })
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
.markdown-container {
  padding: 20px;
  max-width: 960px;
  margin: 0 auto;
  background: linear-gradient(135deg, rgba(84, 197, 243, 0.67), rgba(86, 197, 204, 0.66)) !important; /* 添加渐变背景 */
}

.upload-area {
  border: 2px dashed #bbb;
  border-radius: 12px;
  padding: 80px 24px;
  text-align: center;
  background: #fafafa;
  transition: all 0.25s ease;
  min-height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-area.drag-over {
  border-color: #409eff;
  background: #ecf5ff;
  border-width: 3px;
  box-shadow: 0 0 12px rgba(64, 158, 255, 0.3);
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
  /*line-height: 2.7;*/
}

.rendered-content :deep(pre),
.rendered-content :deep(pre code),
.rendered-content :deep(code) {
 /* line-height: 3 !important;       !* 推荐 1.4 ~ 1.6，代码最舒适 *!*/
  white-space: pre-wrap !important;  /* 保留换行，但允许自动换行 */
  margin: 0.4em 0 !important;        /* 上下外边距减小 */
  padding: 0.1em 0.5em !important;   /* 上下内边距减小，左右稍宽一点好看 */
  background: #ececea;
  /*background: linear-gradient(135deg, rgba(242, 197, 92), rgb(242, 197, 92)) !important; !* 添加渐变背景 *!*/
  border-radius: 10px;
  /*overflow-x: auto;*/
  font-size: 0.94em; /* 字体略小一点，节省高度*/
  color: rgb(252, 123, 153);
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace !important;
}
/* 如果你用了 hljs 的类，也可以针对它设置 */
.rendered-content :deep(.hljs) {
  /*line-height: 3 !important;*/
  color: rgb(186, 112, 7);
}

/*!* 防止其他地方的 line-height 继承干扰 *!
.rendered-content :deep(pre *) {
  line-height: 3 !important;
}*/
.loading,
.error {
  text-align: center;
  padding: 60px 20px;
  font-size: 1.1rem;
}

.loading { color: #909399; }
.error   { color: #f56c6c; }

/* Mermaid 相关样式 */
/* Mermaid 强制优化样式 - sequenceDiagram 专用：浅色背景 + 文字不粗 + 高对比 */

/* 整体容器 */
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

/* participant / actor 框背景 - 强制浅色 */
.rendered-content :deep(.mermaid .actor rect),
.rendered-content :deep(.mermaid g rect.actor) {
  fill: #f1f5f9 !important;          /* 非常浅灰白 */
  stroke: #cbd5e1 !important;        /* 浅灰边框 */
  stroke-width: 1.8px !important;
}

/* 消息文字（箭头上的文字） - 深色、不粗 */
.rendered-content :deep(.mermaid .messageText tspan),
.rendered-content :deep(.mermaid .messageText text),
.rendered-content :deep(.mermaid .signalText) {
  fill: #111827 !important;          /* 深灰，几乎黑 */
  font-weight: normal !important;    /* 不加粗 */
  stroke: none !important;           /* 移除任何描边，避免变粗 */
  font-size: 14px !important;        /* 可微调大小 */
}

/* participant / actor 文字（框上面的名字） - 深色、不粗 */
.rendered-content :deep(.mermaid .actor text) {
  fill: #0f172a !important;          /* 深 slate */
  font-weight: normal !important;
  stroke: none !important;
}

/* Note 框（如果你的图有 Note） - 浅黄色背景 + 深文字 */
.rendered-content :deep(.mermaid .note rect) {
  fill: #fefce8 !important;          /* 浅黄/米白，避免黑 */
  stroke: #ca8a04 !important;
  stroke-width: 1.5px !important;
}

.rendered-content :deep(.mermaid .note text) {
  fill: #713f12 !important;
  font-weight: normal !important;
}

/* 箭头/连线 - 中灰可见 */
.rendered-content :deep(.mermaid .messageLine0 path),
.rendered-content :deep(.mermaid .messageLine1 path),
.rendered-content :deep(.mermaid .edgePath path) {
  stroke: #64748b !important;
  stroke-width: 1.4px !important;
}

.rendered-content :deep(.mermaid marker path) {
  fill: #64748b !important;
}

/* 序号（autonumber）文字 - 保持清晰 */
.rendered-content :deep(.mermaid .sequence-number) {
  fill: #6b7280 !important;
  font-weight: normal !important;
}

/* 暗黑模式额外保障（保持浅背景 + 深文字） */
@media (prefers-color-scheme: dark) {
  .rendered-content :deep(.mermaid .actor rect),
  .rendered-content :deep(.mermaid g rect.actor) {
    fill: #e5e7eb !important;      /* 稍亮的浅灰 */
  }

  .rendered-content :deep(.mermaid .messageText tspan),
  .rendered-content :deep(.mermaid .messageText text),
  .rendered-content :deep(.mermaid .signalText),
  .rendered-content :deep(.mermaid .actor text) {
    fill: #111827 !important;
  }

  .rendered-content :deep(.mermaid .note rect) {
    fill: #fefce8 !important;
  }
}
</style>