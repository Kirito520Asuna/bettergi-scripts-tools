<script setup>
import {goBack, toHomePage} from "@api/web/web.js";
import {computed, ref} from "vue";
import {ElMessage} from "element-plus";
import {
  getBaseBossListJsonAll,
  getBaseCountryJsonAll,
  saveBaseBossJsonAll,
  saveBaseCountryJsonAll
} from "@api/auto_plan/autoPlan.js";
import {bossListDefault, countryListDefault} from "@utils/defaultdata.js";

const goToHome = async () => {
  await toHomePage()
}

const goToBack = async () => {
  await goBack()
}
const cache = ref({
  country: {
    list: [],
    // newCountryName: '',
    // editingIndex: -1,
    // editValue: '',
  },
  boss: {
    list: [],
    countryList: [],
  }
})
const filteredList = computed(() => {
  const category = currentSelectedCategory.value;
  const name = category?.search?.name?.value ?? '';
  const country = category?.search?.country?.value ?? '';
  if (category.id === 'boss') {
    const list = cache.value.boss.list;
    if (name.trim() && country.trim()) {
      return list
    } else if (name.trim() && !country.trim()) {
      return list.filter(item =>
          (item.name ?? '').includes(name)
      )
    } else if (!name.trim() && country.trim()) {
      return list.filter(item =>
          (item.country ?? '').includes(country)
      )
    }
    return list.filter(item =>
        (item.name ?? '').includes(name) && (item.country ?? '').includes(country)
    );
  }
  return []
})
const constantsCategories = ref([
  {
    id: 'country',
    name: '原神国家配置',
    description: '原神国家筛选与配置',
    icon: '✨',
    header: {
      add: {
        key: 'add',
        title: '新增',
        icon: '+',
        async click() {
          const category = currentSelectedCategory.value
          await category?.method?.add()
        }
      },
      reset: {
        key: 'reset',
        title: '重置',
        icon: '↺',
        async click() {
          const category = currentSelectedCategory.value
          await category?.method?.reset()
        }
      },
      submit: {
        key: 'submit',
        title: '提交',
        icon: '✅',
        async click() {
          const category = currentSelectedCategory.value
          await category?.method?.submit(cache.value.country.list)
        }
      }
    },
    method: {
      async init() {
        await this.getAll()
        let value = cache.value;
        cache.value = value
      },
      async getAll() {
        let list = []
        const defaultCountryList = await countryListDefault();
        try {
          const countryList = await getBaseCountryJsonAll();
          list = countryList;
        } catch (e) {
          ElMessage.warning('获取国家列表失败，使用默认数据')
        }

        if (list.length <= 0) {
          list = defaultCountryList;
        }
        //去重
        const uniqueList = [...new Set(list)]
        cache.value.country.list = uniqueList
        return uniqueList
      },
      async delete(index) {
        const list = cache.value.country?.list;
        // 删除去重
        const newList = list
        newList.splice(index, 1);
        cache.value.country.list = newList
      },
      async submit(list = []) {
        try {
          const uniqueList = [...new Set(list)]
          await saveBaseCountryJsonAll(uniqueList)
        } finally {
          await this.getAll()
        }
      },
      async add(item) {
        const countryList = cache.value.country?.list;
        countryList.push(item)
        cache.value.country.list = countryList
      },
      async reset() {
        await this.getAll()
      }
    }
  },
  {
    id: 'boss',
    name: '原神BOSS配置',
    description: '原神BOSS筛选与配置',
    icon: '✨',
    search: {
      name: {
        title: '筛选Boss名称',
        value: '',
      },
      country: {
        title: '筛选国家分类',
        value: '',
      },
      async clear() {
        this.name.value = ''
        this.country.value = ''
      }
    },
    header: {
      add: {
        key: 'add',
        title: '新增',
        icon: '+',
        async click() {
          const category = currentSelectedCategory.value
          let add={name: category.search.name.value, country: category.search.country.value}
          await category?.method?.add(add)
        }
      },
      reset: {
        key: 'reset',
        title: '重置',
        icon: '↺',
        async click() {
          const category = currentSelectedCategory.value
          await category?.method?.reset()
        }
      },
      submit: {
        key: 'submit',
        title: '提交',
        icon: '✅',
        async click() {
          const category = currentSelectedCategory.value
          await category?.method?.submit(cache.value.boss.list)
        }
      }
    },
    method: {
      async init() {
        await this.getCountryAll()
        await this.getAll()
        let value = cache.value
        cache.value = value
      },
      async getAll() {
        let list = []
        const defaultBossList = [...bossListDefault]
        try {
          const BossList = await getBaseBossListJsonAll()
          list = BossList
        } catch (e) {
          ElMessage.warning('获取BOSS列表失败，使用默认数据')
        }

        if (list.length <= 0) {
          list = defaultBossList;
        }
        //去重
        const uniqueList = [...new Set(list)]
        cache.value.boss.list = uniqueList
        return uniqueList
      },
      async getCountryAll() {
        let list = []
        const defaultCountryList = await countryListDefault();
        try {
          const countryList = await getBaseCountryJsonAll();
          list = countryList;
        } catch (e) {
          ElMessage.warning('获取国家列表失败，使用默认数据')
        }

        if (list.length <= 0) {
          list = defaultCountryList;
        }
        //去重
        const uniqueList = [...new Set(list)]
        cache.value.boss.countryList = uniqueList
        return uniqueList
      },
      async delete(index,item) {
        const list = cache.value.boss?.list;
        // 删除去重
        const newList = list

        if (item) {
          const targetIndex = list.findIndex(i => i === item || (i.name === item.name && i.country === item.country));
          if (targetIndex === -1) {
            ElMessage.error('未找到要删除的项');
            return;
          }
          index=targetIndex
        }

        newList.splice(index, 1);
        ElMessage.warning('删除成功')
        await this.submit(newList)
      },
      async submit(list = []) {
        try {
          list = list.filter(i => i.name?.trim() && i.country?.trim())
          const uniqueList = [...new Set(list)]
          await saveBaseBossJsonAll(uniqueList)
        } finally {
          await this.getAll()
        }
      },
      async add(item = {name: '', country: ''}) {
        const countryList = cache.value.boss?.list;
        countryList.push(item)
        cache.value.boss.list = countryList
      },
      async reset() {
        await this.getAll()
      }
    }
  }
])
const selectedCategoryId = ref(null)
const currentSelectedCategory = ref(null)
const configData = ref({parts: []})
const selectCategory = (categoryId) => {
  selectedCategoryId.value = categoryId
  const category = constantsCategories.value.find(cat => cat.id === categoryId)
  if (category) {
    currentSelectedCategory.value = category
    configData.value = JSON.parse(JSON.stringify(category))
    category.method.init()
  }
}

