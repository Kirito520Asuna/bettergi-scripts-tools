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
        <h1>领域配置</h1>
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

// 选择 type
const selectType = (type) => {
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
.layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 250px;
  background-color: #f5f5f5;
  padding: 20px;
  overflow-y: auto;
  border-right: 1px solid #ddd;
}

.type-group {
  margin-bottom: 15px;
}

.type-header {
  font-weight: bold;
  cursor: pointer;
  padding: 8px;
  background-color: #e0e0e0;
  border-radius: 4px;
}

.type-list {
  list-style: none;
  padding-left: 10px;
}

.type-list li {
  padding: 5px 0;
  cursor: pointer;
}

.type-list li:hover {
  color: #007bff;
}

.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.search-input {
  width: 100%;
  padding: 10px;
  margin-bottom: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
}

.card {
  border: 1px solid #ddd;
  border-radius: 8px;
  margin-top: 20px;
  overflow: hidden;
  background-color: #fff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.card-header {
  padding: 15px;
  background-color: #f5f5f5;
  font-weight: bold;
}

.card-body {
  padding: 15px;
}

.card-body ul {
  list-style-type: none;
  padding-left: 0;
}

.card-body li {
  padding: 5px 0;
  border-bottom: 1px dashed #eee;
}

.card-body li:last-child {
  border-bottom: none;
}
.tree-view {
  margin-top: 20px;
}

.tree-node {
  margin-bottom: 15px;
}

.node-header {
  font-weight: bold;
  cursor: pointer;
  padding: 8px;
  background-color: #f0f0f0;
  border-radius: 4px;
}

.node-list {
  list-style: none;
  padding-left: 20px;
  margin-top: 5px;
}

.node-list li {
  padding: 5px 0;
  border-bottom: 1px dashed #eee;
}

.node-list li:last-child {
  border-bottom: none;
}

</style>

