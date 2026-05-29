<template>
  <div class="home">
    <div class="header-fixed">
      <h1>秘境</h1>
      <h2>{{ selectedType || '请选择一个类型' }}</h2>
      <div class="toolbar">
        <button @click="openAddDialog" class="btn primary">新增</button>
      </div>
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
          <div v-for="(item, index) in selectedTypeItems" :key="index" class="card" @click="editSingleCard(index)">
            <div class="card-header">
              <h3>{{ item.name }}</h3>
            </div>
            <div class="card-body">
              <ul class="card-list-items">
                <li v-for="(entry, idx) in item.list" :key="idx">{{ entry }}</li>
              </ul>
            </div>
          </div>

          <!-- 新增占位卡片 -->
          <div class="card add-placeholder" @click="openAddDialog">
            <div class="add-icon">+</div>
            <div class="add-text">新增{{ selectedType || '秘境' }}</div>
          </div>
        </div>

        <div v-else class="empty-tip">
          请选择一个类型以查看内容。
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <div v-if="showEditDialog" class="dialog-overlay" @click.self="closeEditDialog">
      <div class="dialog-content">
        <div class="dialog-header">
          <h2>编辑秘境</h2>
          <button class="close-btn" @click="closeEditDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>秘境名称</label>
            <input v-model="editingItem.name" class="field-input" placeholder="秘境名称"/>
          </div>
          <div class="form-group">
            <label>类型</label>
<!--            <input v-model="editingItem.type" class="field-input" placeholder="类型(天赋/武器/圣遗物)"
                   @change="handleEditTypeChange"/>-->

            <el-select v-model="editingItem.type" class="field-select" @change="handleTypeChange" placeholder="请选择类型">
              <el-option
                  v-for="type in [
                      '天赋',
                      '武器',
                      '圣遗物',
                  ]"
                  :key="type"
                  :label="type"
                  :value="type"
              />
            </el-select>
          </div>
          <div class="form-group">
            <label>刷取物品列表（最多{{ getMaxListLength(editingItem.type) }}个）</label>
            <div class="list-inputs">
              <input
                  v-for="(item, index) in editingItem.list"
                  :key="index"
                  v-model="editingItem.list[index]"
                  class="field-input list-item-input"
                  :placeholder="`物品${index + 1}`"
              />
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button @click="deleteCurrentItem" class="btn danger">删除</button>
          <button @click="closeEditDialog" class="btn secondary">取消</button>
          <button @click="saveCurrentItem" class="btn success">保存</button>
        </div>
      </div>
    </div>

    <!-- 新增弹窗 -->
    <div v-if="showAddDialog" class="dialog-overlay" @click.self="showAddDialog = false">
      <div class="dialog-content">
        <div class="dialog-header">
          <h2>新增秘境</h2>
          <button class="close-btn" @click="showAddDialog = false">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>秘境名称</label>
            <input v-model="newItem.name" class="field-input" placeholder="秘境名称"/>
          </div>
          <div class="form-group">
            <label>类型</label>
            <el-select v-model="newItem.type" class="field-select" @change="handleTypeChange" placeholder="请选择类型">
              <el-option
                  v-for="type in [
                      '天赋',
                      '武器',
                      '圣遗物',
                  ]"
                  :key="type"
                  :label="type"
                  :value="type"
              />
            </el-select>
          </div>
          <div class="form-group">
            <label>刷取物品列表（最多{{ getMaxListLength(newItem.type) }}个）</label>
            <div class="list-inputs">
              <input
                  v-for="(item, index) in newItem.list"
                  :key="index"
                  v-model="newItem.list[index]"
                  class="field-input list-item-input"
                  :placeholder="`物品${index + 1} ${newItem.type!=='圣遗物'?'<请严格按照游戏顺序填写>':''}`"
              />
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button @click="showAddDialog = false" class="btn secondary">取消</button>
          <button @click="confirmAddItem" class="btn success">添加</button>
        </div>
      </div>
    </div>

    <div class="fixed-back">
      <button @click="goToBack" class="btn secondary">返回上一页</button>
    </div>
    <div class="fixed-footer">
      <button @click="goToHome" class="btn secondary"> 返回主页</button>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue';
