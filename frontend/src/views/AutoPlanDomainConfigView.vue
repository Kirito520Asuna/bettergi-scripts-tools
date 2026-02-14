<script setup>
import {ref, computed, watch, watchEffect, onMounted} from 'vue'
import {ElMessage} from "element-plus";
import {getBaseJsonAll, getUidJson, postUidJson, removeUidList} from "@api/domain/autoPlan";
import {CopyToClipboard} from "@utils/local.js";
import {domainsDefault, domainTypesDefault, excludeDomainTypesDefault, selectedAsDaysMap} from "@utils/defaultdata.js";
import router from "@router/router.js";
import draggable from 'vuedraggable'

// 配置列表 → 核心数据结构改为 array
const configs = ref([])
const currentConfig = ref([])
const isLoading = ref(false);
// 秘境数据（保持不变，建议单独抽到一个文件）
const defaultDomains = domainsDefault
const domains = ref([])
const domainTypes = ref([])
const excludeDomainTypes = ref(new Array())
const initDomainTypes = async () => {
  const types = [
    {value: '', label: '请选择类型'}
  ]
  const list = domainTypesDefault();
  list.forEach(item => {
    types.push({value: item, label: item})
  })
  domainTypes.value = types

  const excludes = excludeDomainTypesDefault()
  excludeDomainTypes.value.push(...excludes)
}
const fetchDomains = async () => {
  isLoading.value = true;
  try {
    // const response = await service.get('/auto/plan/domain/json/all');
    const response = await getBaseJsonAll()
    // console.log('response', response)
    if (response && response.length > 0) {
      domains.value = response;
    } else {
      domains.value = defaultDomains;
      ElMessage.warning('无数据存储，使用默认秘境数据。');
    }
  } catch (error) {
    console.error('请求失败:', error);
    domains.value = defaultDomains;
    ElMessage.warning('使用默认秘境数据。');
  } finally {
    isLoading.value = false;
  }
};
const removeConfigToBackend = async () => {
  if (!uid.value) {
    ElMessage.warning("请先设置 UID");
    return;
  }

  let ids = []
  ids.push(uid.value)
  const uidStr = ids.join(',');
  await removeUidList(uidStr)
  return
}
const submitConfigToBackend = async () => {
  if (!uid.value) {
    ElMessage.warning("请先设置 UID");
    return;
  }
  const json = getFinalConfigs()
  await postUidJson(uid.value, JSON.stringify(json))
};
const findDomains = async () => {
  if (!uid.value) {
    ElMessage.warning("请先设置 UID");
    return;
  }

  try {
    const response = await getUidJson(uid.value)
    configs.value = response;
  } catch (error) {
    console.error('请求失败:', error);
    ElMessage.error( error.message);
  } finally {
  }
};
const asDaysMap = selectedAsDaysMap()
onMounted(() => {
  fetchDomains();
  initDomainTypes()
})
// 在 script 中添加跳转逻辑
const goToHome = () => {
  router.push('/'); // 假设主页路径是 '/'
};
const showResultDrawer = ref(false)
const orderSortConfigs = ref(false)
const uid = ref("")
// 新增一条空白配置
const addConfig = () => {
  const newOrder = configs.value.length === 0
      ? 1
      : Math.max(...configs.value.map(c => c.order)) + 1

  configs.value.push({
    order: newOrder,
    // day: undefined,
    days: [],
    dayName: undefined,
    showDaysSelector: false,   // ← 新增
    showPhysicalSelector: false,   // ← 新增
    showDaysButton: true,   // ← 新增
    // daysName: [],
    selectedType: "", // 新增字段
    autoFight: {
      physical: [
        {order: 0, name: "原粹树脂", open: true},
        {order: 1, name: "浓缩树脂", open: false},
        {order: 2, name: "须臾树脂", open: false},
        {order: 3, name: "脆弱树脂", open: false}
      ],
      domainName: undefined,
      partyName: undefined,
      sundaySelectedValue: undefined,
      // sundaySelectedName: undefined,
      DomainRoundNum: 1
    }
  })

  changSortConfigs()

}
const removeConfigAll = () => {
  configs.value = []
}
// 删除某一条
const removeConfig = (index) => {
  configs.value = configs.value.filter(c => c !== configs.value[index])
  // 可选：重新排序 order（如果前端需要显示连续的序号）
  // configs.value.forEach((c, i) => { c.order = i + 1 })
}
const removeConfigMo = (indexList) => {
  for (let index of indexList) {
    removeConfig(index)
  }
}
const filteredDomainsType = ((selectedType) => {
  if (!selectedType) return [];
  return domains.value.filter(d => d.type === selectedType);
});
// 为每一条配置找到对应的秘境对象（用 Map 优化查找性能）
const domainMap = computed(() => {
  const map = new Map()
  domains.value.forEach(d => map.set(d.name, d))
  return map
})
const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
// const showDays = (config, type) => {
//   if (type === 'clear') {
//     config.days = []
//   } else if (type === 'showDaysSelector') {
//     config.showDaysSelector = true
//   } else if (type === 'hideDaysSelector') {
//     config.showDaysSelector = false
//   }
//   changShowDaysButton(config);
// }
const changSortConfigs = () => {
  if (orderSortConfigs.value) {
    configs.value.sort((a, b) => b.order - a.order)
  }
}

