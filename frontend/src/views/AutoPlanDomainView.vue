<template>
  <div class="container">
    <div class="layout">
      <!-- 左侧固定区域：仅展示 type -->
      <div class="sidebar">
        <div v-for="(items, type) in groupedData" :key="type" class="type-group">
          <div class="type-header" @click="selectType(type)">
            {{ type }}
          </div>
        </div>
      </div>

      <!-- 右侧主内容区域：树形结构展示 item -->
      <div class="main-content">
        <h1>秘境</h1>
        <h2>{{ selectedType || '请选择一个类型' }}</h2>
        <div v-if="selectedTypeItems.length > 0" class="tree-view">
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
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue';
import {domainsDefault} from "@utils/defaultdata.js";
import {getBaseJsonAll} from "@api/domain/autoPlan.js";
import {ElMessage} from "element-plus";

// 模拟数据
const domainData = ref(domainsDefault);
const fetchDomains = async () => {
  try {
    // const response = await service.get('/auto/plan/domain/json/all');
    const response = await getBaseJsonAll()
    console.log('response', response)
    if (response && response.length > 0) {
      domainData.value = response;
    } else {
      ElMessage({
        type: 'warning',
        message: '无数据存储，使用默认秘境数据。',
      });
    }
  } catch (error) {
    console.error('请求失败:', error);
    ElMessage({
      type: 'warning',
      message: '使用默认秘境数据。',
    });
  } finally {
  }
};
// 将 domainData 按照 type 分组
// 数据分组
const groupedData = computed(() => {
  const groups = {};
  domainData.value.forEach(item => {
    if (!groups[item.type]) {
      groups[item.type] = [];
    }
    groups[item.type].push(item);
  });
  return groups;
});

// 当前选中的 type 对应的 items
const selectedTypeItems = ref([]);

// 控制 item 展开/收起
const expandedItems = ref([]);
// 当前选中的 type
const selectedType = ref('');
// 选择 type
const selectType = (type) => {
  selectedType.value = type; // 更新选中的 type
  selectedTypeItems.value = groupedData.value[type] || [];
  expandedItems.value = []; // 清空已展开的 item
};

// 切换 item 展开/收起
const toggleItem = (index) => {
  if (expandedItems.value.includes(index)) {
    expandedItems.value = expandedItems.value.filter(i => i !== index);
  } else {
    expandedItems.value.push(index);
  }
};
onMounted(() => {
  fetchDomains();
})
</script>

<style scoped>
/* ==================== 全局基础 ==================== */
.container {
  height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: #333;
  background: url("@assets/MHY_XTLL.png") no-repeat center center fixed;
  background-size: cover;
}

.layout {
  display: flex;
  height: 100vh;
  /* background: #f8f9fa;*/
}

/* ==================== 左侧 Sidebar ==================== */
.sidebar {
  width: 260px;
  /*background: #ffffff;*/
  border-right: 1px solid #e0e0e0;
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
  /* background: #f1f3f5;*/
  background: linear-gradient(135deg, #b6b2b6, #cf6137);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.type-header:hover {
  /*  background: #e3e7eb;*/
  background: linear-gradient(135deg, #b6b2b6, #ff4400);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.type-header:active {
  transform: scale(0.98);
}

/* ==================== 右侧主内容 ==================== */
.main-content {
  flex: 1;
  padding: 24px 40px; /* 更小的内边距 */ /* 整体内边距略微缩小（原 40/50） */
  overflow-y: auto;
/*  background: #ffffff;*/
}

.main-content h1 {
  font-size: 26px; /* 稍小一点，更现代 */
  font-weight: 700;
  color: #4195ff;
  margin-bottom: 4px; /* h1 到 h2 间距大幅缩小 */
  letter-spacing: -0.4px;
}

.main-content h2 {
  font-size: 16px; /* 更低调 */
  color: #e6a327;
  font-weight: 500;
  margin-bottom: 20px; /* h2 到树列表的间距缩小（原 32px） */
  margin-top: 0;
}

/* 空状态提示也跟着上移一点，显得不那么空 */
.main-content > div:last-child {
  margin-top: 80px; /* 原 120px → 80px，更紧凑 */
  text-align: center;
  color: #94a3b8;
  font-size: 16px;
}

/* ==================== 树形结构 ==================== */
/* 当有内容时，让树视图更靠近上方 */
.tree-view {
  max-width: 920px;
  margin-top: 0; /* 去掉可能的额外上边距 */
}

.tree-node {
  margin-bottom: 16px;
  /*  background: #d8cbcb;*/
  background: linear-gradient(135deg, #b6b2b6, #91dcd6);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(230, 227, 227, 0.1);
  transition: all 0.25s ease;
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
  /* background: #f8fafc;*/
  background: linear-gradient(135deg, #b6b2b6, #91dcd6);

  cursor: pointer;
  display: flex;
  align-items: center;
  position: relative;
  transition: background 0.2s;
}

.node-header:hover {
  /*background: #f1f5f9;*/
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

/* 展开时箭头旋转 */
.tree-node:has(.node-list:not([style*="none"])) .node-header::after {
  transform: rotate(90deg);
  color: #3b82f6;
}

.node-list {
  list-style: none;
  padding: 0;
  /*background: #cacaca;*/
  background: linear-gradient(135deg, #b6b2b6, #91dcd6);
  border-top: 1px solid #e2e8f0;
}

.node-list li {
  padding: 14px 28px;
  font-size: 15px;
  color: rgba(0, 0, 0, 0.78);
  border-bottom: 1px solid #f1f5f9;
  transition: background 0.15s;
}

.node-list li:last-child {
  border-bottom: none;
}

.node-list li:hover {
  /*background: #f0f9ff;*/
  background: linear-gradient(135deg, #b6b2b6, #00ffe9);
  padding-left: 32px;
}

/* ==================== 空状态 ==================== */
.main-content > div:last-child {
  margin-top: 120px;
  text-align: center;
  color: #94a3b8;
  font-size: 16px;
}

/* ==================== 响应式小优化 ==================== */
@media (max-width: 768px) {
  .sidebar {
    width: 220px;
  }

  .main-content {
    padding: 24px;
  }
}
</style>