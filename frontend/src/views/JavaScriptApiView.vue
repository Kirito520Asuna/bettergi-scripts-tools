<script setup>
// 跳转主页
import {goBack, toHomePage} from "@api/web/web.js";
import router from "@router/router.js";
import {computed, onMounted, ref} from "vue";
import {ElMessageBox} from "element-plus";
import {CopyToClipboard} from "@utils/local.js";
import {getHostPrefix} from "@utils/ApiRequest.js";
import {getTokenInfo} from "@api/auth/token.js";

const currentRoute = ref(router.currentRoute)
const goToHome = async () => {
  await toHomePage()
}

// 返回上一页
const goToBack = async () => {
  await goBack()
}
const hostPrefix = getHostPrefix();
const JsApiList = ref([])
onMounted(async () => {
  const response = await getTokenInfo()
  let tokenInfo = {
    name: undefined,
    value: undefined,
  }
  if (response.code === 200) {
    tokenInfo.name = response.data.name || '';
    tokenInfo.value = response.data.value || '';
  }
  let token = (tokenInfo?.name && tokenInfo?.value) ? tokenInfo?.name + "=" + tokenInfo?.value : "未设置,如需请前往设置配置";
  JsApiList.value.push({
    name: "自动体力计划API",
    list: [
      {
        name: '拉取配置API',
        value: hostPrefix + 'auto/plan/json',
      },
      {
        name: '推送全部配置API',
        value: hostPrefix + 'auto/plan/domain/json/all',
      },
      {
        name: '推送全部国家配置API',
        value: hostPrefix + 'auto/plan/country/json/all',
      },
      {
        name: '授权Token',
        value: token,
        to: {
          text: '前往设置',
          desc: '点击前往设置授权Token',
          value: 'settings',
          click: async (value) => {
            await ElMessageBox.confirm(
                '确定前往设置吗？',
                '提示', {
                  confirmButtonText: '确定',
                  cancelButtonText: '取消',
                  type: 'warning',
                }
            )
            router.push({name: value})
          }
        }
      },
    ]
  })

  JsApiList.value.push({
    name: "全自动或半自动工具箱API",
    list: [
      {
        name: "CD算法API",
        value: hostPrefix + "/cron/next-timestamp/all",
      }
    ]
  })
})
const searchKeyword = ref('')
const filteredGroups = computed(() => {
  if (!searchKeyword.value.trim()) {
    return JsApiList.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return JsApiList.value.filter(group =>
      group.name.toLowerCase().includes(keyword)
  )
})

const expandedGroups = ref(new Set())

const toggleGroup = (idx) => {
  if (expandedGroups.value.has(idx)) {
    expandedGroups.value.delete(idx)
  } else {
    expandedGroups.value.add(idx)
  }
}

const isExpanded = (idx) => {
  return expandedGroups.value.has(idx)
}

</script>

<template>
  <div class="home">
    <div class="container">
      <div class="header-section">
        <h2 class="title">{{ currentRoute.meta.title }}</h2>
        <p class="subtitle">快速访问和管理脚本接口</p>
        <div class="search-box">
          <el-input
              v-model="searchKeyword"
              placeholder="搜索 API 分组名称..."
              prefix-icon="Search"
              clearable
              class="search-input"
          />
        </div>
      </div>

      <div class="api-groups">
        <div v-for="(group, idx) in filteredGroups" :key="idx" class="api-group-card">
          <div class="group-header" @click="toggleGroup(idx)">
            <div class="group-icon">📡</div>
            <h3 class="group-title">{{ group.name }}</h3>
            <span class="expand-icon" :class="{ 'rotated': isExpanded(idx) }">▼</span>
          </div>

          <transition name="expand">
            <div v-if="isExpanded(idx)" class="api-list">
              <div v-for="(item, index) in group.list" :key="index" class="api-item-card">
                <div class="api-item-header">
                  <div class="api-info">
                    <span class="api-name">{{ item.name }}</span>
                    <el-tag size="small" type="primary" effect="plain">API</el-tag>
                  </div>
                </div>

                <div class="api-value-box">
                  <code class="api-value">{{ item.value }}</code>
                </div>

                <div class="api-actions">
                  <el-tooltip v-if="item.to" :content="item.to.desc" placement="top">
                    <el-button
                        type="success"
                        size="small"
                        icon="Position"
                        @click="item.to.click(item.to.value)"
                        class="action-btn"
                    >
                      {{ item.to.text }}
                    </el-button>
                  </el-tooltip>

                  <el-tooltip content="复制到剪贴板" placement="top">
                    <el-button
                        type="primary"
                        size="small"
                        icon="DocumentCopy"
                        @click="CopyToClipboard(item.value)"
                        class="action-btn"
                    >
                      复制
                    </el-button>
                  </el-tooltip>
                </div>
              </div>
            </div>
          </transition>

          <div v-if="isExpanded(idx) && (!group.list || group.list.length === 0)" class="empty-state">
            <el-empty description="暂无可用 API" :image-size="100"/>
          </div>
        </div>
      </div>
    </div>
    <!-- 底部按钮 -->
    <div class="fixed-back">
      <button @click="goToBack" class="btn secondary">返回上一页</button>
    </div>
    <div class="fixed-footer">
      <button @click="goToHome" class="btn secondary">🏠 返回主页</button>
    </div>
  </div>
</template>

<style scoped>

.container {
  width: 80vw;
  height: 80vh;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3),
  inset 0 0 0 1px rgba(255, 255, 255, 0.1);
  padding: 40px;
  overflow-y: auto;
  position: relative;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.container::-webkit-scrollbar {
  display: none;
}

.container::before {
  width: 80vw;
  height: 80vh;
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #fbfdfb, #959191, #00ccff, #faa76c);
  border-radius: 26px;
  z-index: -1;
  opacity: 0.3;
  filter: blur(20px);
}

.header-section {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  color: white;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 10px;
  text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.2);
}

