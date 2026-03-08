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
import {getBaseJsonAll} from "@api/auto_plan/autoPlan.js";
import {ElMessage} from "element-plus";
import router from "@router/router.js";
import {toHomePage} from "@api/web/web.js";

const goToHome = async () => {
  // router.push('/'); // 假设主页路径是 '/'
  await toHomePage()
};


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
:root {
  --sidebar-bg-light: rgba(255, 255, 255, 0.95);
  --sidebar-bg-dark: rgba(30, 41, 59, 0.95);
  --sidebar-border-light: rgba(0, 118, 255, 0.73);
  --sidebar-border-dark: rgba(99, 179, 237, 0.5);
  --header-bg-light: rgba(255, 255, 255, 0.85);
  --header-bg-dark: rgba(30, 41, 59, 0.85);
  --card-bg-light: linear-gradient(135deg, #b6b2b6, #91dcd6);
  --card-bg-dark: linear-gradient(135deg, #4a5568, #2d3748);
  --card-header-light: linear-gradient(135deg, #b6b2b6, #00ffff);
  --card-header-dark: linear-gradient(135deg, #4a5568, #0ea5e9);
  --text-primary-light: #2c3e50;
  --text-primary-dark: #e2e8f0;
  --text-gradient-light: linear-gradient(90deg, #ff6b6b, #ef006a);
  --text-gradient-dark: linear-gradient(90deg, #fbb6ce, #f687b3);
}

.home {
  width: 100vw;
  height: 100vh !important;
  background: url("@assets/MHY_XTLL.png");
  background-attachment: fixed;
  background-size: cover;
  background-position: center;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .home {
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  }
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
  border-right: 1px solid var(--sidebar-border-light);
  padding: 24px 16px;
  overflow-y: auto;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  background: var(--sidebar-bg-light);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .sidebar {
    background: var(--sidebar-bg-dark);
    border-color: var(--sidebar-border-dark);
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.3);
  }
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

@media (prefers-color-scheme: dark) {
  .type-header {
    color: #63b3ed;
  }
}

/* ==================== 右侧主内容 ==================== */
.header-fixed {
  width: calc(100% - 560px);
  position: fixed;
  top: 10px;
  left: 300px;
  right: 300px;
  background: var(--header-bg-light);
  padding: 16px 40px;
  border-radius: 12px;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  gap: 20px;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .header-fixed {
    background: var(--header-bg-dark);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  }
}

.header-fixed h1 {
  font-size: 26px;
  font-weight: 700;
  color: #4195ff;
  margin-bottom: 4px;
}

@media (prefers-color-scheme: dark) {
  .header-fixed h1 {
    color: #63b3ed;
  }
}

.header-fixed h2 {
  font-size: 16px;
  color: #e6a327;
  font-weight: 500;
  margin-bottom: 20px;
  margin-top: 0;
}

@media (prefers-color-scheme: dark) {
  .header-fixed h2 {
    color: #fbb6ce;
  }
}

.main-content {
  margin-left: 260px;
  margin-top: 80px;
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

@media (prefers-color-scheme: dark) {
  .main-content h1 {
    color: #63b3ed;
  }
}

.main-content h2 {
  font-size: 16px;
  color: #e6a327;
  font-weight: 500;
  margin-bottom: 20px;
  margin-top: 0;
}

@media (prefers-color-scheme: dark) {
  .main-content h2 {
    color: #fbb6ce;
  }
}

/* 树形结构 */
.card-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 20px 0;
}

.card {
  background: var(--card-bg-light);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .card {
    background: var(--card-bg-dark);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  }
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

@media (prefers-color-scheme: dark) {
  .card:hover {
    box-shadow: 0 8px 20px rgba(99, 179, 237, 0.25);
  }
}

.card-header {
  padding: 16px 20px;
  background: var(--card-header-light);
  color: #ffffff;
  font-weight: 600;
  font-size: 18px;
}

@media (prefers-color-scheme: dark) {
  .card-header {
    background: var(--card-header-dark);
  }
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
  border-bottom: 1px solid #f1f5f9;
  color: transparent;
  background: var(--text-gradient-light);
  -webkit-background-clip: text;
  background-clip: text;
  font-size: 1.2rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .card-list-items li {
    background: var(--text-gradient-dark);
    border-color: rgba(255, 255, 255, 0.1);
  }
}

.card-list-items li:last-child {
  border-bottom: none;
}

.tree-view {
  max-width: 920px;
}

.tree-node {
  margin-bottom: 16px;
  background: var(--card-bg-light);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(230, 227, 227, 0.1);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .tree-node {
    background: var(--card-bg-dark);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  }
}

.tree-node:hover {
  box-shadow: 0 10px 25px -5px rgba(138, 35, 35, 0.1);
  transform: translateY(-2px);
}

@media (prefers-color-scheme: dark) {
  .tree-node:hover {
    box-shadow: 0 10px 25px -5px rgba(99, 179, 237, 0.2);
  }
}

.node-header {
  padding: 18px 24px;
  font-size: 17px;
  font-weight: 600;
  color: #615959;
  background: linear-gradient(135deg, #b6b2b6, #91dcd6);
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .node-header {
    color: #e2e8f0;
    background: var(--card-bg-dark);
  }
}

.node-header:hover {
  background: linear-gradient(135deg, #b6b2b6, #00ffff);
}

@media (prefers-color-scheme: dark) {
  .node-header:hover {
    background: linear-gradient(135deg, #4a5568, #0ea5e9);
  }
}

.node-header::after {
  content: '›';
  position: absolute;
  right: 24px;
  font-size: 22px;
  color: #94a3b8;
  transition: transform 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .node-header::after {
    color: #cbd5e1;
  }
}

.tree-node:has(.node-list:not([style*="none"])) .node-header::after {
  transform: rotate(90deg);
  color: #3b82f6;
}

@media (prefers-color-scheme: dark) {
  .tree-node:has(.node-list:not([style*="none"])) .node-header::after {
    color: #63b3ed;
  }
}

.node-list {
  list-style: none;
  padding: 0;
  background: var(--card-bg-light);
  border-top: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .node-list {
    background: var(--card-bg-dark);
    border-color: rgba(255, 255, 255, 0.1);
  }
}

.node-list li {
  padding: 14px 28px;
  font-size: 15px;
  color: rgba(0, 0, 0, 0.78);
  border-bottom: 1px solid #f1f5f9;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .node-list li {
    color: rgba(255, 255, 255, 0.85);
    border-color: rgba(255, 255, 255, 0.1);
  }
}

.node-list li:hover {
  background: linear-gradient(135deg, #b6b2b6, #00ffe9);
  padding-left: 32px;
}

@media (prefers-color-scheme: dark) {
  .node-list li:hover {
    background: linear-gradient(135deg, #4a5568, #0ea5e9);
  }
}

/* 空状态 */
.main-content > div:last-child {
  margin-top: 120px;
  text-align: center;
  color: #94a3b8;
  font-size: 16px;
}

@media (prefers-color-scheme: dark) {
  .main-content > div:last-child {
    color: #718096;
  }
}

.btn.secondary {
  padding: 10px 24px;
  background: #6b7280;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

/* 手机端适配 */
@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    height: auto;
    position: relative;
    border-right: none;
    border-bottom: 1px solid var(--sidebar-border-light);
    padding: 15px;
  }

  @media (prefers-color-scheme: dark) {
    .sidebar {
      border-bottom-color: var(--sidebar-border-dark);
    }
  }

  .layout {
    flex-direction: column;
  }

  .header-fixed {
    position: relative;
    width: calc(100% - 30px);
    left: 15px;
    right: 15px;
    top: 0;
    padding: 12px 20px;
  }

  .header-fixed h1 {
    font-size: 20px;
  }

  .header-fixed h2 {
    font-size: 14px;
    margin-bottom: 15px;
  }

  .main-content {
    margin-left: 0;
    margin-top: 0;
    padding: 15px;
  }

  .main-content h1 {
    font-size: 20px;
  }

  .main-content h2 {
    font-size: 14px;
  }

  .card-list {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .card {
    border-radius: 10px;
  }

  .card-header {
    padding: 12px 16px;
    font-size: 16px;
  }

  .card-body {
    padding: 12px 16px;
  }

  .card-list-items li {
    font-size: 1rem;
    padding: 6px 0;
  }

  .tree-view {
    max-width: 100%;
  }

  .tree-node {
    border-radius: 10px;
  }

  .node-header {
    padding: 14px 18px;
    font-size: 15px;
  }

  .node-header::after {
    right: 18px;
    font-size: 18px;
  }

  .node-list li {
    padding: 12px 20px;
    font-size: 14px;
  }

  .type-header {
    padding: 12px 16px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .sidebar {
    padding: 12px;
  }

  .header-fixed {
    width: calc(100% - 24px);
    left: 12px;
    right: 12px;
    padding: 10px 15px;
  }

  .header-fixed h1 {
    font-size: 18px;
  }

  .header-fixed h2 {
    font-size: 12px;
  }

  .main-content {
    padding: 12px;
  }

  .card-header {
    padding: 10px 14px;
    font-size: 15px;
  }

  .card-body {
    padding: 10px 14px;
  }

  .card-list-items li {
    font-size: 0.9rem;
  }

  .node-header {
    padding: 12px 16px;
    font-size: 14px;
  }

  .node-header::after {
    font-size: 16px;
    right: 16px;
  }

  .node-list li {
    padding: 10px 16px;
    font-size: 13px;
  }

  .type-header {
    padding: 10px 14px;
    font-size: 13px;
  }
}

/* 横屏手机适配 */
@media (max-width: 768px) and (orientation: landscape) {
  .main-content {
    max-height: calc(100vh - 120px);
    overflow-y: auto;
  }

  .card-list {
    gap: 10px;
  }

  .card {
    margin-bottom: 0;
  }
}
</style>