const selectedCategory = computed(() => {
  return constantsCategories.value.find(cat => cat.id === selectedCategoryId.value)
})

</script>

<template>
  <div class="home">
    <div class="constants-container">
      <div class="page-header">
        <h1>常量配置中心</h1>
        <p class="subtitle">可视化管理和配置常量</p>
      </div>

      <div class="content">
        <!-- 左侧：分类列表 -->
        <div class="sidebar">
          <div class="sidebar-header">
            <span class="icon">📚</span>
            <h2>分类</h2>
          </div>
          <div class="content-list">
            <div
                v-for="category in constantsCategories"
                :key="category.id"
                :class="['content-item', { active: selectedCategoryId === category.id }]"
                @click="selectCategory(category.id)"
            >
              <span class="item-icon">{{ category.icon }}</span>
              <div class="item-info">
                <span class="item-name">{{ category.name }}</span>
                <span class="item-desc">{{ category.description }}</span>
              </div>
            </div>
          </div>
        </div>
        <!-- 右侧：可视化配置面板 -->
        <div class="main-content">
          <div v-if="currentSelectedCategory" class="config-panel">
            <div class="panel-header">
              <div class="header-left">
                <span class="header-icon">{{ currentSelectedCategory.icon }}</span>
                <div>
                  <h2>{{ currentSelectedCategory.name }}</h2>
                  <p class="header-desc">{{ currentSelectedCategory.description }}</p>
                </div>
              </div>
              <div :class="`header-center`" v-if="currentSelectedCategory?.search">
                <div :class="`search-row ${currentSelectedCategory.id}`" v-if="currentSelectedCategory.id==='boss'">
                  <el-input
                      :class="`search-input ${currentSelectedCategory.id}`"
                      v-if="currentSelectedCategory?.search?.name"
                      v-model="currentSelectedCategory.search.name.value"
                      :placeholder="`请输入${currentSelectedCategory.search.name.title}`"
                  />
                  <el-select
                      :class="`search-select ${currentSelectedCategory.id}`"
                      v-if="currentSelectedCategory?.search?.country"
                      v-model="currentSelectedCategory.search.country.value"
                      :placeholder="`请输入(输入/选择)${currentSelectedCategory.search.country.title}`"
                      filterable
                      allow-create
                      clearable style="width: 80%"
                  >
                    <el-option
                        v-for="country in cache.boss.countryList"
                        :key="country"
                        :value="country"
                        :label="country"
                    />
                  </el-select>

                  <el-button :class="`search-clear-btn ${currentSelectedCategory.id}`" @click="currentSelectedCategory.search.clear()">清空筛选</el-button>
                </div>
              </div>
              <div :class="`header-actions ${currentSelectedCategory.id}`">
                <!--                   v-if="selectedCategory.id === 'country'||selectedCategory.id ==='boss'"-->
                <button @click="currentSelectedCategory.header.add.click()"
                        :class="`btn btn-${currentSelectedCategory.header.add.key}`">
                  <span class="btn-icon">{{ currentSelectedCategory.header.add.icon }}</span>
                  {{ currentSelectedCategory.header.add.title }}
                </button>
                <button @click="currentSelectedCategory.header.reset.click()"
                        :class="`btn btn-${currentSelectedCategory.header.reset.key}`">
                  <span class="btn-icon">{{ currentSelectedCategory.header.reset.icon }}</span>
                  {{ currentSelectedCategory.header.reset.title }}
                </button>
                <button @click="currentSelectedCategory.header.submit.click()"
                        :class="`btn btn-${currentSelectedCategory.header.submit.key}`">
                  <span class="btn-icon">{{ currentSelectedCategory.header.submit.icon }}</span>
                  {{ currentSelectedCategory.header.submit.title }}
                </button>
              </div>
            </div>

            <div class="config-body">
              <!-- 国家配置器 -->
              <div v-if="currentSelectedCategory.id === 'country'" class="configurator">
                <!--  list for 国家卡片               -->
                <div :class="`config-section ${currentSelectedCategory.id}`">
                  <!-- 国家卡片列表 -->
                  <div v-if="cache?.country?.list?.length>0" class="country-list">
                    <div
                        v-for="(country, index) in cache.country.list"
                        :key="country + '-' + index"
                        class="country-card"
                    >
                      <!-- 展示模式 -->
                      <el-input v-model="cache.country.list[index]"/>
                      <div class="card-actions">
                        <button
                            class="btn btn-secondary"
                            @click="currentSelectedCategory.method.delete(index)"
                        >
                          🗑
                        </button>
                      </div>
                    </div>
                  </div>

                  <!-- 添加配置占位卡片 -->
                  <div :class="`add-config-placeholder ${currentSelectedCategory.id}`"
                       @click="currentSelectedCategory.method.add()">
                    <div class="placeholder-icon">+</div>
                    <div class="placeholder-text">添加新国家</div>
                    <div class="placeholder-hint">点击创建新的国家</div>
                  </div>
                </div>
              </div>

              <div v-else-if="currentSelectedCategory.id === 'boss'" class="configurator">
                <!--  list for boss卡片 编辑/新增用点击弹出窗口              -->
                <div :class="`config-section ${currentSelectedCategory.id}`">
                  <div v-if="cache?.boss?.list?.length>0" class="boss-list">
                    <div v-for="(boss,index) in filteredList" :key="index"
                         :class="`card ${currentSelectedCategory.id}`">
                      <div :class="`form-group ${currentSelectedCategory.id}`">
                        <label>BOSS名称:</label>
                        <el-input :class="`input ${selectedCategory.id}`" v-model="boss.name" placeholder="BOSS名称"/>
                      </div>

                      <div :class="`form-group ${currentSelectedCategory.id}`">
                        <label>国家:</label>
                        <el-select
                            v-model="boss.country"
                            placeholder="请(选择/输入)国家"
                            clearable
                            filterable
                            allow-create
                        >
                          <el-option
                              v-for="country in cache.boss.countryList"
                              :key="country"
                              :label="country"
                              :value="country"
                          />
                        </el-select>
                      </div>

                      <div class="card-actions">
                        <button
                            class="btn btn-secondary"
                            @click="currentSelectedCategory.method.delete(index,boss)"
                        >
                          🗑
                        </button>
                      </div>
                    </div>
                  </div>

                  <!-- 添加配置占位卡片 -->
                  <div :class="`add-config-placeholder ${currentSelectedCategory.id}`"
                       @click="currentSelectedCategory.method.add({name:currentSelectedCategory.search.name.value,country:currentSelectedCategory.search.country.value})">
                    <div class="placeholder-icon">+</div>
                    <div class="placeholder-text">添加新boss</div>
                    <div class="placeholder-hint">点击创建新的boss</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 未选择时的提示 -->
          <div v-else class="empty-state">
            <div class="empty-icon">👈</div>
            <h3>请选择一个分类</h3>
            <p>从左侧列表选择一个类型进行配置</p>
          </div>
        </div>
      </div>
    </div>


    <!-- 底部导航 -->
    <div class="fixed-back">
      <button class="btn secondary" @click="goToBack">返回上一页</button>
    </div>
    <div class="fixed-footer">
      <button class="btn secondary" @click="goToHome">🏠 返回主页</button>
    </div>
  </div>
