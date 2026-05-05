<template>
  <div class="home">
    <div class="syntax-key-container">
      <div class="page-header">
        <h1>语法配置中心</h1>
        <p class="subtitle">可视化管理和配置各类脚本语法</p>
      </div>

      <div class="content-wrapper">
        <!-- 左侧：语法分类列表 -->
        <div class="sidebar">
          <div class="sidebar-header">
            <span class="icon">📚</span>
            <h2>语法分类</h2>
          </div>
          <div class="syntax-list">
            <div
                v-for="category in syntaxCategories"
                :key="category.id"
                :class="['syntax-item', { active: selectedCategoryId === category.id }]"
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
          <div v-if="selectedCategory" class="config-panel">
            <div class="panel-header">
              <div class="header-left">
                <span class="header-icon">{{ selectedCategory.icon }}</span>
                <div>
                  <h2>{{ selectedCategory.name }}</h2>
                  <p class="header-desc">{{ selectedCategory.description }}</p>
                </div>
              </div>
              <div class="header-actions">
                <button @click="addPart" class="btn btn-add">
                  <span class="btn-icon">+</span>
                  添加部件
                </button>
                <button @click="resetConfig" class="btn btn-secondary">
                  <span class="btn-icon">↺</span>
                  重置
                </button>
                <button @click="showPreviewDialog = true" class="btn btn-primary">
                  <span class="btn-icon">👁️</span>
                  查看语法
                </button>
              </div>
            </div>

            <div class="config-body">
              <!-- 圣遗物强化配置器 -->
              <div v-if="selectedCategory.id === 'holy_relics_up'" class="relic-configurator">
                <div class="config-section">
                  <h3>🎯 部件配置</h3>
                  <p class="section-hint">选择要配置的圣遗物部件及其属性</p>

                  <!-- 部件列表 -->
                  <div class="parts-container">
                    <div
                        v-for="(part, partIndex) in configData.parts"
                        :key="partIndex"
                        class="part-card"
                    >
                      <div class="part-header">
                        <span class="part-number">部件 {{ partIndex + 1 }}</span>
                        <button
                            v-if="configData.parts.length > 1"
                            @click="removePart(partIndex)"
                            class="btn-remove-part"
                            title="删除此部件"
                        >
                          ×
                        </button>
                      </div>

                      <!-- 选择部件类型 -->
                      <div class="part-type-selector">
                        <label class="field-label">部件类型：</label>
                        <div class="type-options">
                          <button
                              v-for="type in relicTypes"
                              :key="type.value"
                              :class="['type-btn', { active: part.type === type.value }]"
                              @click="setPartType(partIndex, type.value)"
                          >
                            {{ type.label }}
                          </button>
                        </div>
                      </div>

                      <!-- 主词条配置 -->
                      <div class="main-stats-section">
                        <label class="field-label">主词条{{ getPartTypeName(part.type) }}：</label>
                        <div class="stats-selector">
                          <div class="selected-stats">
                            <span
                                v-for="(stat, statIndex) in part.mainStats"
                                :key="statIndex"
                                class="stat-tag main-stat"
                            >
                              {{ getStatLabel(stat) }}
                              <button @click="removeMainStat(partIndex, statIndex)" class="tag-remove">×</button>
                            </span>
                            <span v-if="part.mainStats.length === 0" class="empty-hint">未选择主词条</span>
                          </div>
                          <div class="available-stats">
                            <button
                                v-for="stat in getAvailableMainStats(part.type)"
                                :key="stat.value"
                                :disabled="part.mainStats.includes(stat.value)"
                                @click="addMainStat(partIndex, stat.value)"
                                class="stat-option-btn"
                            >
                              {{ stat.label }}
                            </button>
                          </div>
                        </div>
                      </div>

                      <!-- 副词条配置 -->
                      <div class="sub-stats-section">
                        <label class="field-label">副词条：</label>
                        <div class="stats-selector">
                          <div class="selected-stats">
                            <span
                                v-for="(stat, statIndex) in part.subStats"
                                :key="statIndex"
                                class="stat-tag sub-stat"
                            >
                              {{ getStatLabel(stat) }}
                              <button @click="removeSubStat(partIndex, statIndex)" class="tag-remove">×</button>
                            </span>
                            <span v-if="part.subStats.length === 0" class="empty-hint">未选择副词条</span>
                          </div>
                          <div class="available-stats">
                            <button
                                v-for="stat in getAvailableSubStats(part)"
                                :key="stat.value"
                                :disabled="part.subStats.includes(stat.value)"
                                @click="addSubStat(partIndex, stat.value)"
                                class="stat-option-btn"
                            >
                              {{ stat.label }}
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>

                    <!-- 添加部件按钮 -->
                    <button @click="addPart" class="btn-add-part">
                      <span class="add-icon">+</span>
                      添加新部件
                    </button>
                  </div>
                </div>
              </div>

              <!-- 其他语法类型的占位 -->
              <div v-else class="placeholder-config">
                <p>该语法类型的可视化配置器正在开发中...</p>
              </div>
            </div>
          </div>

          <!-- 未选择时的提示 -->
          <div v-else class="empty-state">
            <div class="empty-icon">👈</div>
            <h3>请选择一个语法分类</h3>
            <p>从左侧列表选择一个语法类型进行配置</p>
          </div>
        </div>
      </div>

      <!-- 语法预览对话框 -->
      <el-dialog
          v-model="showPreviewDialog"
          title="📝 生成的语法"
          width="600px"
          :close-on-click-modal="false"
      >
        <div class="dialog-content">
          <div class="preview-box">
            <code>{{ generatedSyntax }}</code>
          </div>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <button @click="copyAndClose" class="btn-copy">
              📋 复制到剪贴板
            </button>
            <button @click="showPreviewDialog = false" class="btn-close">
              关闭
            </button>
          </div>
        </template>
      </el-dialog>
    </div>


    <!-- 底部导航 -->
    <div class="fixed-footer">
      <button @click="goToBack" class="btn secondary">返回上一页</button>

      <button @click="goToHome" class="btn secondary">🏠 返回主页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { goBack, toHomePage } from '@api/web/web.js'