function changShowDaysButton(config) {
  if (config.days && config.days.length > 0) {
    config.dayName = "已选中:" + config.days.map(dayIndex => weekDays[dayIndex]).join(', ')
  } else if (config.days && config.days.length <= 0) {
    config.dayName = undefined
  }
  if ((!excludeDomainTypes.value.includes(config.selectedType)) && config.autoFight.sundaySelectedValue) {
    // 实时监听 days 与 asDaysMap.get(sundaySelectedValue) 是否相同
    const daysFromMap = asDaysMap.get(config.autoFight.sundaySelectedValue + "");
    if (daysFromMap && Array.isArray(daysFromMap)) {
      config.days.sort((a, b) => a - b)
      daysFromMap.sort((a, b) => a - b)
      const currentDays = Array.isArray(config.days) ? config.days : [];
      const areEqual = JSON.stringify(currentDays) === JSON.stringify(daysFromMap);
      config.showDaysButton = !areEqual; // 相同则设为 false，否则设为 true
    }
  }
}

// 监听每一项的 domainName 变化 → 自动填充 sundaySelectedValue
watchEffect(
    () => configs.value,
    (newConfigs) => {
      newConfigs.forEach(config => {
        const domainName = config.autoFight.domainName
        if (!domainName) {
          config.autoFight.sundaySelectedValue = undefined
          return
        }

        const domain = domainMap.value.get(domainName)
        if (!domain) return
        // 处理 days 数组
        if (Array.isArray(config.days) && config.days.length > 0) {
          config.dayName = config.days.map(dayIndex => weekDays[dayIndex]).join(', ')
        } else {
          config.dayName = ''
        }

        if (domain.hasOrder && domain.list?.length > 0) {
          // 自动选第一个（也可改为 undefined，让用户手动选）
          if (!config.autoFight.sundaySelectedValue) {
            config.autoFight.sundaySelectedValue = domain.list[0]
          }
        } else {
          config.autoFight.sundaySelectedValue = config.autoFight.sundaySelectedValue || undefined
        }

        changShowDaysButton(config);
      })

      if (orderSortConfigs.value) {
        newConfigs.sort((a, b) => b.order - a.order)
      }

    },
    {deep: true}
)

// 初始化时至少有一条（可选）
if (configs.value.length === 0) {
  addConfig()
}

