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
    <div class="fixed-back">
      <button @click="goToBack" class="btn secondary">返回上一页</button>
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
import {goBack, toHomePage} from "@api/web/web.js";
// 在 script 中添加跳转逻辑
const goToHome = async () => {
  // router.push('/'); // 假设主页路径是 '/'
  await toHomePage()
};
const goToBack = async () => {
  await goBack();
}

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
@import '@css/markdown.css';
</style>