import { ElMessage } from 'element-plus'
import {CopyToClipboard} from "@utils/local.js";

// 圣遗物类型定义
const relicTypes = [
  { value: 'flower', label: '花', fullName: '生之花' },
  { value: 'feather', label: '羽', fullName: '死之羽' },
  { value: 'sand', label: '沙', fullName: '时之沙' },
  { value: 'goblet', label: '杯', fullName: '空之杯' },
  { value: 'crown', label: '冠', fullName: '理之冠' }
]

// 可用属性定义
const allStats = [
  { value: 'hp', label: '生命', hasPercent: true },
  { value: 'hp_percent', label: '生命%', hasPercent: false },
  { value: 'atk', label: '攻击', hasPercent: true },
  { value: 'atk_percent', label: '攻击%', hasPercent: false },
  { value: 'def', label: '防御', hasPercent: true },
  { value: 'def_percent', label: '防御%', hasPercent: false },
  { value: 'heal', label: '治疗' },
  { value: 'mastery', label: '精通' },
  { value: 'crit_rate', label: '暴率' },
  { value: 'crit_dmg', label: '爆伤' },
  { value: 'recharge', label: '充能' },
  { value: 'physical', label: '物伤' },
  { value: 'anemo', label: '风伤' },
  { value: 'hydro', label: '水伤' },
  { value: 'electro', label: '雷伤' },
  { value: 'geo', label: '岩伤' },
  { value: 'dendro', label: '草伤' },
  { value: 'cryo', label: '冰伤' },
  { value: 'pyro', label: '火伤' }
]

// 元素伤害类型（仅杯的主词条）
const elementDamageTypes = ['physical', 'anemo', 'hydro', 'electro', 'geo', 'dendro', 'cryo', 'pyro']