import {domainsDefault} from "@utils/defaultdata.js";
import {getBaseJsonAll, saveBaseJsonAll} from "@api/auto_plan/autoPlan.js";
import {ElMessage} from "element-plus";
import {goBack, toHomePage} from "@api/web/web.js";

const goToHome = async () => {
  await toHomePage();
};

const goToBack = async () => {
  await goBack();
};

const domainData = ref([]);
const selectedTypeItems = ref([]);
const selectedType = ref('');

// 弹窗控制
const showEditDialog = ref(false);
const showAddDialog = ref(false);
const editingItem = ref(null);
const editingIndex = ref(-1);

// 新增数据
const newItem = ref({
  name: '',
  type: '',
  hasOrder: true,
  list: []
});

// 按 type 分组
const groupedData = computed(() => {
  const groups = {};
  domainData.value.forEach(item => {
    if (!groups[item.type]) groups[item.type] = [];
    groups[item.type].push(item);
  });
  return groups;
});

// 选择类型
const selectType = (type) => {
  selectedType.value = type;
  selectedTypeItems.value = groupedData.value[type] || [];
};

// 获取最大 list 长度
const getMaxListLength = (type) => {
  return type === '圣遗物' ? 2 : 3;
};

// 创建默认 list 数组
const createDefaultList = (type) => {
  const length = getMaxListLength(type);
  return Array(length).fill(undefined);
};

// 打开新增弹窗
const openAddDialog = () => {
  newItem.value = {
    name: '',
    type: selectedType.value || '天赋',
    hasOrder: true,
    list: createDefaultList(selectedType.value || '天赋')
  };
  showAddDialog.value = true;
};

// 点击卡片进入编辑
const editSingleCard = (index) => {
  editingIndex.value = index;
  const item = selectedTypeItems.value[index];
  const maxLength = getMaxListLength(item.type);
  const newList = Array(maxLength).fill(undefined);
  if (item.list) {
    item.list.forEach((val, idx) => {
      if (idx < maxLength) newList[idx] = val;
    });
  }
  editingItem.value = {
    ...item,
    list: newList
  };
  showEditDialog.value = true;
};

// 关闭编辑弹窗
const closeEditDialog = () => {
  showEditDialog.value = false;
  editingItem.value = null;
  editingIndex.value = -1;
};

// 处理编辑时类型切换
const handleEditTypeChange = () => {
  const maxLength = getMaxListLength(editingItem.value.type);
  const newList = Array(maxLength).fill(undefined);
  editingItem.value.list.forEach((val, idx) => {
    if (idx < maxLength) newList[idx] = val;
  });
  editingItem.value.list = newList;
};

// 保存当前编辑的卡片
const saveCurrentItem = async () => {
  if (!editingItem.value.name.trim()) {
    ElMessage.warning('请填写秘境名称');
    return;
  }

  const originalItem = selectedTypeItems.value[editingIndex.value];
  const originalIndex = domainData.value.indexOf(originalItem);

  domainData.value[originalIndex] = {
    name: editingItem.value.name,
    type: editingItem.value.type,
    hasOrder: editingItem.value.hasOrder,
    list: editingItem.value.list.filter(item => item !== undefined)
  };

  selectType(selectedType.value);

  await saveAll()

  closeEditDialog();
  ElMessage.success('保存成功');
};
const saveAll = async () => {
  for (let val of domainData.value) {

    if (!val.name.trim()) {
      ElMessage.warning('请填写秘境名称');
      return;
    }
    if (val?.type?.trim() === '圣遗物') {
      val.hasOrder = false
    }
    val.list = val.list.filter(item => item !== undefined && item.trim() !== "");

    if (val?.list?.length <= 0) {
      ElMessage.warning('请填写物品列表');
      return;
    }

    if (val?.list?.length !== 2 && val?.type === '圣遗物') {
      ElMessage.warning(val?.type + '物品填写不全');
      return;
    } else if (val?.list?.length !== 3 && val?.type !== '圣遗物') {
      ElMessage.warning(val?.type + '物品填写不全');
      return;
    } else if (val?.list?.length > 2 && val?.type === '圣遗物') {
      ElMessage.warning('非法数据！');
      return;
    } else if (val?.list?.length > 3 && val?.type !== '圣遗物') {
      ElMessage.warning('非法数据！');
      return;
    }
  }
  await saveBaseJsonAll(domainData.value);
  ElMessage.success('保存成功');
};
// 删除当前编辑的卡片
const deleteCurrentItem = async () => {
  const originalItem = selectedTypeItems.value[editingIndex.value];
  const originalIndex = domainData.value.indexOf(originalItem);

  if (originalIndex > -1) {
    domainData.value.splice(originalIndex, 1);
    selectType(selectedType.value);
    await saveAll()
    closeEditDialog();
    ElMessage.success('已删除');
  }
};