.subtitle {
  color: rgba(255, 255, 255, 0.9);
  font-size: 1.1rem;
  margin: 0;
}

.search-box {
  max-width: 500px;
  margin: 0 auto;
}

.search-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  padding: 12px 16px;
}

.search-input :deep(.el-input__inner) {
  font-size: 1rem;
}

.api-groups {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 20px;
  max-height: calc(100% - 120px);
  overflow-y: auto;
  padding-right: 10px;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.api-groups::-webkit-scrollbar {
  display: none;
}

.api-group-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.api-group-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
}

.group-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 0;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.3s ease;
  user-select: none;
}

.group-header:hover {
  background: rgba(102, 126, 234, 0.1);
}

.group-icon {
  font-size: 2rem;
}

.group-title {
  margin: 0;
  font-size: 1.5rem;
  color: #333;
  font-weight: 600;
  flex: 1;
}

.expand-icon {
  font-size: 1rem;
  color: #999;
  transition: transform 0.3s ease;
}

.expand-icon.rotated {
  transform: rotate(180deg);
}

.api-list {
  display: grid;
  gap: 16px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px solid #f0f0f0;
}

.api-item-card {
  background: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
}

.api-item-card:hover {
  background: white;
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
}

.api-item-header {
  margin-bottom: 12px;
}

.api-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.api-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
}

.api-value-box {
  background: #1e1e1e;
  border-radius: 8px;
  padding: 16px;
  margin: 12px 0;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.api-value-box::-webkit-scrollbar {
  display: none;
}

.api-value {
  color: #4ec9b0;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 0.95rem;
  line-height: 1.6;
  word-break: break-all;
}

.api-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.action-btn {
  flex: 1;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.empty-state {
  padding: 40px 0;
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
  margin-top: 0;
  padding-top: 0;
}

.expand-enter-to,
.expand-leave-from {
  opacity: 1;
  max-height: 2000px;
}
</style>