</template>

<style scoped>
.constants-container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 40px 0;
  height: calc(100vh - 60px); /* 60px 是 footer 预留高度 */
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 禁止滚动 */
}


/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: 1rem;
  background: linear-gradient(90deg, #00ffff, #55e0ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;

  box-shadow: 0 15px 35px rgba(255, 0, 166, 0.3);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 10px;
}

.page-header h1 {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
  text-shadow: none;
}

.subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
  color: white;
}

/* 内容区域：左右两栏 */
.content {
  flex: 1;
  display: flex;
  gap: 28px;
  overflow: hidden; /* 防止左右布局溢出滚动 */
}

/* 左侧侧边栏 */
.sidebar {
  width: 280px;
  flex-shrink: 0;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column; /* 让子元素纵向排列 */
  overflow: hidden; /* 禁止 sidebar 本身滚动 */
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 22px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0; /* 固定高度不压缩 */
}

.sidebar-header .icon {
  font-size: 1.4rem;
}

.sidebar-header h2 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #334155;
}

.content-list {
  padding: 10px 12px;
}

.sidebar {
  width: 280px;
  flex-shrink: 0;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column; /* 让子元素纵向排列 */
  overflow: hidden; /* 禁止 sidebar 本身滚动 */
}

/*.sidebar-header {
  !* 保持现有样式，不需额外修改 *!
  flex-shrink: 0;            !* 固定高度不压缩 *!
}*/