// 可用的主词条（根据部件类型动态计算）
const getAvailableMainStats = (partType) => {
  if (partType === 'flower') {
    // 花固定生命
    return allStats.filter(stat => stat.value === 'hp')
  } else if (partType === 'feather') {
    // 羽毛固定攻击
    return allStats.filter(stat => stat.value === 'atk')
  } else if (partType === 'goblet') {
    // 杯：元素伤害 + 精通，无双爆、无充能、无治疗
    return allStats.filter(stat =>
        elementDamageTypes.includes(stat.value) ||
        stat.value === 'mastery' ||
        stat.value === 'hp_percent' ||
        stat.value === 'atk_percent' ||
        stat.value === 'def_percent'
    )
  } else if (partType === 'crown') {
    // 冠：治疗 + 双爆 + 精通 + 生命%攻击%防御%，排除元素伤害和充能
    return allStats.filter(stat =>
        !elementDamageTypes.includes(stat.value) &&
        stat.value !== 'recharge' &&
        stat.value !== 'hp' &&
        stat.value !== 'atk' &&
        stat.value !== 'def'
    )
  } else {
    // 沙：生命%攻击%防御%精通充能，排除元素伤害、治疗、固定值、双爆
    return allStats.filter(stat =>
        !elementDamageTypes.includes(stat.value) &&
        stat.value !== 'heal' &&
        stat.value !== 'hp' &&
        stat.value !== 'atk' &&
        stat.value !== 'def' &&
        stat.value !== 'crit_rate' &&
        stat.value !== 'crit_dmg'
    )
  }
}

// 可用的副词条（排除元素伤害和治疗）
const getAvailableSubStats = (part) => {
  const excludedStats = [...elementDamageTypes, 'heal']

  return allStats.filter(stat => !excludedStats.includes(stat.value))
}


// 获取属性显示标签
const getStatLabel = (statValue) => {
  const stat = allStats.find(s => s.value === statValue)
  if (!stat) return statValue

  // 检查是否有百分比后缀
  const parts = statValue.split('%')
  const baseValue = parts[0]
  const hasPercent = parts.length > 1

  if (hasPercent && stat.hasPercent) {
    return `${stat.label}%`
  }
  return stat.label
}

// 语法分类配置
const syntaxCategories = ref([
  {
    id: 'holy_relics_up',
    name: '圣遗物强化语法',
    description: '圣遗物筛选与强化配置',
    icon: '✨',
    defaultConfig: {
      parts: [
        {
          type: 'flower',
          mainStats: [],
          subStats: ['crit_rate', 'crit_dmg']
        },
        {
          type: 'feather',
          mainStats: [],
          subStats: ['crit_rate', 'crit_dmg']
        },
        {
          type: 'sand',
          mainStats: [],
          subStats: ['crit_rate', 'crit_dmg']
        },
        {
          type: 'crown',
          mainStats: ['crit_rate', 'crit_dmg'],
          subStats: ['crit_rate', 'crit_dmg']
        },
        {
          type: 'goblet',
          mainStats: ['physical', 'anemo', 'hydro', 'electro', 'geo', 'dendro', 'cryo', 'pyro'],
          subStats: ['crit_rate', 'crit_dmg']
        }
      ]
    }
  }
])

const selectedCategoryId = ref(null)
const configData = ref({ parts: [] })
const showPreviewDialog = ref(false)

const selectedCategory = computed(() => {
  return syntaxCategories.value.find(cat => cat.id === selectedCategoryId.value)
})

// 生成语法字符串
const generatedSyntax = computed(() => {
  if (!configData.value.parts || configData.value.parts.length === 0) {
    return ''
  }

  const parts = configData.value.parts.map(part => {
    let partStr = `@${getPartSymbol(part.type)}`
    // 添加主词条
    if (part.mainStats && part.mainStats.length > 0) {
      partStr += '#'
      part.mainStats.forEach((stat, index) => {
        if (index > 0) partStr += '#'
        partStr += formatStat(stat)
      })
      partStr += '&'
    }
    // 添加副词条
    if (part.subStats && part.subStats.length > 0) {
      part.subStats.forEach(stat => {
        partStr += `*${formatStat(stat)}`
      })
    }
    return partStr
  })

  return parts.join('|')
})

