<script setup>
import {ref, computed, watch, watchEffect, onMounted, nextTick} from 'vue'
import {ElMessage, ElMessageBox} from "element-plus";
import {
  getBaseCountryJsonAll,
  getBaseJsonAll,
  getUidJson,
  postUidPlan,
  removeUidList,
  getAllUid
} from "@api/auto_plan/autoPlan";
import {CopyToClipboard} from "@utils/local.js";
import {
  countryListDefault,
  domainsDefault,
  domainTypesDefault,
  excludeDomainTypesDefault, leyLineOutcropTypeNamesDefault, leyLineOutcropTypesDefault,
  runTypesDefault,
  selectedAsDaysMap
} from "@utils/defaultdata.js";

import draggable from 'vuedraggable'
import {debounce} from 'lodash-es';
import {toHomePage} from "@api/web/web.js";

const cloud = ref({
  UidList: [],
  LoadingUidList: false,
  lastRequestTime: 0,
  // 設定冷卻時間（單位：毫秒），例如每 1 秒最多請求 1 次
  cooldownMs: 1000,
})
const querySearchAsync = (queryString, cb) => {
  if (queryString?.trim() === "" || !queryString) {
    cb(cloud.value.UidList)
  } else {
    cb([])
  }
}
const findAllUid = async () => {
  const res = await getAllUid()
  cloud.value.UidList = res
  cloud.value.lastRequestTime = Date.now()
}
const initAllUid = async () => {
  try {
    await findAllUid()
  } catch (e) {
  }
}
const loadCloudUidListIfNeeded = async () => {
  const now = Date.now()

  // 正在冷卻中 → 直接返回
  if (now - cloud.value.lastRequestTime < cloud.value.cooldownMs) {
    // 可選：console.log(`請求太頻繁，距離上次還剩 ${COOLDOWN_MS - (now - lastRequestTime.value)}ms`)
    return
  }
  //  正在加载中 → 直接返回
  if (cloud.value.LoadingUidList) return

  cloud.value.LoadingUidList = true

  try {
    await findAllUid()
    // console.log("获取云端UID列表成功:", cloud.value)
  } catch (e) {
    console.error("获取云端UID列表失败", e)
    // 这里**不弹 ElMessage**，保持安静
  } finally {
    cloud.value.LoadingUidList = false
  }
}
// 计算属性：是否有云端数据
const hasCloudUidList = computed(() => {
  return cloud.value.UidList.length > 0
})
const handleUidSelect = (item) => {
  uid.value = item
  ElMessage.success(`已选择云端 UID：${item}`)
  findDomains()
}
// 配置列表 → 核心数据结构改为 array
const configs = ref([])

