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
@import "@css/auto_plan_domain.css";
</style>