// 获取最终用于保存/提交的数据
const getFinalConfigs = () => {
  return configs.value.map(c => {
    let autoFight = c.autoFight
    if (autoFight.domainName) {
      const info = domainMap.value.get(autoFight.domainName);
      let index = 1
      for (let item of info.list) {
        if (autoFight.sundaySelectedValue === item) {
          // autoFight.sundaySelectedName = autoFight.sundaySelectedValue
          autoFight.sundaySelectedValue = index
        }
        index++
      }
    }
    // c.autoFight.physical.sort((a, b) => a.order - b.order)
    changShowDaysButton(c)
    let json = {
      order: c.order,
      // day: c.day,
      days: c.days,
      dayName: c.dayName,
      // daysName: c.daysName,
      physical: c.physical,
      selectedType: c.selectedType, // 新增字段
      autoFight: autoFight
    };
    json.days.sort((a, b) => a - b)
    return json
  })
}
const getFinalConfigsMapShow = () => {
  const finalConfigs = getFinalConfigs();
  if (uid.value !== "") {
    const map = new Map();
    map.set(uid.value, finalConfigs)
    return [...map]
  }
  return finalConfigs
}
const getFinalConfigsMap = () => {
  const finalConfigs = getFinalConfigs();
  if (uid.value !== "") {
    const map = new Map();
    map.set(uid.value, finalConfigs)
    return map
  }
  return finalConfigs
}
const getFinalConfigsToKey = () => {
  let key = ""
  //"队伍名称|秘境名称/刷取物品名称|刷几轮|限时/周日|执行顺序,..."
  getFinalConfigs().forEach(item => {
    let autoFight = item.autoFight;
    let physical = [...autoFight.physical];
    physical.sort((a, b) => a.order - b.order)
    key += (autoFight.partyName || "")
    key += "|"
    key += (autoFight.domainName)
    key += "|"
    key += (autoFight.DomainRoundNum || "")
    key += "|"
    key += (autoFight.sundaySelectedValue || 1)
    key += "|"
    // key += (item.day || "")
    key += (item.days.join('/') || "") // 将数组转换为字符串
    key += "|"
    key += (physical.filter(p => p.open).map(p => p.name).join('/') || "")
    key += "|"
    key += (item.order || 1) + ","
  })
  if (key.endsWith(",")) {
    key = key.substring(0, key.length - 1);
  }
  return key
}
const specifyDate = async (item) => {
  let pass = false
  const autoFight = item.autoFight;
  // console.log("item:",JSON.stringify(item))
  if (!item.selectedType) {
    ElMessage({
      type: 'error',
      message: `请选择类型！`
    })
  } else if (!autoFight.domainName) {
    ElMessage({
      type: 'error',
      message: `请选择秘境！`
    })
  } else if (!autoFight.sundaySelectedValue) {
    ElMessage({
      type: 'error',
      message: `请选择材料！`
    })
  } else {
    pass = true
  }
  if (pass) {
    //1--days 0,1,4
    //2--days 0,2,5
    //3--days 0,3,6
    const days = asDaysMap.get(autoFight.sundaySelectedValue + "");
    if (!days || !Array.isArray(days)) {
      ElMessage({type: 'error', message: '请选择正确的材料！'});
      return;
    }
    // 类型检查和默认值处理
    const currentDays = Array.isArray(item.days) ? item.days : [];
    const newDays = Array.isArray(days) ? days : [];

    // 比较数组内容是否相同
    const areEqual = JSON.stringify(currentDays) === JSON.stringify(newDays);

    if (!areEqual) {
      // 更新 days 字段
      item.days = [...newDays]; // 使用解构避免引用污染
    }
    // item.showDaysButton = false
    changShowDaysButton(item);
  }
}
const updatePhysicalOrder = (config) => {
  config.autoFight.physical.forEach((item, index) => {
    item.order = index;
  });
  // 至少保留一个启用
  const enabledCount = config.autoFight.physical
      .filter(item => item.open).length

  if (enabledCount === 0) {
    ElMessage({
      type: 'error',
      message: '至少保留一个启用！'
    })
    const fallback = config.autoFight.physical.find(
        item => item.name === '原粹树脂'
    )
    if (fallback) fallback.open = true
  }
};
const copyToClipboard = (text) => {
  CopyToClipboard(text)
};

const handleDaysConfirm = (config) => {
  changShowDaysButton(config)
  config.showDaysDialog = false
}

const clearDays = (config) => {
  config.days = []
  changShowDaysButton(config)
  // 可选择是否关闭弹窗：config.showDaysDialog = false
}
const handleCurrentConfig = (config, type) => {
  if (type === "show-day") {
    config.showDaysDialog = true
  } else if (type === "hide-day") {
    config.showDaysDialog = false
  } else if (type === "show-physical") {
    config.showPhysicalDialog = true
  } else if (type === "hide-physical") {
    config.showPhysicalDialog = false
  }
  updateCurrentConfig(config)
}
const updateCurrentConfig = (config) => {
  currentConfig.value = config
}
</script>

