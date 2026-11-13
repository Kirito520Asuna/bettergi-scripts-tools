<script setup>
import { goBack, toHomePage } from '@api/web/web.js'
import {ref} from "vue";

// 加载状态
const loadingBack = ref(false)
const loadingHome = ref(false)

const goToBack = async () => {
  if (loadingBack.value) return
  loadingBack.value = true
  try {
    await goBack()
  } finally {
    loadingBack.value = false
  }
}

const goToHome = async () => {
  if (loadingHome.value) return
  loadingHome.value = true
  try {
    await toHomePage()
  } finally {
    loadingHome.value = false
  }
}
</script>

<template>
  <!-- 返回上一页 -->
  <div class="fixed-btn fixed-btn--left">
    <button
        @click="goToBack"
        class="btn secondary"
        :disabled="loadingBack"
    >
      {{ loadingBack ? '跳转中...' : '返回上一页' }}
    </button>
  </div>

  <!-- 返回主页 -->
  <div class="fixed-btn fixed-btn--right">
    <button
        @click="goToHome"
        class="btn secondary"
        :disabled="loadingHome"
    >
      {{ loadingHome ? '跳转中...' : '🏠 返回主页' }}
    </button>
  </div>
</template>

<style scoped>
.fixed-btn {
  position: fixed;
  bottom: 80px;
  z-index: 1000;
}
.fixed-btn--left {
  left: 30px;
}
.fixed-btn--right {
  right: 30px;
}

.btn.secondary {
  padding: 10px 18px;
  border: none;
  border-radius: 999px;
  font-size: 14px;
  cursor: pointer;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  /* 统一过渡动画 */
  transition: all 0.25s ease;
}

.btn.secondary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn.secondary:active:not(:disabled) {
  transform: translateY(0);
}

.btn.secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 移动端适配，屏幕小的时候边距缩小 */
@media (max-width: 640px) {
  .fixed-btn--left {
    left: 16px;
  }
  .fixed-btn--right {
    right: 16px;
  }
  .fixed-btn {
    bottom: 24px;
  }
}
</style>
