<template>
  <div class="container">
    <h1>领域配置</h1>
    <input v-model="searchKeyword" type="text" placeholder="搜索..." class="search-input"/>
    <div class="domain-list">
      <div v-for="(item, index) in filteredData" :key="index" class="card">
        <div class="card-header" @click="toggleCard(index)">
          {{ item.name }} ({{ item.type }})
        </div>
        <div v-show="expandedCards.includes(index)" class="card-body">
          <ul>
            <li v-for="(entry, idx) in item.list" :key="idx">{{ entry }}</li>
          </ul>
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

<style scoped>
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
</style>