<template>
  <div class="home">
    <div class="container">
      <div class="fixed-container">
        <h2 class="title">自动秘境计划配置列表</h2>
        <div class="config-header">
          <input type="text" v-model="uid" placeholder="设置 UID" class="uid-input"/>
          <!-- 添加配置按钮 -->
          <button @click="addConfig" class="btn btn-add">➕ 添加一条配置</button>
          <div class="sort-control-card">
            <span class="sort-label">执行排序</span>
            <el-switch
                v-model="orderSortConfigs"
                @change="changSortConfigs"
            />
          </div>
          <button @click="submitConfigToBackend" class="btn btn-submit">同步到云端</button>
          <button @click="findDomains" class="btn btn-submit">加载云端配置</button>
          <button @click="removeConfigToBackend" class="btn danger">🗑️ 移除云端配置</button>
          <button @click="removeConfigAll" class="btn danger">🗑️ 清除全部</button>

        </div>
      </div>

      <div class="external-pop-up-frame">
        <!-- 弹窗 -->
        <el-dialog
            v-model="currentConfig.showDaysDialog"
            title="选择执行日期"
            width="480px"
            :close-on-click-modal="false"
            append-to-body
        >

          <div class="dialog-content">
            <div class="checkbox-group">
              <label v-for="(dayName, idx) in weekDays" :key="idx" class="checkbox-label">
                <el-checkbox :label="idx" v-model="currentConfig.days">
                  {{ dayName }}
                </el-checkbox>
              </label>
            </div>

            <div class="dialog-actions">
              <el-button @click="currentConfig.showDaysDialog = false">取消</el-button>
              <el-button type="primary" @click="handleDaysConfirm(currentConfig)">确定</el-button>
              <el-button type="danger" plain size="small" @click="clearDays(currentConfig)">清空</el-button>
            </div>
          </div>
        </el-dialog>
        <el-dialog
            v-model="currentConfig.showPhysicalDialog"
            title="调整树脂使用顺序与启用状态"
            width="520px"
            direction="rtl"
            :close-on-click-modal="false"
        >
          <div class="dialog-content">
            <div class="selector-title">拖拽调整顺序</div>
            <draggable
                v-model="currentConfig.autoFight.physical"
                item-key="name"
                handle=".draggable-item"
                @end="updatePhysicalOrder(currentConfig)"
            >
              <template #item="{ element }">
                <div class="draggable-item">
                  <span class="drag-handle">☰</span>
                  <span class="physical-name">{{ element.name }}</span>
                  <el-switch
                      v-model="element.open"
                      @change="updatePhysicalOrder(currentConfig)"
                  />
                </div>
              </template>
            </draggable>

            <div class="dialog-actions" style="margin-top: 24px; text-align: right;">
              <el-button @click="currentConfig.showPhysicalDialog = false">关闭</el-button>
            </div>
          </div>
        </el-dialog>
        <!-- 主内容区保持原样，只在最外层加一个抽屉 -->
        <el-drawer
            v-model="showResultDrawer"
            direction="rtl"
            size="45%"
            :with-header="true"
            :close-on-press-escape="true"
            :modal="true"
            custom-class="result-drawer"
        >
          <template #title>
            <span style="font-weight: bold; color: #409eff;">配置结果预览</span>
          </template>

          <div class="drawer-content">
            <!-- Json 配置卡片 -->
            <div class="result-card">
              <div class="card-header">
                <label class="result-key">Json配置</label>
                <el-tooltip content="复制到剪贴板" placement="top">
                  <el-button
                      type="primary"
                      size="small"
                      icon="DocumentCopy"
                      @click="copyToClipboard(getFinalConfigsMapShow())"
                  >
                    复制
                  </el-button>
                </el-tooltip>
              </div>
              <pre class="result code-block">{{ getFinalConfigsMapShow() || '暂无返回数据' }}</pre>
            </div>

            <!-- 语法 key 卡片 -->
            <div class="result-card" style="margin-top: 24px;">
              <div class="card-header">
                <label class="result-key">语法key</label>
                <el-tooltip content="复制到剪贴板" placement="top">
                  <el-button
                      type="success"
                      size="small"
                      icon="DocumentCopy"
                      @click="copyToClipboard(getFinalConfigsToKey())"
                  >
                    复制
                  </el-button>
                </el-tooltip>
              </div>
              <pre class="result code-block">{{ getFinalConfigsToKey() || '暂无返回数据' }}</pre>
            </div>
          </div>

          <!-- 可选：底部操作 -->
          <template #footer>
            <div style="text-align: right;">
              <el-button @click="showResultDrawer = false">关闭</el-button>
            </div>
          </template>
        </el-drawer>
      </div>

      <div class="content-area">
        <div class="config-list">
          <div v-for="(config,index) in configs" :key="config.order" class="config-item">
            <h3>#{{ index }} 配置</h3>
            <hr/>

            <div class="form-group">
              <label>执行顺序：</label>
              <input class="limited-input" v-model.number="config.order" type="number" min="1" max="99999999"
                     placeholder="建议 1~10"/>
              <span style="color: red;">数值高的优先执行</span>
            </div>

            <div class="form-group">
              <label>执行日：</label>
              <div
                  class="days-display"
                  @click="handleCurrentConfig(config,'show-day')"
                  :class="{ 'has-selection': config.days?.length > 0 }"
              >
              <span v-if="config.days?.length === 0">
                每天执行（点击指定执行日期）
              </span>
                <span v-else>
                {{ config.dayName || '已选择 ' + config.days.length + ' 天' }}
              </span>
              </div>
            </div>
            <div class="form-group" v-if="config.selectedType&&!excludeDomainTypes.includes(config.selectedType)">
              <label>材料忽略限时开放：</label>
              <el-button
                  size="small"
                  :disabled="!config.showDaysButton"
                  @click="specifyDate(config)"
              >
                {{ config.showDaysButton ? '启用' : '已启用' }}
              </el-button>
              <span style="color: red;">默认包含周日</span>
            </div>
            <!-- 秘境选择 -->
            <!-- 新增 type 选择器 -->
            <div class="form-group">
              <label>秘境类型：</label>
              <select v-model="config.selectedType">
                <option
                    v-for="type in domainTypes"
                    :key="type.value"
                    :value="type.value"
                >
                  {{ type.label }}
                </option>
              </select>
            </div>
            <!-- 秘境选择（根据 selectedType 过滤） -->
            <div class="form-group">
              <label>秘境：</label>
              <select v-model="config.autoFight.domainName">
                <option value="">请选择秘境</option>
                <option
                    v-for="d in filteredDomainsType(config.selectedType)"
                    :key="d.name"
                    :value="d.name"
                >
                  {{ d.name }}
                </option>
              </select>
            </div>
            <!-- 物品名称选择（根据 domainName 过滤） -->
            <div v-if="domainMap.get(config.autoFight.domainName)?.hasOrder" class="form-group">
              <label>周日/限时材料：</label>
              <select
                  v-model="config.autoFight.sundaySelectedValue">
                <option
                    v-for="(item,index) in domainMap.get(config.autoFight.domainName)?.list || []"
                    :key="item"
                    :value="index + 1"
                >
                  {{ item }}
                </option>
              </select>
            </div>
            <div
                v-if="(!domainMap.get(config.autoFight.domainName)?.hasOrder)&&(domainMap.get(config.autoFight.domainName)?.list?.length>0)"
                class="form-group">
              <label>秘境圣遗物：</label>
              <ul>
                <li v-for="item in domainMap.get(config.autoFight.domainName)?.list" :key="item">
                  {{ item }}
                </li>
              </ul>
            </div>


            <div class="form-group">
              <label>队伍名称（可选）：</label>
              <input class="limited-input" v-model="config.autoFight.partyName" placeholder="队伍1 / 主C+副C+辅助"/>
            </div>
            <div class="form-group">
              <label>副本轮数：</label>
              <input class="limited-input" v-model.number="config.autoFight.DomainRoundNum" type="number" min="1"
                     max="99"
                     placeholder="建议 1~10"/>
            </div>

            <!--          <hr/>-->


            <div class="form-group">
              <label>树脂使用顺序：</label>
              <!-- 原 physical-display 改成 -->
              <div
                  class="physical-display"
                  @click="handleCurrentConfig(config,'show-physical')"
              >
              <span>
                {{
                  config.autoFight.physical
                      .filter(p => p.open)
                      .map(p => p.name)
                      .join(' → ') || '未选择'
                }}
              </span>
              </div>
            </div>

            <!-- 删除按钮 -->

            <button @click="removeConfig(index)" class="btn danger">🗑️ 删除</button>

          </div>
        </div>
        <!-- 右侧固定触发按钮（悬浮在页面右中部） -->
        <div class="fixed-trigger" @click="showResultDrawer = true" title="查看/复制配置结果">
          <i class="el-icon-document"></i>
          <span>查看/复制配置结果</span>
        </div>
        <!--        <div class="result-all">
                  <label class="result-key">Json配置:</label>
                  <pre class="result">{{ getFinalConfigsMapShow() || '暂无返回数据' }}</pre>
                  <button @click="copyToClipboard(getFinalConfigsMapShow())" class="copy-btn">📋 复制</button>
                </div>
                <div class="result-all">
                  <label class="result-key">语法key:</label>
                  <pre class="result">{{ getFinalConfigsToKey() || '暂无返回数据' }}</pre>
                  <button @click="copyToClipboard(getFinalConfigsToKey())" class="copy-btn">📋 复制</button>
                </div>-->
      </div>
    </div>
    <!-- 在 template 最后添加 -->
    <div class="fixed-footer">
      <button @click="goToHome" class="btn secondary">🏠 返回主页</button>
    </div>

  </div>
