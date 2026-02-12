<template>
  <div class="home">
    <div class="header-fixed">
      <h1>秘境</h1>
      <h2>{{ selectedType || '请选择一个类型' }}</h2>
    </div>
    <div class="layout">
      <!-- 左侧固定区域：仅展示 type -->
      <div class="sidebar">
        <div v-for="(items, type) in groupedData" :key="type" class="type-group">
          <div class="type-header" @click="selectType(type)">
            {{ type }}
          </div>
        </div>
      </div>
      <!-- 右侧主内容区域 -->
      <div class="main-content">
<div v-if="selectedTypeItems.length > 0" class="card-list">
  <div v-for="(item, index) in selectedTypeItems" :key="index" class="card">
    <div class="card-header">
      <h3>{{ item.name }}</h3>
    </div>
    <div class="card-body">
      <ul class="card-list-items">
        <li v-for="(entry, idx) in item.list" :key="idx">{{ entry }}</li>
      </ul>
    </div>
  </div>
</div>

<!--        <div v-if="selectedTypeItems.length > 0" class="tree-view">
          <div v-for="(item, index) in selectedTypeItems" :key="index" class="tree-node">
            <div class="node-header" @click="toggleItem(index)">
              {{ item.name }}
            </div>
            <ul v-show="expandedItems.includes(index)" class="node-list">
              <li v-for="(entry, idx) in item.list" :key="idx">{{ entry }}</li>
            </ul>
          </div>
        </div>
        <div v-else>
          请选择一个类型以查看内容。
        </div>-->
      </div>
    </div>
    <div class="fixed-footer">
      <button @click="goToHome" class="btn secondary">🏠 返回主页</button>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue';
import {domainsDefault} from "@utils/defaultdata.js";
import {getBaseJsonAll} from "@api/domain/autoPlan.js";
import {ElMessage} from "element-plus";
import router from "@router/router.js";

const goToHome = () => router.push('/');

const domainData = ref(domainsDefault);
const selectedTypeItems = ref([]);
const expandedItems = ref([]);
const selectedType = ref('');

const groupedData = computed(() => {
  const groups = {};
  domainData.value.forEach(item => {
    if (!groups[item.type]) groups[item.type] = [];
    groups[item.type].push(item);
  });
  return groups;
});

const selectType = (type) => {
  selectedType.value = type;
  selectedTypeItems.value = groupedData.value[type] || [];
  expandedItems.value = [];
};

const toggleItem = (index) => {
  if (expandedItems.value.includes(index)) {
    expandedItems.value = expandedItems.value.filter(i => i !== index);
  } else {
    expandedItems.value.push(index);
  }
};

onMounted(() => {
  fetchDomains();
});

const fetchDomains = async () => {
  try {
    const response = await getBaseJsonAll();
    if (response && response.length > 0) {
      domainData.value = response;
    } else {
      ElMessage.warning('无数据存储，使用默认秘境数据。');
    }
  } catch (error) {
    console.error('请求失败:', error);
    ElMessage.warning('使用默认秘境数据。');
  }
};
</script>

<style scoped>
.home{
  width: 100vw;
  height: 100vh !important;
}

.layout {
  display: flex;
  width: 100vw;
  height: 100vh;
}

/* ==================== 左侧 Sidebar ==================== */
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: 260px;
  height: 100vh;
  border-right: 1px solid rgba(0, 118, 255, 0.73);
  padding: 24px 16px;
  overflow-y: auto;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.type-group {
  margin-bottom: 8px;
}

