<!-- MonitorCard.vue -->
<template>
  <div class="monitor-card">
    <!-- 头部区域 -->
    <div class="card-header" @click="handleToggle">
      <div class="header-left">
        <span class="icon" v-html="getIcon({icon: icon})" v-if="icon"/>
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
              v-for="(item, index) in buttonList"
              :key="item.key"
              class="action-btn"
              :class="{ 'is-disabled': item.disabled }"
              :style="{ backgroundColor: lightColors[index % lightColors.length] }"
              @click="item.click()"
          >
            <span v-if="item.icon" v-html="getIcon(item)" class="icon"/>
            <div class="action-btn-label">{{ item.label }}</div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import {iconAsMapDefault} from "@utils/defaultdata.js";
const emit = defineEmits(['update:expand'])
// ✅ 核心：defineModel，自动双向绑定，handleToggle 直接改它
// const expand = defineModel({
//   type: Boolean,
//   default: true
// })
const props = defineProps({
  expand: {
    type: Boolean,
    default: true
  },
  icon:{
    type: String,
    default: ''
  },
  mainTitle: {
    type: String,
    default: ''
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
      // {
      //   key:'btn1',
      //   disabled: false,
      //   label: '按钮1',
      //   icon: '🔍',
      //   click: () => { console.log('按钮1点击') }
      // },
      // {
      //   key:'btn2',
      //   disabled: false,
      //   label: '按钮2',
      //   icon: '🔍',
      //   click: () => { console.log('按钮2点击') }
      // }
    ]
  }
})
// 切换展开收起
const handleToggle = () => {
  // expand.value = !expand.value
  emit('update:expand', !props.expand)
}
let iconAsMap = iconAsMapDefault()
const lightColors = [
  '#f8d7da',
  '#d1ecf1',
  'rgba(116,181,181,0.56)',
  '#e1c7ba',
  'rgba(255,141,195,0.54)',
  '#ced4da',
];
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
  box-shadow: 0 3px 14px rgba(110, 255, 233, 0.16);
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
      color: #3498db;
      font-weight: bold;
    }
    .sub-title {
      font-size: 28px;
      color: #ff8833;
    }
  }

  .toggle-text {
    font-size: 22px;
    color: #3498db;
  }
}

.card-body {
  margin-top: 14px;
  overflow: hidden;
  max-height: 2000px;
}

.btn-grid {
  display: grid;
  gap: 16px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #222831;
  padding: 20px 0;
  border-radius: 999px;
  font-size: 24px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.22s ease;
  user-select: none;
  .action-btn-label {
    border: none;
    background: transparent;
    color: #3498db;
    cursor: pointer;
    font-weight: 500;
  }
  &:hover {
    filter: brightness(1.08);
    transform: translateY(-2px);
  }

  &.is-disabled {
    opacity: 0.55;
    cursor: not-allowed;
    transform: none;
    filter: none;
  }
  .icon {
    font-size: 22px;
    color: #222831;
  }
  .icon-png {
    height: 24px;
    vertical-align: middle;
  }
}

.slide-enter-active,
.slide-leave-active {
  transition: max-height 0.3s ease, opacity 0.3s ease;
  overflow: hidden;
}
.slide-enter-from,
.slide-leave-to {
  max-height: 0;
  opacity: 0;
  margin-top: 0;
}
</style>