</template>

<style>
/* 页面全屏背景 */
.home {
  /*  display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    width: 100vw;*/
  /*  background: linear-gradient(135deg, #a1c4fd, #c2e9fb);*/
  min-height: 100vh;
  /*  //display: flex;
    //align-items: center;
    //justify-content: center;*/
  background: url("@assets/MHY_XTLL.png");
  /* 关键：固定背景，不随滚动重复或变形 */
  background-attachment: fixed; /* ← 核心属性 */
  background-size: cover; /* 覆盖整个容器 */
  background-position: center;
  /*  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;*/
}

/* 整体容器 */
.container {
  width: 80%;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* 固定容器样式 */
.fixed-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 15%;
  /* background: rgba(255, 255, 255, 0.9); !* 半透明白色背景 *!*/
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  z-index: 1000; /* 确保在最上层 */
  padding: 10px 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); /* 添加阴影 */
}

/* 内容区域补偿高度 */
.content-area {
  margin-top: 15%; /* 根据 .fixed-container 的实际高度调整 */
  width: 100%;
}

/* 标题样式（保持原有样式） */
.title {
  font-size: 36px;
  font-weight: 800;
  margin-bottom: 15px;
  color: transparent;
  background: linear-gradient(90deg, #d612cc, #9e367d);
  -webkit-background-clip: text;
  background-clip: text;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.title:hover {
  transform: scale(1.05);
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}


/* 标题样式 */
h2 {
  text-align: center;
  color: #333;
  font-size: 1.8rem;
  margin-bottom: 20px;
}

/* UID 输入框 */
.uid-input {
  max-width: 40%;
  padding: 10px;
  margin-bottom: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.uid-input:focus {
  max-width: 40%;
  border-color: #409eff;
  outline: none;
  box-shadow: 0 0 5px rgba(64, 158, 255, 0.5);
}

/* 添加配置按钮 */
.add-config-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s ease;
}

.add-config-btn:hover {
  background-color: #66b1ff;
}

.config-header {
  display: flex;
  flex-wrap: wrap; /* 允许子元素换行 */
  gap: 20px; /* 设置子元素之间的间距 */
  justify-content: flex-start; /* 子元素左对齐 */
  padding: 10px;
}

.config-list {
  display: flex;
  flex-wrap: wrap; /* 允许子元素换行 */
  gap: 20px; /* 设置子元素之间的间距 */
  justify-content: flex-start; /* 子元素左对齐 */
}

/* 配置项卡片 */
.config-item {
  max-width: 40%;
  background: linear-gradient(135deg, #b6b2b6, #91dcd6);
  border: 1px solid #b9bcc6;
  border-radius: 12px;
  padding: 10px;
  margin-bottom: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  /* 禁止超出框限制*/
  overflow: visible; /* 禁止内容超出容器 */
}

.config-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

/* 配置标题 */
.config-item h3 {
  margin-top: 0;
  color: #333;
  font-size: 1rem;
}

/* 删除按钮 */
.remove-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 6px;
  cursor: pointer;
  float: right;
  transition: background-color 0.3s ease;
}

.remove-btn:hover {
  background-color: #ff4d4f;
}

/* 表单项通用样式 */
.form-group {
  margin-bottom: 8px;
}

.form-group label {
  font-size: 0.9rem; /* 从默认大小减小 */
  /*  display: block;
    margin-bottom: 5px;
    font-weight: bold;*/
  color: #606266;
}

.form-group select {
  align-items: center;
  /* width: 80%;*/
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.days-display {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  min-height: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.2s;
}

.days-display:hover {
  border-color: #409eff;
}

.days-display.has-selection {
  color: #409eff;
  font-weight: 500;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 32px;
  margin-bottom: 12px;
}

.checkbox-label {
  min-width: 80px;
}


.form-group input {
  align-items: center;
  width: 40%;
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-group select:focus,
.form-group input:focus {
  border-color: #409eff;
  outline: none;
  box-shadow: 0 0 5px rgba(64, 158, 255, 0.5);
}

/* 结果展示区域 */
.result-all {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
}

.result-key {
  background-color: #ffffff; /* 白色背景 */
  color: #000000; /* 黑色文字 */
  padding: 10px 15px; /* 内边距 */
  border-radius: 8px; /* 圆角 */
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1); /* 添加阴影，模拟卡片效果 */
  display: inline-block; /* 确保样式生效 */
  font-weight: bold; /* 加粗文字 */
  transition: all 0.3s ease; /* 平滑过渡效果 */
}

.result-key:hover {
  transform: translateY(-2px); /* 悬停时轻微上移 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); /* 悬停时增强阴影 */
}

.result {
  flex: 1;
  background: linear-gradient(135deg, #ffda47, #ffffff);
  padding: 15px;
  border-radius: 8px;
  white-space: pre-wrap;
  font-family: monospace;
  font-size: 0.9rem;
  color: #ff09c5;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.copy-btn {
  background-color: #67c23a;
  color: white;
  border: none;
  padding: 10px 15px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.copy-btn:hover {
  background-color: #85ce61;
}
.sort-control-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;

  background-color: #85ce61; /* 白色背景 */
  color: #000000; /* 黑色文字 */
  padding: 10px 20px; /* 内边距 */
  border-radius: 8px; /* 圆角 */
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1); /* 添加阴影，模拟卡片效果 */
  border: none; /* 去除边框 */
  font-weight: bold; /* 加粗文字 */
  transition: all 0.3s ease; /* 平滑过渡效果 */
}

.sort-control-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35),
  inset 0 1px 0 rgba(255, 255, 255, 0.08);
  border-color: rgba(96, 165, 250, 0.4);
}

.sort-label {
  color: #000000;
  font-size: 0.95rem;
  font-weight: 500;
  letter-spacing: 0.3px;
}

/* 讓 switch 看起來更精緻（可選） */
:deep(.el-switch__core) {
  border-color: rgba(96, 165, 250, 0.5) !important;
}

:deep(.el-switch.is-checked .el-switch__core) {
  border-color: #60a5fa !important;
  background-color: #3b82f6 !important;
}
.btn.btn-add {
  background-color: #85ce61; /* 白色背景 */
  color: #000000; /* 黑色文字 */
  padding: 10px 20px; /* 内边距 */
  border-radius: 8px; /* 圆角 */
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1); /* 添加阴影，模拟卡片效果 */
  border: none; /* 去除边框 */
  font-weight: bold; /* 加粗文字 */
  transition: all 0.3s ease; /* 平滑过渡效果 */
}