.type-header {
  padding: 14px 20px;
  font-size: 15px;
  font-weight: 600;
  color: #04b8d8;
  background: linear-gradient(135deg, #b6b2b6, #cf6137);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.type-header:hover {
  background: linear-gradient(135deg, #b6b2b6, #ff4400);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* ==================== 右侧主内容 ==================== */
.header-fixed {
  width: 20%;
  position: fixed;
  top: 10px;
  left: 300px; /* 与左侧 sidebar 宽度对齐 */
  right: 300px;
/*  background: #ffffff;*/
  padding: 16px 40px;
  border-radius: 12px;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  gap: 20px;
}
.header-fixed h1 {
  font-size: 26px;
  font-weight: 700;
  color: #4195ff;
  margin-bottom: 4px;
}

.header-fixed h2 {
  font-size: 16px;
  color: #e6a327;
  font-weight: 500;
  margin-bottom: 20px;
  margin-top: 0;
}

.main-content {
  margin-left: 260px;          /* 关键修复：避免与左侧 fixed 重叠 */
  margin-top: 80px; /* 为固定头部预留空间 */
  flex: 1;
  padding: 24px 40px;
  overflow-y: auto;
}

.main-content h1 {
  font-size: 26px;
  font-weight: 700;
  color: #4195ff;
  margin-bottom: 4px;
}

.main-content h2 {
  font-size: 16px;
  color: #e6a327;
  font-weight: 500;
  margin-bottom: 20px;
  margin-top: 0;
}

/* 树形结构 - 保留你的原背景 */
.card-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); /* 自适应列数 */
  gap: 20px; /* 卡片间距 */
  padding: 20px 0;
}

.card {
  background: linear-gradient(135deg, #b6b2b6, #91dcd6);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.card-header {
  padding: 16px 20px;
  background: linear-gradient(135deg, #b6b2b6, #00ffff);
  color: #ffffff;
  font-weight: 600;
  font-size: 18px;
}

.card-body {
  padding: 16px 20px;
}

.card-list-items {
  list-style: none;
  padding: 0;
  margin: 0;
}

.card-list-items li {
  padding: 8px 0;
  font-size: 15px;
 /* color: rgba(149, 31, 77, 0.78);*/
  border-bottom: 1px solid #f1f5f9;

  color: transparent;
  background: linear-gradient(90deg, #ff6b6b, #ef006a); /* 渐变色方向和颜色 */
  -webkit-background-clip: text; /* 兼容 WebKit 内核浏览器 */
  background-clip: text;/* 将背景裁剪为文字形状*/
  color: transparent; /* 文字颜色设为透明 */
  font-size: 1.2rem; /* 可根据需要调整字体大小 */
  font-weight: 600; /* 可根据需要调整字体粗细 */
}

.card-list-items li:last-child {
  border-bottom: none;
}

.tree-view {
  max-width: 920px;
}

.tree-node {
  margin-bottom: 16px;
  background: linear-gradient(135deg, #b6b2b6, #91dcd6);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(230, 227, 227, 0.1);
}

.tree-node:hover {
  box-shadow: 0 10px 25px -5px rgba(138, 35, 35, 0.1);
  transform: translateY(-2px);
}

.node-header {
  padding: 18px 24px;
  font-size: 17px;
  font-weight: 600;
  color: #615959;
  background: linear-gradient(135deg, #b6b2b6, #91dcd6);
  cursor: pointer;
  position: relative;
}

.node-header:hover {
  background: linear-gradient(135deg, #b6b2b6, #00ffff);
}

.node-header::after {
  content: '›';
  position: absolute;
  right: 24px;
  font-size: 22px;
  color: #94a3b8;
  transition: transform 0.3s ease;
}

.tree-node:has(.node-list:not([style*="none"])) .node-header::after {
  transform: rotate(90deg);
  color: #3b82f6;
}

.node-list {
  list-style: none;
  padding: 0;
  background: linear-gradient(135deg, #b6b2b6, #91dcd6);
  border-top: 1px solid #e2e8f0;
}

.node-list li {
  padding: 14px 28px;
  font-size: 15px;
  color: rgba(0, 0, 0, 0.78);
  border-bottom: 1px solid #f1f5f9;
}

.node-list li:hover {
  background: linear-gradient(135deg, #b6b2b6, #00ffe9);
  padding-left: 32px;
}

/* 空状态 */
.main-content > div:last-child {
  margin-top: 120px;
  text-align: center;
  color: #94a3b8;
  font-size: 16px;
}

/*!* 固定底部按钮 *!
.fixed-footer {
  position: fixed;
  bottom: 0;
  left: 260px;
  right: 0;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}*/

.btn.secondary {
  padding: 10px 24px;
  background: #6b7280;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

/* 响应式 */
@media (max-width: 768px) {
  .sidebar { width: 220px; }
  .main-content { margin-left: 220px; }
  .fixed-footer { left: 220px; }
}
</style>