<!-- MonitorCard.vue -->
<template>
  <div class="monitor-card">
    <!-- 头部区域 -->
    <div class="card-header" @click="handleToggle">
      <div class="header-left">
        <span class="icon" v-if="icon">{{ icon }}</span>
        <span class="main-title">{{ mainTitle }}</span>
        <span v-if="subTitle" class="sub-title">{{ subTitle }}</span>
      </div>
      <span class="toggle-text">{{expand ? '收起' : '展开'}}</span>
    </div>

    <!-- 折叠内容区 -->
    <transition name="fade">
      <div v-show="expand" class="card-body">
        <div
            class="btn-grid"
            :style="{ gridTemplateColumns: `repeat(${column}, 1fr)` }"
        >
          <div
              v-for="item in buttonList"
              :key="item.key"
              class="action-btn"
              :class="{ 'is-disabled': item.disabled }"
              @click="item.click()"
          >
            <span v-if="item.icon" v-html="getIcon(item)" class="icon"></span>
            {{ item.label }}
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import {iconAsMapDefault} from "../../utils/defaultdata.js";
// ✅ 核心：defineModel，自动双向绑定，handleToggle 直接改它
const expand = defineModel({
  type: Boolean,
  default: true
})
const props = defineProps({
  icon:{
    type: String,
    default: '🔍'
  },
  mainTitle: {
    type: String,
    default: '实时监测'
  },
  subTitle: {
    type: String,
    default: ''
  },
  column: {
    type: Number,
    default: 2
  },
  // 按钮配置数组
  buttonList: {
    type: Array,
    default: () => [
      {
        key:'btn1',
        disabled: false,
        label: '按钮1',
        icon: '🔍',
        click: () => { console.log('按钮1点击') }
      }
    ]
  }
})

const emit = defineEmits(['click-btn'])

// 切换展开收起
const handleToggle = () => {
  expand.value = !expand.value
}
// 按钮点击
const handleBtnClick = (item) => {
  if (item.disabled) return
  emit('click-btn', item)
}
let iconAsMap = iconAsMapDefault()
// 获取图标
const getIcon = (item) => {
  // 优先使用 meta.icon，没有则根据类型给默认 emoji
  let rawIcon = item?.icon;
  if (rawIcon) {
    // 字符串处理
    if (typeof rawIcon === "string") {
      const trimmed = rawIcon.trim();
      // 如果是 img 字符串
      if (trimmed.trim().startsWith('<img')) {
        return trimmed.trim() // 直接返回字符串
      }
      // 如果是 PNG 图片路径或 Base64 数据
      if (trimmed.endsWith('.png') || trimmed.endsWith('.jpg') || trimmed.startsWith('data:image/png')) {
        return `<img src="${trimmed}" class="icon-png" />`;
      }
      // 如果是 SVG 字符串
      if (trimmed.trim().startsWith('<svg')) {
        return trimmed.trim() // 直接返回字符串
      }
      // 优先级 2：从 iconMap 中根据别名查找（新加的部分）
      const alias = item?.icon; // 假设别名放在 meta.iconAlias，或用 key/name
      if (alias && iconAsMap.has(trimmed)) {
        const svgOrEmoji = iconAsMap.get(trimmed);

        // 如果是 SVG 字符串
        if (typeof svgOrEmoji === "string" && svgOrEmoji.trim().startsWith("<svg")) {
          return svgOrEmoji.trim() // 直接返回字符串
        }
        // 如果是 emoji 或其他字符串
        return svgOrEmoji;
      }
    }
    return rawIcon;
  }
  rawIcon = item.isLink ? "🔗" : item.isSwagger ? "📖" : item.isRote ? "🚀" : "";
  // 其他情况兜底（比如传了奇怪的东西）
  return rawIcon;
};

</script>

<style scoped lang="scss">
.monitor-card {
  background-color: #ffffff;
  border-radius: 24px;
  padding: 14px;
  box-shadow: 0 3px 14px rgba(255, 110, 170, 0.16);
  box-sizing: border-box;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 6px;
  cursor: pointer;

  .header-left {
    display: flex;
    align-items: center;
    gap: 10px;

    .icon {
      font-size: 26px;
    }
    .main-title {
      font-size: 26px;
      color: #ff4794;
      font-weight: bold;
    }
    .sub-title {
      font-size: 28px;
      color: #ff8833;
    }
  }

  .toggle-text {
    font-size: 22px;
    color: #b5427d;
  }
}

.card-body {
  margin-top: 14px;

  .btn-grid {
    display: grid;
    gap: 16px;
  }

  .action-btn {
    background: linear-gradient(90deg, #ff76b4, #ff57a0);
    color: #fff;
    text-align: center;
    padding: 20px 0;
    border-radius: 999px;
    font-size: 24px;
    font-weight: bold;
    cursor: pointer;
    transition: all 0.22s ease;
    user-select: none;

    &:hover {
      filter: brightness(1.1);
      transform: translateY(-2px);
    }

    &.is-disabled {
      opacity: 0.55;
      cursor: not-allowed;
      transform: none;
      filter: none;
    }
  }
}

/* 折叠过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.28s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