.btn.btn-submit:hover {
  transform: translateY(-2px); /* 悬停时轻微上移 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); /* 悬停时增强阴影 */
}

.btn.btn-submit {
  background-color: #18c3e8; /* 白色背景 */
  color: #000000; /* 黑色文字 */
  padding: 10px 20px; /* 内边距 */
  border-radius: 8px; /* 圆角 */
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1); /* 添加阴影，模拟卡片效果 */
  border: none; /* 去除边框 */
  font-weight: bold; /* 加粗文字 */
  transition: all 0.3s ease; /* 平滑过渡效果 */
}

.btn.btn-add:hover {
  transform: translateY(-2px); /* 悬停时轻微上移 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); /* 悬停时增强阴影 */
}

.btn.danger {
  background-color: #e19025; /* 白色背景 */
  color: #000000; /* 黑色文字 */
  padding: 10px 20px; /* 内边距 */
  border-radius: 8px; /* 圆角 */
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1); /* 添加阴影，模拟卡片效果 */
  border: none; /* 去除边框 */
  font-weight: bold; /* 加粗文字 */
  transition: all 0.3s ease; /* 平滑过渡效果 */
}

.btn.danger:hover {
  transform: translateY(-2px); /* 悬停时轻微上移 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); /* 悬停时增强阴影 */
}