// 处理新增时类型切换
const handleTypeChange = () => {
  newItem.value.list = createDefaultList(newItem.value.type);
};

// 确认新增
const confirmAddItem = async () => {
  if (!newItem.value.name.trim()) {
    ElMessage.warning('请填写秘境名称');
    return;
  }

  const itemToAdd = {
    name: newItem.value.name,
    type: newItem.value.type,
    hasOrder: newItem.value.hasOrder,
    list: newItem.value.list.filter(item => item !== undefined)
  };

  domainData.value.push(itemToAdd);
  selectType(selectedType.value);
  await saveAll()
  showAddDialog.value = false;
  ElMessage.success('添加成功');
};

// 获取数据
const fetchDomains = async () => {
  try {
    const response = await getBaseJsonAll();
    if (response && response.length > 0) {
      domainData.value = response;
    } else {
      ElMessage.warning('无数据存储，使用默认秘境数据。');
      domainData.value = JSON.parse(JSON.stringify(domainsDefault));
    }
  } catch (error) {
    console.error('请求失败:', error);
    ElMessage.warning('使用默认秘境数据。');
    domainData.value = JSON.parse(JSON.stringify(domainsDefault));
  }
};

onMounted(() => {
  fetchDomains();
});
</script>

<style scoped>
@import "@css/auto_plan_domain.css";

/* 新增占位卡片样式 */
.add-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 150px;
  border: 2px dashed #91dcd6;
  background: transparent;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-placeholder:hover {
  border-color: #00ffff;
  background: rgba(0, 255, 255, 0.1);
}

.add-icon {
  font-size: 48px;
  color: #91dcd6;
  margin-bottom: 8px;
}

.add-text {
  font-size: 16px;
  color: #91dcd6;
  font-weight: 600;
}

/* 弹窗样式 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.dialog-content {
  background: linear-gradient(135deg, #b6b2b6, #91dcd6);
  border-radius: 12px;
  padding: 24px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-shrink: 0;
}

.dialog-header h2 {
  color: #4195ff;
  font-size: 20px;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #94a3b8;
  cursor: pointer;
  padding: 0 8px;
}

.close-btn:hover {
  color: #ef4444;
}

.dialog-body {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 20px;
  min-height: 0;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #615959;
  font-weight: 600;
  font-size: 14px;
}

.field-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.9);
  box-sizing: border-box;
}

.field-input:focus {
  outline: none;
  border-color: #00ffff;
  box-shadow: 0 0 0 3px rgba(0, 255, 255, 0.1);
}

/* Select 美化样式 - 圆角 */
.field-select {
  width: 100%;
}

.field-select :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
  color: black;
}

.field-select :deep(.el-input__wrapper:hover) {
  border-color: #00ffff;
}

.field-select :deep(.el-input__wrapper.is-focus) {
  border-color: #00ffff;
  box-shadow: 0 0 0 3px rgba(0, 255, 255, 0.2);
}

.field-select :deep(.el-select-dropdown) {
  border-radius: 8px;
  overflow: hidden;
}

.field-select :deep(.el-select-dropdown__item) {
  border-radius: 4px;
  margin: 2px 8px;
}

.field-select :deep(.el-select-dropdown__item:hover) {
  background: linear-gradient(135deg, #4195ff, #00ffff);
  color: white;
}

.field-select :deep(.el-select-dropdown__item.is-selected) {
  background: linear-gradient(135deg, #4195ff, #00ffff);
  color: white;
  font-weight: 600;
}

.list-inputs {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.list-item-input {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-shrink: 0;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn.primary {
  background: linear-gradient(135deg, #4195ff, #00ffff);
  color: white;
}

.btn.success {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
}

.btn.danger {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
}

.btn.secondary {
  background: #6b7280;
  color: white;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 响应式 */
@media (max-width: 768px) {
  .dialog-content {
    width: 95%;
    max-height: 95vh;
    padding: 16px;
  }
}
</style>