// 获取部件符号
const getPartSymbol = (type) => {
  const typeMap = {
    flower: '花',
    feather: '羽',
    sand: '沙',
    goblet: '杯',
    crown: '冠'
  }
  return typeMap[type] || type
}

// 格式化属性
const formatStat = (statValue) => {
  const stat = allStats.find(s => s.value === statValue)
  if (!stat) return statValue

  const parts = statValue.split('%')
  const baseValue = parts[0]
  const hasPercent = parts.length > 1

  if (hasPercent && stat.hasPercent) {
    return `${stat.label}%`
  }
  return stat.label
}

onMounted(() => {
  if (syntaxCategories.value.length > 0) {
    selectCategory(syntaxCategories.value[0].id)
  }
})

const selectCategory = (categoryId) => {
  selectedCategoryId.value = categoryId
  const category = syntaxCategories.value.find(cat => cat.id === categoryId)
  if (category) {
    configData.value = JSON.parse(JSON.stringify(category.defaultConfig))
  }
}

// 部件操作
const addPart = () => {
  configData.value.parts.push({
    type: 'flower',
    mainStats: [],
    subStats: []
  })
}

const removePart = (index) => {
  configData.value.parts.splice(index, 1)
}

const setPartType = (partIndex, type) => {
  configData.value.parts[partIndex].type = type
}

// 主词条操作
const addMainStat = (partIndex, stat) => {
  const part = configData.value.parts[partIndex]
  if (!part.mainStats.includes(stat)) {
    part.mainStats.push(stat)
  }
}

const removeMainStat = (partIndex, statIndex) => {
  configData.value.parts[partIndex].mainStats.splice(statIndex, 1)
}

// 副词条操作
const addSubStat = (partIndex, stat) => {
  const part = configData.value.parts[partIndex]
  if (!part.subStats.includes(stat)) {
    part.subStats.push(stat)
  }
}

const removeSubStat = (partIndex, statIndex) => {
  configData.value.parts[partIndex].subStats.splice(statIndex, 1)
}

const resetConfig = () => {
  const category = syntaxCategories.value.find(cat => cat.id === selectedCategoryId.value)
  if (category) {
    configData.value = JSON.parse(JSON.stringify(category.defaultConfig))
    ElMessage.success('已重置为默认配置 ✨')
  }
}

const copySyntax = async () => {
    await CopyToClipboard(generatedSyntax.value)
}

const copyAndClose = async () => {
  await copySyntax()
  showPreviewDialog.value = false
}

const goToHome = async () => {
  await toHomePage()
}

const goToBack = async () => {
  await goBack()
}

// 获取部件类型名称
const getPartTypeName = (type) => {
  if (type === 'flower') return '（固定生命）'
  if (type === 'feather') return '（固定攻击）'
  return '（可选）'
}
</script>

<style scoped>
.home {
  height: 100vh;
  width: 100vw;
}
.syntax-key-container {
  min-height: 100vh;
  width: 100vw;
  padding: 2rem;
  position: relative;

  /*  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);*/
  background: url("@assets/MHY_XTLL.png");
  background-attachment: fixed; /* ← 核心属性 */
  background-size: cover; /* 覆盖整个容器 */
  background-position: center;
}

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

.content-wrapper {
  display: flex;
  gap: 2rem;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  height: calc(100vh - 200px);
}