.btn.danger:hover {
  background: #c0392b;
  transform: scale(1.05);
}

.limited-input {
  /* width: 200px; !* 限制输入框宽度 *!*/
  /* 禁止超出框限制*/
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.el-button.is-disabled {
  background-color: #e0e0e0;
  color: #999;
  cursor: not-allowed;
}


.drag-handle {
  cursor: move;
  margin-right: 12px;
  font-size: 1.2rem;
  color: #999;
}

.draggable-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 6px;
  margin-bottom: 8px;
}

.actions {
  text-align: right;
  margin-top: 12px;
}

.physical-display {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #f5f7fa;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.2s;
  min-height: 36px;
}

.physical-display:hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.draggable-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 8px;
}

.drag-handle {
  cursor: move;
  font-size: 1.3rem;
  color: #909399;
}

.physical-name {
  font-weight: 500;
}

/* 右侧固定触发按钮 */
.fixed-trigger {
  position: fixed;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 999;
  width: 4%;
  height: 40%;
  background: rgba(64, 158, 255, 0.9);
  color: white;
  border-radius: 12px 0 0 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  box-shadow: -2px 0 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.fixed-trigger:hover {
  right: 18px;
  background: rgba(64, 158, 255, 1);
  box-shadow: -4px 0 16px rgba(0, 0, 0, 0.2);
}

.fixed-trigger i {
  font-size: 1.8rem;
}

.fixed-trigger span {
  font-size: 0.9rem;
  writing-mode: vertical-rl;
  letter-spacing: 2px;
}

/* 抽屉自定义样式 */
.result-drawer {
  --el-drawer-bg-color: rgba(206, 33, 33, 0.96);
  --el-drawer-border-color: #1b3e8f;
  background: #fadbd8;
  backdrop-filter: blur(6px);
}

.drawer-content {
  padding: 0 16px 24px;
  height: 100%;
  overflow-y: auto;
  backdrop-filter: blur(6px);
}

.result-card {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #29cbc5, #cf12e3); /* 添加渐变背景 */
  border-bottom: 1px solid #e9ecef;
}

.result-key {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #303133;
}

.code-block {
  margin: 0;
  padding: 16px;
  background: linear-gradient(135deg, #ddb568, #ffffff); /* 添加渐变背景 */
  color: rgb(230, 0, 103); /* 修改为你想要的颜色 */
  font-family: 'Consolas', 'Courier New', monospace;
  font-size: 0.94rem;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 45vh;
  overflow-y: auto;
}

.external-pop-up-frame {
  /* 讓彈窗有「浮在背景上」的氛圍 */
  position: relative;
  z-index: 2000; /* 確保蓋過其他內容 */
}

/* 對所有從這裡彈出的 el-dialog / el-drawer 生效 */
.external-pop-up-frame .el-dialog,
.external-pop-up-frame .el-drawer {
  /*  --el-dialog-bg-color         : rgba(206, 210, 225, 0.88) !important;*/
  /*background                   : linear-gradient(135deg, #5b818c, #38e0c2);*/
  /*  --el-overlay-bg-color        : rgba(224, 208, 208, 0.65) !important;*/
  backdrop-filter: blur(12px) saturate(1.6);
  border: 1px solid rgba(100, 160, 255, 0.25);
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(80, 76, 76, 0.5);
  overflow: hidden;
}

/* 標題區域加強 */
.external-pop-up-frame .el-dialog__header,
.external-pop-up-frame .el-drawer__header {
  color: #e4e8ea;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  padding: 16px 24px;
}

/* 內容區域 */
.external-pop-up-frame .el-dialog__body,
.external-pop-up-frame .el-drawer__body {
  background: transparent;
  /*  color                        : #e2e8f0;*/
  padding: 20px 24px;
}

/* 按鈕區域（footer） */
.external-pop-up-frame .el-dialog__footer {
  background: rgba(0, 0, 0, 0.2);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  padding: 16px 24px;
}


</style>