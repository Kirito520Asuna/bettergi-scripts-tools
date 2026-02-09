<template>
  <div class="container">
    <div class="layout">
      <!-- 左侧固定区域 -->
      <div class="sidebar">
        <div v-for="(items, type) in groupedData" :key="type" class="type-group">
          <div class="type-header" @click="toggleType(type)">
            {{ type }}
          </div>
          <ul v-show="expandedTypes.includes(type)" class="type-list">
            <li v-for="(item, index) in items" :key="index" @click="selectItem(item)">
              {{ item.name }}
            </li>
          </ul>
        </div>
      </div>

      <!-- 右侧主内容区域 -->
      <div class="main-content">
        <h1>领域配置</h1>
        <input v-model="searchKeyword" type="text" placeholder="搜索..." class="search-input"/>
        <div class="domain-tree">
          <!-- 动态展示选中项的详细内容 -->
          <div v-if="selectedItem" class="card">
            <div class="card-header">{{ selectedItem.name }}</div>
            <div class="card-body">
              <ul>
                <li v-for="(entry, idx) in selectedItem.list" :key="idx">{{ entry }}</li>
              </ul>
            </div>
          </div>
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
// 控制分组展开/收起
const expandedTypes = ref([]);
// 切换分组展开/收起
const toggleType = (type) => {
  if (expandedTypes.value.includes(type)) {
    expandedTypes.value = expandedTypes.value.filter(t => t !== type);
  } else {
    expandedTypes.value.push(type);
  }
};
// 选中项
const selectedItem = ref(null);

const selectItem = (item) => {
  selectedItem.value = item;
};

onMounted(() => {
  fetchDomains();
})
// 响应式数据
const searchKeyword = ref('');
const expandedCards = ref([]);

// 计算属性：过滤后的数据
const filteredData = computed(() => {
  const keyword = searchKeyword.value.toLowerCase();
  return domainData.filter(
      (item) =>
          item.name.toLowerCase().includes(keyword) ||
          item.list.some((entry) => entry.toLowerCase().includes(keyword))
  );
});

// 方法：切换卡片展开/收起
const toggleCard = (index) => {
  if (expandedCards.value.includes(index)) {
    expandedCards.value = expandedCards.value.filter((i) => i !== index);
  } else {
    expandedCards.value.push(index);
  }
};
</script>

<!--<style scoped>
.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
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
  margin-bottom: 15px;
  overflow: hidden;
  background-color: #fff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.card-header {
  padding: 15px;
  background-color: #f5f5f5;
  font-weight: bold;
  cursor: pointer;
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

</style>-->

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
</style>