.content-list {
  flex: 1; /* 占据剩余高度 */
  overflow-y: auto; /* ★ 允许垂直滚动 */
  padding: 10px 12px;
  /* 美化滚动条（可选） */
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}

.content-list::-webkit-scrollbar {
  width: 4px;
}

.content-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

.content-item {
  background: #eddfdf;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 4px;
}

.content-item:hover {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  transform: translateX(2px);
}

.content-item.active {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.25);
}

.content-item.active .item-name,
.content-item.active .item-desc {
  color: white;
}

.item-icon {
  font-size: 1.3rem;
  flex-shrink: 0;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
}

.item-name {
  font-weight: 600;
  font-size: 0.95rem;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-desc {
  font-size: 0.8rem;
  color: #94a3b8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 右侧主内容 */
.main-content {
  flex: 1;
  min-width: 0;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 禁止 main-content 整体滚动 */
}

/* 配置面板 */
.config-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.panel-header {
  flex-shrink: 0; /* 固定高度 */
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px;
  border-bottom: 1px solid #e2e8f0;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  font-size: 2rem;
  background: #f1f5f9;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.header-left h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 4px;
}

.header-desc {
  color: #64748b;
  font-size: 0.9rem;
}


.header-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* 按钮基础样式 */
.constants-container .btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  background: #f1f5f9;
  color: #334155;
}

.constants-container .btn:hover {
  filter: brightness(0.95);
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
}

.constants-container .btn .btn-icon {
  font-size: 1.1rem;
}

.constants-container .btn-add {
  background: #3b82f6;
  color: white;
}

.constants-container .btn-secondary {
  background: #e2e8f0;
  color: #334155;
}

.constants-container .btn-primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
}

.config-body {
  flex: 1; /* 占据剩余高度 */
  overflow-y: auto; /* ★ 允许垂直滚动 */
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  /* 滚动条美化 */
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}

.config-body::-webkit-scrollbar {
  width: 5px;
}

.config-body::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