const isLoading = ref(false);
// 秘境数据（保持不变，建议单独抽到一个文件）
const defaultDomains = domainsDefault
const domains = ref([])
const domainTypes = ref([])
const runTypes = ref([])
const leyLineOutcropTypes = ref([])
const countryList = ref(null)
const excludeDomainTypes = ref(new Array())
const initDomainTypes = async () => {
  const types = [
    // {value: '', label: '请选择类型'}
  ]
  const list = domainTypesDefault();
  list.forEach(item => {
    types.push({value: item, label: item})
  })
  domainTypes.value = types

  const excludes = excludeDomainTypesDefault()
  excludeDomainTypes.value.push(...excludes)
}
const initRunTypes = async () => {
  runTypes.value = runTypesDefault();
}
const leyLineOutcropTypeNames = ref([])
const initLeyLineOutcropTypes = async () => {
  leyLineOutcropTypes.value = leyLineOutcropTypesDefault();
  leyLineOutcropTypeNames.value = leyLineOutcropTypes.value.map(item => item.name)
}
const initCountryList = async () => {
  try {
    countryList.value = await getBaseCountryJsonAll()
  } catch (e) {
    ElMessage.warning('获取国家列表失败，使用默认数据')
  }
  if ((!countryList.value) || countryList.value?.length <= 0)
    countryList.value = await countryListDefault()
}
const currentConfig = ref(null)
const materialsOrderMaps = ref(new Map())
const materialsDomainMaps = ref(new Map())
const materialsALL = ref(new Array())
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

  if (domains.value && domains.value.length > 0) {
    domains.value.forEach(item => {
      if (item.hasOrder) {
        // console.log('item', item)
        let index = 1
        for (let one of item.list) {
          materialsOrderMaps.value.set(one, index)
          materialsDomainMaps.value.set(one, item.name)
          materialsALL.value.push({name: one, type: item.type, index: index, domain: item.name})
          index++
        }
      }
    })
  }
};
const removeConfigToBackend = async () => {
  if (!uid.value) {
    ElMessage.warning("请先设置 UID");
    return;
  }
  await ElMessageBox.confirm(`确定移除UID:${uid.value}的云端数据吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  let ids = []
  ids.push(uid.value)
  const uidStr = ids.join(',');
  await removeUidList(uidStr)
  cloud.value.UidList = cloud.value.UidList.filter(item => item !== uid.value)
  return
}
const submitConfigToBackend = async () => {
  if (!uid.value) {
    ElMessage.warning("请先设置 UID");
    return;
  }
  await ElMessageBox.confirm(`确定提交UID:${uid.value}的数据至云端吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  const planList = getFinalConfigs()
  // await postUidJson(uid.value, JSON.stringify(json))
  await postUidPlan(uid.value, planList)
};
const initConfigsId = () => {
  configs.value.forEach(
      config => {
        if (!config.id) {
          //随机生成唯一id，防止重复
          config.id = Date.now() + Math.random().toString(36).substr(2, 9);
        }
      }
  )
}
const findDomains = async () => {
  if (!uid.value) {
    ElMessage.warning("请先设置 UID");
    return;
  }
  await ElMessageBox.confirm(`确定加载UID:${uid.value}的云端数据吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  try {
    const response = await getUidJson(uid.value)
    configs.value = response;
    configs.value.forEach(config => {
      let autoStygianOnslaught = config.autoStygianOnslaught;
      if (autoStygianOnslaught?.bossNum === null) {
        autoStygianOnslaught.bossNum = undefined
      }
    })
  } catch (error) {
    console.error('请求失败:', error);
    ElMessage.error(error.message);
  } finally {
    initConfigsId()
  }
};

const asDaysMap = selectedAsDaysMap()
onMounted(() => {
  fetchDomains();
  initDomainTypes()
  initRunTypes()
  initLeyLineOutcropTypes()
  initCountryList()
  initAllUid()

})
// 在 script 中添加跳转逻辑
const goToHome = async () => {
  // router.push('/'); // 假设主页路径是 '/'
  await toHomePage()
};

const showResultDrawer = ref(false)
const orderSortConfigs = ref(false)
const uid = ref("")
// 新增一条空白配置
const addConfig = (config = undefined) => {
  let newConfig;
  if (!config) {
    newConfig = {
      order: 1,
      // day: undefined,
      days: [],
      runType: runTypesDefault()[0],//先写死 预留地脉类型

      dayName: undefined,
      showDaysSelector: false,   // ← 新增
      showPhysicalSelector: false,   // ← 新增
      showDaysButton: true,   // ← 新增
      // daysName: [],
      selectedType: undefined, // 新增字段
      autoFight: {
        physical: [
          {order: 0, name: "浓缩树脂", open: true},
          {order: 1, name: "原粹树脂", open: true},
          {order: 2, name: "须臾树脂", open: false},
          {order: 3, name: "脆弱树脂", open: false}
        ],
        domainName: undefined,
        partyName: undefined,
        sundaySelectedValue: undefined,
        sundaySelectedDomain: undefined,
        domainRoundNum: 1
      },
      // 新增：地脉专用字段（默认值）
      autoLeyLineOutcrop: {
        count: 1,                        // 刷几次（0=自动/无限）
        country: countryListDefault()[0],                     // 国家地区
        leyLineOutcropType: leyLineOutcropTypeNamesDefault()[0], // 需映射为经验/摩拉
        useAdventurerHandbook: false,    // 是否使用冒险之证
        friendshipTeam: "",              // 好感队伍ID
        team: "",                        // 主队伍ID
        timeout: 120,                      // 超时时间（秒）
        isGoToSynthesizer: false,        // 是否前往合成台
        useFragileResin: false,          // 使用脆弱树脂
        useTransientResin: false,        // 使用须臾树脂（须臾=Transient）
        isNotification: false            // 是否通知
      },
      // 新添加幽境
      autoStygianOnslaught: {
        physical: [
          {order: 0, name: "浓缩树脂", open: true, count: 1},
          {order: 1, name: "原粹树脂", open: true, count: 1},
          {order: 2, name: "须臾树脂", open: false, count: 1},
          {order: 3, name: "脆弱树脂", open: false, count: 1}
        ],
        specifyResinUse: false,// 是否指定使用
        bossNum: undefined,
        fightTeamName: "",
      }
    };
  } else {
    // 深拷贝现有配置
    newConfig = JSON.parse(JSON.stringify(config));
    // 为复制的配置生成新的唯一ID
    newConfig.id = Date.now() + Math.random().toString(36).substr(2, 9);
  }
  configs.value.push(newConfig)
  initConfigsId()
  // console.log("addConfig", JSON.stringify(newConfig))
  changSortConfigs()
  // 强制更新状态
  nextTick(() => {
    updateSelectAllState()
  })
}
const removeConfigAll = async () => {
  await ElMessageBox.confirm(`确定清除全部本地数据吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  configs.value = []
  // 强制更新状态
  await nextTick(() => {
    updateSelectAllState()
  })
}
// 删除某一条
const removeConfig = (id) => {
  if (id) {
    // 单个删除
    const find = configs.value.find(c => c.id === id);
    // console.log("find", JSON.stringify(find))
    configs.value = configs.value.filter(c => c !== find);
    batchJson.value.selectedConfigs.delete(id)

    // 强制更新状态
    nextTick(() => {
      updateSelectAllState()
    })
  }
}
const removeConfigMultiple = () => {
  let removeIds = [...batchJson.value.selectedConfigs]
  for (let id of removeIds) {
    removeConfig(id)
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
const changSortConfigs = () => {
  if (orderSortConfigs.value) {
    configs.value.sort((a, b) => b.order - a.order)
  }
}

// 在 script setup 部分添加方法
function getFilteredMaterials(config) {
  if (!config || !config.selectedType) {
    return materialsALL.value || [];
  }
  return materialsALL.value.filter(e => e.type === config.selectedType);
}

function handleSundaySelection(config) {
  const selectedItem = config.autoFight.sundaySelectedDomain;
  if (selectedItem) {
    config.autoFight.sundaySelectedName = selectedItem.name;
    config.autoFight.domainName = selectedItem.domain;
    config.autoFight.sundaySelectedValue = selectedItem.index;
    config.autoFight.sundaySelectedDomain = undefined
  } else {
    config.autoFight.sundaySelectedName = undefined
    config.autoFight.domainName = undefined
    config.autoFight.sundaySelectedValue = undefined
  }
}

function changShowDaysButton(config) {
  if (config.days && config.days.length > 0) {
    config.dayName = "已选中:" + config.days.map(dayIndex => weekDays[dayIndex]).join(', ')
  } else if (config.days && config.days.length <= 0) {
    config.dayName = undefined
  }
  if (runTypesDefault[0] === config.runType && (!excludeDomainTypes.value.includes(config.selectedType)) && config.autoFight.sundaySelectedValue) {
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

const debouncedSort = debounce(() => {
  changSortConfigs();
}, 300); // 延迟 300ms 执行
// 监听每一项的 domainName 变化 → 自动填充 sundaySelectedValue
watchEffect(
    () => configs.value,
    (newConfigs) => {
      newConfigs.forEach(config => {
        if (runTypesDefault[0] === config.runType ) {
          let domainName = config.autoFight?.domainName
          if (!domainName) {
            return
          }
          const domain = domainMap.value.get(domainName)
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
        }
        handleSundaySelection(config)
        changShowDaysButton(config);
      })

      debouncedSort()
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
    let autoLeyLineOutcrop = c.autoLeyLineOutcrop
    let autoStygianOnslaught = c.autoStygianOnslaught
    // c.autoFight.physical.sort((a, b) => a.order - b.order)
    changShowDaysButton(c)
    let json = {
      order: c.order,
      // day: c.day,
      days: c.days,
      dayName: c.dayName,
      runType: c.runType,
      // daysName: c.daysName,
      // physical: c.physical,
      selectedType: c.selectedType, // 新增字段
      autoFight: autoFight,
      autoLeyLineOutcrop: autoLeyLineOutcrop,
      autoStygianOnslaught: autoStygianOnslaught,
    };
    if (c.runType === runTypesDefault()[0]) {
      json.autoLeyLineOutcrop = undefined
      json.autoStygianOnslaught = undefined

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
      json.autoFight = autoFight
    } else if (c.runType === runTypesDefault()[1]) {
      json.autoFight = undefined
      json.autoStygianOnslaught = undefined
    } else if (c.runType === runTypesDefault()[2]) {
      json.autoFight = undefined
      json.autoLeyLineOutcrop = undefined
    } else {
      /*      ElMessage.error("请选择类型！")
            throw new Error("请选择类型！")*/
    }
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

  getFinalConfigs().forEach(item => {
    // 类型|执行日期|执行顺序
    key += (item.runType || "")
    key += "|"
    key += (item.days.join('/') || "") // 将数组转换为字符串
    key += "|"
    key += (item.order || 1)
    key += "|"
    if (item.runType === runTypesDefault()[0]) {
      //"|队伍名称|秘境名称/刷取物品名称|刷几轮|限时/周日,..."
      let autoFight = item.autoFight;
      let physical = [...autoFight.physical];
      physical.sort((a, b) => a.order - b.order)

      key += (autoFight.partyName || "")
      key += "|"
      key += (autoFight.domainName)
      key += "|"
      key += (autoFight.domainRoundNum || "")
      key += "|"
      key += (autoFight.sundaySelectedValue || 1)
      key += "|"
      key += (physical.filter(p => p.open).map(p => p.name).join('/') || "")
    } else if (item.runType === runTypesDefault()[1]) {
      //"|队伍名称|国家|刷几轮|花类型|好感队|是否使用脆弱树脂|是否使用须臾树脂|是否前往合成台合成浓缩树脂|是否使用冒险之证|发送详细通知|战斗超时时间,..."
      let autoLeyLineOutcrop = item.autoLeyLineOutcrop;
      //todo:  LeyLineOutcrop
      key += (autoLeyLineOutcrop.team || "")
      key += "|"
      key += (autoLeyLineOutcrop.country || "")
      key += "|"
      key += (autoLeyLineOutcrop.count || "")
      key += "|"
      key += (autoLeyLineOutcrop.leyLineOutcropType || "")
      key += "|"
      key += (autoLeyLineOutcrop.friendshipTeam || "")
      key += "|"
      key += (autoLeyLineOutcrop.useFragileResin || "")
      key += "|"
      key += (autoLeyLineOutcrop.useTransientResin || "")
      key += "|"
      key += (autoLeyLineOutcrop.isGoToSynthesizer || "")
      key += "|"
      key += (autoLeyLineOutcrop.useAdventurerHandbook || "")
      key += "|"
      key += (autoLeyLineOutcrop.isNotification || "")
      key += "|"
      key += (autoLeyLineOutcrop.timeout || "")
    } else if (item.runType === runTypesDefault()[2]) {
      let autoStygianOnslaught = item.autoStygianOnslaught
      let physical = autoStygianOnslaught.physical
      key += (autoStygianOnslaught.bossNum || "")
      key += "|"
      key += (autoStygianOnslaught.friendshipTeam || "")
      key += "|"
      key += (autoStygianOnslaught.specifyResinUse ? "1" : "")
      if (autoStygianOnslaught.specifyResinUse) {
        key += "|"
        key += (physical.filter(p => p.open).map(p => p.name).join('/') || "")
        key += "|"
        key += (physical.filter(p => p.open).map(p => p.count).join('/') || "")
      }
    }
    key += ","
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
  if (config.runType === runTypesDefault()[1]) {
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
  } else if (config.runType === runTypesDefault()[2]) {
    if (config.autoStygianOnslaught.specifyResinUse) {
      config.autoStygianOnslaught.physical.forEach((item, index) => {
        item.order = index;
      })
      // 至少保留一个启用
      const enabledCount = config.autoStygianOnslaught.physical
          .filter(item => item.open).length

      if (enabledCount === 0) {
        ElMessage({
          type: 'error',
          message: '至少保留一个启用！'
        })
        const fallback = config.autoStygianOnslaught.physical.find(
            item => item.name === '原粹树脂'
        )
        if (fallback) fallback.open = true
      }
    }
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
  } else if (type === "show-physical-domain") {
    config.showPhysicalDialogFromDomain = true
  } else if (type === "hide-physical-domain") {
    config.showPhysicalDialogFromDomain = false
  } else if (type === "show-physical-stygianOnslaught") {
    config.showPhysicalDialogFromStygianOnslaught = true
  } else if (type === "hide-physical-stygianOnslaught") {
    config.showPhysicalDialogFromStygianOnslaught = false
  }
  updateCurrentConfig(config)
}
const updateCurrentConfig = (config) => {
  currentConfig.value = config
}
const batchJson = ref({
  selectedConfigs: new Set(),
  batch: {
    show: false,
    autoFight: {
      partyName: "",
    },
    autoLeyLineOutcrop: {
      // count: 1,                        // 刷几次（0=自动/无限）
      // country: countryListDefault()[0],                     // 国家地区
      // leyLineOutcropType: leyLineOutcropTypeNamesDefault()[0], // 需映射为经验/摩拉
      // useAdventurerHandbook: false,    // 是否使用冒险之证
      friendshipTeam: "",              // 好感队伍ID
      team: "",                        // 主队伍ID
      // timeout: 120,                      // 超时时间（秒）
      // isGoToSynthesizer: false,        // 是否前往合成台
      // useFragileResin: false,          // 使用脆弱树脂
      // useTransientResin: false,        // 使用须臾树脂（须臾=Transient）
      // isNotification: false            // 是否通知
    },
    autoStygianOnslaught: {
      bossNum: undefined,
      friendshipTeam: "",
    }
  }
})

// ... existing code ...
// 批量复制选中的配置
const batchCopyConfigs = () => {
  // const selectedIds = Array.from(selectedConfigs.value)
  const ids = [...batchJson.value.selectedConfigs]
  const configsToCopy = configs.value.filter(c => ids.includes(c.id))
  // console.log("configsToCopy:", JSON.stringify(configsToCopy))
  // console.log("ids:", JSON.stringify(ids))
  // console.log("selectedConfigs:", batchJson.value.selectedConfigs)
  configsToCopy.forEach(config => {
    addConfig(config)
  })

  // 复制完成后清空选择
  // selectedConfigs.value.clear()
  batchJson.value.selectedConfigs.clear()
  // isAllSelected.value = false
  // 强制更新状态
  nextTick(() => {
    updateSelectAllState()
  })
  ElMessage.success(`成功复制 ${configsToCopy.length} 个配置`)
}


// 检查是否全选
const isAllSelected = ref(false)

const multipleChoices = ref(false)
// 计算属性优化：基于多选状态的派生数据
const multiSelectEnabled = computed({
  get() {
    return multipleChoices.value
  },
  set(value) {
    changeMultipleChoices()
  }
})
// 添加专门的状态更新方法
const updateSelectAllState = () => {
  const totalConfigs = configs.value.length
  const selectedCount = batchJson.value.selectedConfigs.size
  // 更新全选状态
  isAllSelected.value = totalConfigs > 0 && selectedCount === totalConfigs
}

// 带有加载状态和错误处理的切换函数
const changeMultipleChoices = debounce(async () => {
  try {
    // 可以在这里添加加载状态
    // loading.value = true

    const newValue = !multipleChoices.value
    multipleChoices.value = newValue

    if (!newValue) {
      // 关闭多选时的清理工作
      batchJson.value.selectedConfigs.clear()
      // isAllSelected.value = false
      // 可以添加其他清理逻辑
    }
    // 触发相关的副作用
    // emit('multiple-choice-changed', newValue)

  } catch (error) {
    console.error('切换多选模式失败:', error)
    // 可以添加错误提示
    ElMessage.error('操作失败，请重试')
  } finally {
    // loading.value = false
    // 强制更新状态
    await nextTick(() => {
      updateSelectAllState()
    })
  }
})


const isAllSelectedComputed = computed({
  get() {
    return isAllSelected.value
  },
  set(value) {
    if (value) {
      // 全选
      configs.value.forEach(config => {
        batchJson.value.selectedConfigs.add(config.id)
      })
    } else {
      // 取消全选
      batchJson.value.selectedConfigs.clear()
    }

    // 强制更新状态
    nextTick(() => {
      updateSelectAllState()
    })

  }
})

// 修正计算属性
const isConfigSelected = (configId) => {
  return batchJson.value.selectedConfigs.has(configId)
}

// 处理选中状态变化
const handleConfigSelection = (configId, isSelected) => {
  if (isSelected) {
    batchJson.value.selectedConfigs.add(configId)
  } else {
    batchJson.value.selectedConfigs.delete(configId)
  }
  // 强制更新状态
  nextTick(() => {
    updateSelectAllState()
  })
}

const batchUpdate = () => {
  const batch = batchJson.value.batch;
  const autoLeyLineOutcrop = batch.autoLeyLineOutcrop;
  const autoStygianOnslaught = batch.autoStygianOnslaught;
  const autoFight = batch.autoFight;
  configs.value.forEach(config => {
    if (batchJson.value.selectedConfigs.has(config.id)) {
      if (config.runType === runTypesDefault()[0]) {
        //秘境
        config.autoFight.partyName = autoFight.partyName
      } else if (config.runType === runTypesDefault()[1]) {
        //地脉
        config.autoLeyLineOutcrop.team = autoLeyLineOutcrop.team
        config.autoLeyLineOutcrop.friendshipTeam = autoLeyLineOutcrop.friendshipTeam
      } else if (config.runType === runTypesDefault()[2]) {
        config.autoStygianOnslaught.friendshipTeam = autoStygianOnslaught.friendshipTeam
        config.autoStygianOnslaught.bossNum = autoStygianOnslaught.bossNum
      }
    }
  })
  batchJson.value.batch.show = false
}

</script>

<template>
  <div class="home">
    <div class="container">
      <div class="fixed-container">
        <h2 class="title">自动体力计划配置列表</h2>
        <div class="config-header">
          <!-- template 部分保持基本相同，但增加 v-if 判断 -->
          <div class="sort-control-card">
            <el-autocomplete
                v-model="uid"
                :fetch-suggestions="querySearchAsync"
                placeholder="设置UID/点击云端配置"
                :trigger-on-focus="hasCloudUidList"
                :clearable="true"
                :show-loading="cloud.LoadingUidList"
                @select="handleUidSelect"
                @focus="loadCloudUidListIfNeeded"
                style="width: 180px;"
            >
              <template #default="{ item }">
                <div class="uid-item">
                  <span class="uid-text">{{ item }}</span>
                  <!--                <span v-if="item.lastSync" class="uid-time">最后同步: {{ item.lastSync }}</span>-->
                </div>
              </template>

            </el-autocomplete>
          </div>

          <!--          <div class="sort-control-card">
                      <input type="text" v-model="uid" placeholder="设置 UID" class="uid-input"/>
                    </div>-->
          <!-- 添加配置按钮 -->
          <button @click="addConfig()" class="btn btn-add">➕ 添加一条配置</button>
          <div class="sort-control-card">
            <span class="sort-label">执行排序</span>
            <el-switch
                v-model="orderSortConfigs"
                @change="debouncedSort"
            />
          </div>
          <button @click="submitConfigToBackend" class="btn btn-submit">同步到云端</button>
          <button @click="findDomains" class="btn btn-submit">加载云端配置</button>
          <button @click="removeConfigToBackend" class="btn danger">🗑️ 移除云端配置</button>
          <button @click="removeConfigAll" class="btn danger">🗑️ 清除全部</button>

        </div>

        <!-- 在配置列表上方添加批量操作区域 -->
        <div class="config-header" v-if="configs.length > 0">
          <!--          <button class="btn btn-submit" v-if="configs.length>0"  @click="toggleSelectAll">全选</button>-->
          <div class="sort-control-card">
            <el-switch
                v-if="configs.length > 0"
                v-model="multiSelectEnabled"
                active-text="多选"
                inactive-text="取消"
                style="margin-left: 12px;"
            />
          </div>
          <div class="sort-control-card" v-if="multiSelectEnabled">
            <el-switch
                v-if="configs.length > 0"
                v-model="isAllSelectedComputed"
                active-text="全选"
                inactive-text="取消"
                style="margin-left: 12px;"
            />
          </div>
        </div>
        <div class="config-header" v-if="multiSelectEnabled && configs.length > 0">
          <button class="btn danger" @click="removeConfigMultiple">🗑️ 批量删除
          </button>
          <button class="btn btn-submit" @click="batchCopyConfigs">📋 批量复制
          </button>
          <button class="btn btn-submit" v-if="batchJson.selectedConfigs.size>0" @click="batchJson.batch.show=true">📝
            批量修改
          </button>
        </div>
      </div>

      <div class="external-pop-up-frame">
        <!-- 弹窗 -->
        <el-dialog
            v-if="currentConfig"
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
            v-if="currentConfig"
            v-model="currentConfig.showPhysicalDialogFromDomain"
            title="调整树脂使用顺序与启用状态"
            width="520px"
            direction="rtl"
            :close-on-click-modal="false"
        >
          <div class="dialog-content">
            <div class="selector-title">拖拽调整顺序</div>
            <draggable
                v-if="currentConfig"
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
              <el-button @click="currentConfig.showPhysicalDialogFromDomain = false">关闭</el-button>
            </div>
          </div>
        </el-dialog>
        <el-dialog
            v-if="currentConfig"
            v-model="currentConfig.showPhysicalDialogFromStygianOnslaught"
            title="调整树脂使用顺序与启用状态"
            width="520px"
            direction="rtl"
            :close-on-click-modal="false"
        >
          <div class="dialog-content">
            <div class="selector-title">拖拽调整顺序</div>
            <draggable
                v-if="currentConfig"
                v-model="currentConfig.autoStygianOnslaught.physical"
                item-key="name"
                handle=".draggable-item"
                @end="updatePhysicalOrder(currentConfig)"
            >
              <template #item="{ element }">
                <div class="draggable-item">
                  <span class="drag-handle">☰</span>
                  <span class="physical-name">{{ element.name }}</span>
                  <div class="physical-count">
                    <span class="physical-count-label">运行次数:</span>
                    <el-input-number class="physical-count-number" width="10px" v-model="element.count" min="0"
                                     placeholder="运行次数" style="width: 100px;"></el-input-number>
                  </div>
                  <el-switch
                      v-model="element.open"
                      @change="updatePhysicalOrder(currentConfig)"
                  />
                </div>
              </template>
            </draggable>

            <div class="dialog-actions" style="margin-top: 24px; text-align: right;">
              <el-button @click="currentConfig.showPhysicalDialogFromStygianOnslaught = false">关闭</el-button>
            </div>
          </div>
        </el-dialog>
        <!-- 主内容区保持原样，只在最外层加一个抽屉 -->
        <el-drawer
            v-model="showResultDrawer"
            direction="rtl"
            :with-header="true"
            :close-on-press-escape="true"
            :modal="true"
            class="result-drawer"
        >
          <template #header>
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
        <el-drawer
            v-model="batchJson.batch.show"
            direction="rtl"

            :with-header="true"
            :close-on-press-escape="true"
            :modal="true"
            class="batch-drawer"
        >
          <template #header>
            <span style="font-weight: bold; color: #409eff;">批量配置</span>
          </template>
          <div class="drawer-content">
            <div class="batch-card" style="margin-top: 24px;">
              <div class="card-header">
                <label class="result-key">秘境配置</label>
              </div>
              <div class="batch-item">
                <label>队伍名称（可选）：</label>
                <input class="limited-input" v-model="batchJson.batch.autoFight.partyName"
                       placeholder="队伍1 / 主C+副C+辅助"/>
              </div>
            </div>

            <div class="batch-card" style="margin-top: 24px;">
              <div class="card-header">
                <label class="result-key">地脉配置</label>
              </div>
              <div class="batch-item">
                <label>队伍名称（可选）：</label>
                <input class="limited-input" v-model="batchJson.batch.autoLeyLineOutcrop.team"
                       placeholder="队伍1 / 主C+副C+辅助"/>
              </div>
              <div class="batch-item">
                <label>好感队伍名称（可选）：</label>
                <input class="limited-input" v-model="batchJson.batch.autoLeyLineOutcrop.friendshipTeam"
                       placeholder="队伍1"/>
              </div>
            </div>
            <div class="batch-card" style="margin-top: 24px;">
              <div class="card-header">
                <label class="result-key">幽境配置</label>
              </div>
              <div class="batch-item">
                <label>队伍名称（可选）：</label>
                <input class="limited-input" v-model="batchJson.batch.autoStygianOnslaught.friendshipTeam"
                       placeholder="队伍1 / 主C+副C+辅助"/>
              </div>
              <div class="batch-item">
                <label>指定刷取战场（可选）：</label>
                <select v-model="batchJson.batch.autoStygianOnslaught.bossNum">
                  <option :value="undefined">请选择</option>
                  <option
                      v-for="type in [{key:'战场一',value:1},{key:'战场二',value:2},{key:'战场三',value:3}] "
                      :key="type.key"
                      :value="type.value"
                  >
                    {{ type.key }}
                  </option>
                </select>
              </div>
            </div>
          </div>

          <!-- 可选：底部操作 -->
          <template #footer>
            <div style="text-align: right;">
              <el-button @click="batchUpdate">📝批量修改</el-button>
            </div>
          </template>
        </el-drawer>
      </div>

      <div class="content-area">

        <div class="config-list">
          <div v-for="(config,index) in configs" :key="index" class="config-item">
            <!--            <el-checkbox v-model="isConfigSelected"-->
            <!--                         v-if="multiSelectEnabled"></el-checkbox>-->
            <el-checkbox
                :model-value="isConfigSelected(config.id)"
                @update:model-value="(val) => handleConfigSelection(config.id, val)"
                v-if="multiSelectEnabled"
            ></el-checkbox>
            <h3>#{{ index }} 配置</h3>
            <hr/>
            <div class="common-section">
              <div class="form-group common">
                <label>执行顺序：</label>
                <input class="limited-input" @change="debouncedSort" v-model.number="config.order" type="number" min="1"
                       max="99999999"
                       placeholder="建议 1~10"/>
                <span style="color: red;">数值高的优先执行</span>
              </div>
              <div class="form-group common">
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
                  {{ config.dayName || '已选择 ' + config.days?.length || 0 + ' 天' }}
                </span>
                </div>
              </div>
              <div class="form-group common">
                <label>执行类型：</label>
                <select v-model="config.runType">
                  <option value="">请选择执行类型</option>
                  <option
                      v-for="type in runTypes"
                      :key="type"
                      :value="type"
                  >
                    {{ type }}
                  </option>
                </select>
              </div>
            </div>
            <div class="domain-section" v-if="config.runType === runTypes[0]">
              <div class="form-group domain"
                   v-if="config.selectedType&&!excludeDomainTypes.includes(config.selectedType)">
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
              <div class="form-group domain">
                <label>秘境类型：</label>
                <select v-model="config.selectedType"
                        @change="handleSundaySelection(config)">
                  <option value="">请选择秘境类型</option>
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
              <div class="form-group domain" v-if="!config.autoFight.sundaySelectedDomain">
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
              <div class="form-group domain" v-else>
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
              <div class="form-group domain"
                   v-if="config.autoFight.domainName&&domainMap.get(config.autoFight.domainName)?.hasOrder">
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
              <div class="form-group domain"
                   v-else-if="(!config.autoFight.domainName)&&config.selectedType&&!excludeDomainTypes.includes(config.selectedType)">
                <label>周日/限时材料：</label>
                <select
                    v-model="config.autoFight.sundaySelectedDomain"
                    @change="handleSundaySelection(config)">
                  <option
                      v-for="(item) in getFilteredMaterials(config)|| []"
                      :key="item.name"
                      :value="item"
                  >
                    {{ item.name }}
                  </option>
                </select>
              </div>

              <div
                  v-else-if="excludeDomainTypes.includes(config.selectedType)&&(!domainMap.get(config.autoFight.domainName)?.hasOrder)&&(domainMap.get(config.autoFight.domainName)?.list?.length>0)"
                  class="form-group domain">
                <label>秘境圣遗物：</label>
                <ul>
                  <li v-for="item in domainMap.get(config.autoFight.domainName)?.list" :key="item">
                    {{ item }}
                  </li>
                </ul>
              </div>

              <div class="form-group domain">
                <label>队伍名称（可选）：</label>
                <input class="limited-input" v-model="config.autoFight.partyName" placeholder="队伍1 / 主C+副C+辅助"/>
              </div>
              <div class="form-group domain">
                <label>副本轮数：</label>
                <input class="limited-input" v-model.number="config.autoFight.domainRoundNum" type="number" min="1"
                       max="99"
                       placeholder="建议 1~10"/>
              </div>
              <!--          <hr/>-->
              <div class="form-group domain">
                <label>树脂使用顺序：</label>
                <!-- 原 physical-display 改成 -->
                <div
                    class="physical-display"
                    @click="handleCurrentConfig(config,'show-physical-domain')"
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
            </div>

            <div class="leyLineOutcrop-section" v-else-if="config.runType === runTypes[1]">
              <div class="form-group leyLineOutcrop">
                <label>地脉类型：</label>
                <select v-model="config.autoLeyLineOutcrop.leyLineOutcropType">
                  <option value="">请选择地脉类型</option>
                  <option
                      v-for="item in leyLineOutcropTypes"
                      :key="item.value"
                      :value="item.name"
                      :default="leyLineOutcropTypes[0].name"
                  >
                    {{ item.value }}
                  </option>
                </select>
              </div>
              <div class="form-group leyLineOutcrop">
                <label>国家/地区：</label>
                <select v-model="config.autoLeyLineOutcrop.country">
                  <option value="">请选择国家/地区</option>
                  <option
                      v-for="item in countryList"
                      :key="item"
                      :value="item"
                  >
                    {{ item }}
                  </option>
                </select>
              </div>
              <div class="form-group leyLineOutcrop">
                <label>刷取次数：</label>
                <input
                    class="limited-input"
                    v-model.number="config.autoLeyLineOutcrop.count"
                    type="number"
                    default="1"
                    min="0"
                />
              </div>

              <div class="form-group leyLineOutcrop">
                <label>使用队伍：</label>
                <input
                    class="limited-input"
                    v-model="config.autoLeyLineOutcrop.team"
                    placeholder="队伍ID / 队伍名称"
                />
              </div>

              <div class="form-group leyLineOutcrop">
                <label>好感队伍（可选）：</label>
                <input
                    class="limited-input"
                    v-model="config.autoLeyLineOutcrop.friendshipTeam"
                    placeholder="好感刷取队伍"
                />
              </div>


              <div class="form-group leyLineOutcrop checkbox-group" style="display: flex; flex-wrap: wrap; gap: 16px;">
                <el-checkbox v-model="config.autoLeyLineOutcrop.useAdventurerHandbook">
                  使用冒险之证
                </el-checkbox>
                <el-checkbox v-model="config.autoLeyLineOutcrop.useFragileResin">
                  使用脆弱树脂
                </el-checkbox>
                <el-checkbox v-model="config.autoLeyLineOutcrop.useTransientResin">
                  使用须臾树脂
                </el-checkbox>
                <el-checkbox v-model="config.autoLeyLineOutcrop.isGoToSynthesizer">
                  合成浓缩树脂
                </el-checkbox>
                <el-checkbox v-model="config.autoLeyLineOutcrop.isNotification">
                  完成后通知
                </el-checkbox>
              </div>

              <div class="form-group leyLineOutcrop">
                <label>战斗超时时间（秒）：</label>
                <input
                    class="limited-input"
                    v-model.number="config.autoLeyLineOutcrop.timeout"
                    type="number"
                    min="0"
                    default="120"
                    placeholder="0 = 不限制"
                />
              </div>
            </div>
            <div class="stygianOnslaught-section" v-if="config.runType === runTypes[2]">
              <div class="form-group stygianOnslaught">
                <label>队伍名称（可选）：</label>
                <input class="limited-input" v-model="config.autoStygianOnslaught.friendshipTeam"
                       placeholder="队伍1 / 主C+副C+辅助"/>
              </div>
              <div class="form-group stygianOnslaught">
                <label>指定刷取战场（可选）：</label>
                <!--              <el-input-number min="1" max="3" v-model="config.autoStygianOnslaught.bossNum"-->
                <!--                               placeholder="战场一,战场二,战场三"/>-->
                <select v-model="config.autoStygianOnslaught.bossNum">
                  <option :value="undefined">请选择</option>
                  <option
                      v-for="type in [{key:'战场一',value:1},{key:'战场二',value:2},{key:'战场三',value:3}] "
                      :key="type.key"
                      :value="type.value"
                  >
                    {{ type.key }}
                  </option>
                </select>
              </div>
              <div class="form-group stygianOnslaught">
                <label>自定义树脂使用：</label>
                <el-switch
                    v-model="config.autoStygianOnslaught.specifyResinUse"
                />
              </div>
              <!--          <hr/>-->
              <div class="form-group stygianOnslaught">
                <label>自定义树脂使用顺序：</label>
                <!-- 原 physical-display 改成 -->
                <div
                    class="physical-display"
                    @click="handleCurrentConfig(config,'show-physical-stygianOnslaught')"
                >
                <span>
                  {{
                    config.autoStygianOnslaught.physical
                        .filter(p => p.open)
                        .map(p => p.name + p.count + "次")
                        .join(' → ') || '未选择'
                  }}
                </span>
                </div>
              </div>
            </div>
            <div class="config-btn">
              <!-- 删除按钮 -->
              <button class="btn danger" @click="removeConfig(config.id)">🗑️ 删除</button>
              <button class="btn btn-submit" @click="addConfig(config)">拷贝一份</button>
            </div>
          </div>
        </div>
        <!-- 右侧固定触发按钮（悬浮在页面右中部） -->
        <div class="fixed-trigger" @click="showResultDrawer = true" title="查看/复制配置结果">
          <i class="el-icon-document"></i>
          <span>查看/复制配置结果</span>
        </div>
        <!--        <div class="fixed-search" @click="showResultDrawer = true" title="查看/复制配置结果">
                  <i class="el-icon-document"></i>
                  <span>sous</span>
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
  position: fixed; /* 固定定位 */
  top: 0; /* 距离顶部为 0 */
  left: 0; /* 距离左侧为 0 */
  width: 100%; /* 占满整个视口宽度 */
  z-index: 1000; /* 确保在最上层 */
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); /* 添加阴影 */
  border-radius: 12px; /* 添加圆角 */
  padding: 10px 20px; /* 内边距 */
}

/* 内容区域补偿高度 */
.content-area {
  margin-top: 300px; /* 根据 .fixed-container 的实际高度调整 */
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
  max-width: 100px;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.uid-input:focus {
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
  max-width: 300px;
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

/* 配置按钮容器 - 均匀分布 */
.config-btn {
  display: flex;
  justify-content: space-between; /* 横向均匀分布 */
  align-items: center; /* 垂直居中对齐 */
  gap: 12px; /* 元素间间距 */
  margin-top: 16px;
  padding: 8px 0;
}

/* 确保按钮在容器中有合适的最小宽度 */
.config-btn .btn {
  flex: 1; /* 平均分配空间 */
  min-width: 120px; /* 最小宽度 */
  text-align: center; /* 文字居中 */
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

.leyLineOutcrop-section .checkbox-group {
  display: grid;
  grid-template-columns: repeat(2, 1fr); /* 強制兩欄 */
  gap: 16px 32px; /* 行距 16px，列距 32px 可調 */
  margin: 16px 0 24px;
  padding: 12px 0;
  border-top: 1px dashed rgba(100, 100, 100, 0.15); /* 可選：加條分隔線好看 */
}

.leyLineOutcrop-section .checkbox-group .el-checkbox {
  /* 可選：讓文字靠左對齊 */
  justify-self: start;
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
  max-height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;

  background-color: #85ce61; /* 白色背景 */
  color: #000000; /* 黑色文字 */
  padding: 4px 8px; /* 内边距 */
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
  right: 10px;
  top: 80%;
  transform: translateY(-50%);
  z-index: 999;
  width: 60px;
  height: 200px;
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
  min-width: 80% !important;
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
  max-width: 500px;
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

/* 批量操作区域样式 */
.batch-operation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.select-all-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-count {
  color: #409eff;
  font-weight: 500;
  font-size: 0.9rem;
}

/* 批量操作按钮区域美化 */
.batch-buttons {
  display: flex;
  gap: 12px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea 0%, #d51999 100%);
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  overflow: visible; /* 禁止内容超出容器 */
}

.batch-buttons:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 批量操作按钮美化 */
.batch-buttons .btn {
  position: relative;
  overflow: hidden;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.batch-buttons .btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.batch-buttons .btn:hover::before {
  left: 100%;
}

.batch-buttons .btn.danger {
  background: linear-gradient(135deg, #ff6b6b, #ee5a52);
  color: white;
}

.batch-buttons .btn.danger:hover {
  background: linear-gradient(135deg, #ff5252, #e53935);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 82, 82, 0.4);
}

.batch-buttons .btn.btn-submit {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  color: white;
}

.batch-buttons .btn.btn-submit:hover {
  background: linear-gradient(135deg, #00c6ff, #0072ff);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 198, 255, 0.4);
}

/* 按钮图标动画 */
.batch-buttons .btn i {
  margin-right: 8px;
  transition: transform 0.3s ease;
}

.batch-buttons .btn:hover i {
  transform: scale(1.2) rotate(10deg);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .batch-buttons {
    flex-direction: column;
    gap: 8px;
  }

  .batch-buttons .btn {
    width: 100%;
    text-align: center;
  }
}

/* 按钮按下效果 */
.batch-buttons .btn:active {
  transform: translateY(0) scale(0.98);
  transition: all 0.1s ease;
}


.config-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.config-checkbox {
  zoom: 1.2; /* 稍微放大复选框 */
}

.config-item {
  position: relative;
  border-left: 3px solid transparent;
  transition: all 0.3s ease;
}

.config-item:hover {
  border-left-color: #409eff;
}

/* 当配置被选中时的样式 */
.config-item:has(.config-checkbox:checked) {
  background: rgba(64, 158, 255, 0.05);
  border-left-color: #409eff;
}

.batch-drawer {
  --el-drawer-bg-color: rgba(206, 33, 33, 0.96);
  --el-drawer-border-color: #1b3e8f;
  min-width: 60% !important;
  background: #fadbd8;
  backdrop-filter: blur(6px);
}

.batch-card {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  background: #e8e8e8;
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
}

.batch-item {
  margin-bottom: 4px;
  padding: 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

/*.batch-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}*/

.batch-item label {
  font-weight: 500;
  color: #495057;
  margin-bottom: 8px;
  display: block;
}

.batch-item .limited-input {
  width: 20%;
  padding: 10px 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  transition: border-color 0.3s ease;
}

.batch-item .limited-input:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.batch-item select {
  width: 25%;
  padding: 10px 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  background-color: #fff;
  transition: border-color 0.3s ease;
  cursor: pointer;
}

.batch-item select:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.batch-item select:hover {
  border-color: #409eff;
}
</style>