.sidebar {
  width: 320px;
  background: #f8f9fa;
  border-right: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.sidebar-header .icon {
  font-size: 1.5rem;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 1.25rem;
}

.syntax-list {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
}

.syntax-item {
  padding: 1rem;
  margin-bottom: 0.5rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 1rem;
  border: 2px solid transparent;
}

.syntax-item:hover {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateX(4px);
}

.syntax-item.active {
  background: white;
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.item-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.item-name {
  font-weight: 600;
  color: #2d3748;
  font-size: 1rem;
}

.item-desc {
  font-size: 0.85rem;
  color: #718096;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.config-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.panel-header {
  padding: 2rem;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header-icon {
  font-size: 2.5rem;
}

.header-left h2 {
  margin: 0 0 0.25rem 0;
  font-size: 1.75rem;
}

.header-desc {
  margin: 0;
  opacity: 0.9;
  font-size: 0.95rem;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
}

.content-wrapper .btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.content-wrapper .btn-icon {
  font-size: 1.1rem;
}

.content-wrapper .btn-primary {
  background: white;
  color: #f5576c;
}

.content-wrapper .btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.content-wrapper .btn-secondary {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  backdrop-filter: blur(10px);
}

.content-wrapper .btn-secondary:hover {
  background: rgba(255, 255, 255, 0.3);
}

.config-body {
  flex: 1;
  overflow-y: auto;
  padding: 2rem;
  background: #f8f9fa;
}

.relic-configurator {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.config-section {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.config-section h3 {
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 1.4rem;
}

.section-hint {
  margin: 0 0 1.5rem 0;
  color: #718096;
  font-size: 0.95rem;
}

.parts-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 1.5rem;
}

.part-card {
  background: #f7fafc;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
  min-width: 0;
}


.part-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.part-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.part-number {
  font-weight: 600;
  color: #667eea;
  font-size: 1.1rem;
}

.btn-remove-part {
  width: 32px;
  height: 32px;
  border: none;
  background: #fc8181;
  color: white;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.5rem;
  line-height: 1;
  transition: all 0.3s ease;
}

.btn-remove-part:hover {
  background: #f56565;
  transform: scale(1.1);
}

.field-label {
  display: block;
  font-weight: 600;
  color: #4a5568;
  margin-bottom: 0.75rem;
  font-size: 0.95rem;
}

.type-options {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.type-btn {
  padding: 0.6rem 1.2rem;
  border: 2px solid #cbd5e0;
  background: white;
  color: #4a5568;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.type-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.type-btn.active {
  background: #667eea;
  border-color: #667eea;
  color: white;
}

.stats-selector {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.selected-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  min-height: 40px;
  padding: 0.75rem;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
}

.stat-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 0.8rem;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
}

.main-stat {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.sub-stat {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.tag-remove {
  background: rgba(255, 255, 255, 0.3);
  border: none;
  color: white;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.tag-remove:hover {
  background: rgba(255, 255, 255, 0.5);
  transform: scale(1.1);
}

.empty-hint {
  color: #a0aec0;
  font-style: italic;
}

.available-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.stat-option-btn {
  padding: 0.5rem 1rem;
  border: 2px solid #e2e8f0;
  background: white;
  color: #4a5568;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.stat-option-btn:hover:not(:disabled) {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
}

.stat-option-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-add-part {
  padding: 1.2rem;
  border: 2px dashed #cbd5e0;
  background: transparent;
  color: #667eea;
  border-radius: 12px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.add-icon {
  font-size: 1.5rem;
}

.btn-add-part:hover {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.05);
  transform: translateY(-2px);
}

.preview-box {
  background: #1a202c;
  padding: 1.5rem;
  border-radius: 8px;
  overflow-x: auto;
}

.preview-box code {
  color: #68d391;
  font-family: 'Courier New', monospace;
  font-size: 0.95rem;
  word-break: break-all;
}

.dialog-content {
  padding: 1rem 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.btn-copy {
  padding: 0.75rem 1.5rem;
  border: 2px solid #667eea;
  background: white;
  color: #667eea;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-copy:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
}

.btn-close {
  padding: 0.75rem 1.5rem;
  border: 2px solid #e2e8f0;
  background: white;
  color: #4a5568;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-close:hover {
  border-color: #cbd5e0;
  background: #f7fafc;
}

.placeholder-config {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #a0aec0;
  font-size: 1.2rem;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #a0aec0;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.empty-state h3 {
  margin: 0 0 0.5rem 0;
  color: #718096;
}

.empty-state p {
  margin: 0;
  font-size: 0.95rem;
}



@media (max-width: 1024px) {
  .content-wrapper {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    max-height: 300px;
  }

  .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