/* 配置器内部占位 */
.configurator {
  border: 2px dashed #e2e8f0;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  background: #fafbfc;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 16px;
  opacity: 0.7;
}

.empty-state h3 {
  font-size: 1.3rem;
  color: #334155;
  margin-bottom: 8px;
}

.empty-state p {
  color: #94a3b8;
  font-size: 0.95rem;
}
/* ═══════ BOSS 搜索行 ═══════ */
.search-row {
  width: 400px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

/* BOSS 搜索输入框与选择框统一宽度 */
.search-input.boss,
.search-select.boss {
  width: 200px;                /* 固定宽度，可根据需要调整 */
}

/* 确保 el-select 内部触发器宽度撑满 */
.search-select.boss .el-select__trigger {
  width: 100%;
}

/* 清空按钮微调 */
.search-clear-btn.boss {
  height: 40px;                /* 与 Element Plus 控件高度一致 */
  padding: 0 16px;
  border-radius: 10px;
  font-weight: 500;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  color: #475569;
  transition: all 0.2s ease;
}

.search-clear-btn.boss:hover {
  background: #e2e8f0;
  border-color: #cbd5e1;
  color: #1e293b;
}

/* 输入框/选择框的圆角与边框微调 */
.search-input.boss .el-input__wrapper,
.search-select.boss .el-input__wrapper {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
}

.search-input.boss .el-input__wrapper:hover,
.search-select.boss .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px #3b82f6 inset;
}
/* ---------- BOSS 列表与卡片 ---------- */
.boss-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 10px;
}

/* BOSS 卡片 */
.boss-list .card {
  flex: 0 0 200px; /* 固定宽度 200px，不伸缩 */
  max-width: 100%; /* 小屏自适应 */
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.boss-list .card:hover {
  border-color: #3b82f6;
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.12);
  transform: translateY(-1px);
}

/* 表单行 */
.boss-list .form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.boss-list .form-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #475569;
}

/* 输入框 / 选择框统一宽度 */
.boss-list .form-group .el-input,
.boss-list .form-group .el-select {
  width: 100%;
}

/* 必填占位符提示色 */
.boss-list .form-group .el-input__inner::placeholder {
  color: #94a3b8;
}

/* 卡片操作区域（如果需要按钮可以预留） */
.boss-list .card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 4px;
}

/* 国家卡片 */
.country-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px 16px;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  margin-bottom: 10px;
}

.country-card:hover {
  border-color: #3b82f6;
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.12);
  transform: translateY(-1px);
}

.country-name {
  font-size: 1rem;
  font-weight: 500;
  color: #1e293b;
}

.card-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.card-actions .btn {
  padding: 6px 10px;
  font-size: 0.85rem;
}

.edit-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s;
}

.edit-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 新增国家输入区 */
.add-country-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.country-input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s;
}

.country-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.08);
}

.country-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 10px;
}

.country-list .country-card {
  flex: 0 0 200px; /* 固定宽度 280px，不伸缩 */
  max-width: 100%; /* 小屏幕时仍能自适应缩小 */
  margin-bottom: 0; /* 删除单独的外边距 */
}

/* 空状态 */
.empty-country {
  padding: 40px 20px;
  text-align: center;
  color: #94a3b8;
  background: #f8fafc;
  border-radius: 12px;
}

/* 列表过渡动画 */
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* 添加配置占位卡片 */
.add-config-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px 20px;
  margin-top: 16px;
  border: 2px dashed #cbd5e1;
  border-radius: 16px;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.25s ease;
  text-align: center;
  user-select: none;
}

.add-config-placeholder:hover {
  border-color: #3b82f6;
  background: #f0f7ff;
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.12);
  transform: translateY(-2px);
}

.add-config-placeholder:active {
  transform: translateY(0);
}

.placeholder-icon {
  font-size: 2.5rem;
  font-weight: 300;
  color: #94a3b8;
  width: 56px;
  height: 56px;
  line-height: 56px;
  border-radius: 50%;
  background: #e2e8f0;
  transition: all 0.25s ease;
}

.add-config-placeholder:hover .placeholder-icon {
  background: #3b82f6;
  color: #fff;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.35);
}

.placeholder-text {
  font-size: 1rem;
  font-weight: 600;
  color: #334155;
}

.placeholder-hint {
  font-size: 0.8rem;
  color: #94a3b8;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .constants-container {
    padding: 20px 16px 90px;
  }

  .content {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }

  .panel-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>