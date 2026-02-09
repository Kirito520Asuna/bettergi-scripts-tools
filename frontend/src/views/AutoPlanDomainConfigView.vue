<script setup>
import {ref, computed, watch, watchEffect, onMounted} from 'vue'
import {ElMessage} from "element-plus";
import {getBaseJsonAll, getUidJson, postUidJson} from "@api/domain/autoPlan.js";
import {CopyToClipboard} from "@utils/local.js";
// 配置列表 → 核心数据结构改为 array
const configs = ref([])
const isLoading = ref(false);
// 秘境数据（保持不变，建议单独抽到一个文件）
const defaultDomains = [
  {
    "name": "无光的深都",
    "type": "天赋",
    "hasOrder": true,
    "list": [
      "「月光」的哲学",
      "「乐园」的哲学",
      "「浪迹」的哲学"
    ]
  },
  {
    "name": "蕴火的幽墟",
    "type": "天赋",
    "hasOrder": true,
    "list": [
      "「角逐」的哲学",
      "「焚燔」的哲学",
      "「纷争」的哲学"
    ]
  },
  {
    "name": "苍白的遗荣",
    "type": "天赋",
    "hasOrder": true,
    "list": [
      "「公平」的哲学",
      "「正义」的哲学",
      "「秩序」的哲学"
    ]
  },
  {
    "name": "昏识塔",
    "type": "天赋",
    "hasOrder": true,
    "list": [
      "「诤言」的哲学",
      "「巧思」的哲学",
      "「笃行」的哲学"
    ]
  },
  {
    "name": "董色之庭",
    "type": "天赋",
    "hasOrder": true,
    "list": [
      "「浮世」的哲学",
      "「风雅」的哲学",
      "「天光」的哲学"
    ]
  },
  {
    "name": "太山府",
    "type": "天赋",
    "hasOrder": true,
    "list": [
      "「繁荣」的哲学",
      "「勤劳」的哲学",
      "「黄金」的哲学"
    ]
  },
  {
    "name": "忘却之峡",
    "type": "天赋",
    "hasOrder": true,
    "list": [
      "「自由」的哲学",
      "「抗争」的哲学",
      "「纷争」的哲学"
    ]
  }
  //================================
  ,
  {
    "name": "失落的月庭",
    "type": "武器",
    "hasOrder": true,
    "list": [
      "奇巧秘器的真愿",
      "长夜火的烈辉",
      "终北遗嗣的煌熠"
    ]
  },
  {
    "name": "深古瞭望所",
    "type": "武器",
    "hasOrder": true,
    "list": [
      "神合秘烟的启示",
      "谚妄圣主的神面",
      "贡祭炽心的荣膺"
    ]
  },
  {
    "name": "深潮的余响",
    "type": "武器",
    "hasOrder": true,
    "list": [
      "悠古弦音的回响",
      "纯圣露滴的真粹",
      "无垢之海的金杯"
    ]
  },
  {
    "name": "有顶塔",
    "type": "武器",
    "hasOrder": true,
    "list": [
      "谧林涓露的金符",
      "绿洲花园的真谛",
      "烈日威权的旧日"
    ]
  },
  {
    "name": "砂流之庭",
    "type": "武器",
    "hasOrder": true,
    "list": [
      "远海夷地的金枝",
      "鸣神御灵的勇武",
      "今昔剧画之鬼人"
    ]
  },
  {
    "name": "震雷连山密宫",
    "type": "武器",
    "hasOrder": true,
    "list": [
      "孤云寒林的神体",
      "雾海云间的转还",
      "漆黑陨铁的一块"
    ]
  },
  {
    "name": "塞西莉亚苗圃",
    "type": "武器",
    "hasOrder": true,
    "list": [
      "高塔孤王的碎梦",
      "凛风奔狼的怀乡",
      "狮牙斗士的理想"
    ]
  }
  //================================
  ,
  {
    "name": "月童的库藏",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "风起之日",
      "晨星与月的晓歌"
    ]
  },
  {
    "name": "霜凝的机枢",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "纺月的夜歌",
      "穹境示现之夜"
    ]
  },
  {
    "name": "荒废砌造坞",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "深廊终曲",
      "长夜之誓"
    ]
  },
  {
    "name": "虹灵的净土",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "黑曜秘典",
      "城勇者绘卷"
    ]
  },
  {
    "name": "褪色的剧场",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "未竟的遐思",
      "谐律异想断章"
    ]
  },
  {
    "name": "临瀑之城",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "回声之林夜话",
      "昔时之歌"
    ]
  },
  {
    "name": "罪祸的终末",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "黄金剧团",
      "逐影猎人"
    ]
  },
  {
    "name": "熔铁的孤塞",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "花海甘露之光",
      "水仙之梦"
    ]
  },
  {
    "name": "赤金的城墟",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "乐园遗落之花",
      "沙上楼阁史话"
    ]
  },
  {
    "name": "缘觉塔",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "饰金之梦",
      "深林的记忆"
    ]
  },
  {
    "name": "沉眠之庭",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "海染砗磲",
      "华馆梦醒形骸记"
    ]
  },
  {
    "name": "花染之庭",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "绝缘之旗印",
      "追忆之注连"
    ]
  },
  {
    "name": "岩中幽谷",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "辰砂往生录",
      "来歆余响"
    ]
  },
  {
    "name": "华池岩柚",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "染血的骑士道",
      "昔日宗室之仪"
    ]
  },
  {
    "name": "无妄引答密宫",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "炽烈的炎之魔女",
      "渡过烈火的贤人"
    ]
  },
  {
    "name": "孤云凌霄之处",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "悠古的磐岩",
      "逆飞的流星"
    ]
  },
  {
    "name": "山脊守望",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "千岩牢固",
      "苍白之火"
    ]
  },
  {
    "name": "芬德尼尔之顶",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "冰风迷途的勇士",
      "沉沦之心"
    ]
  },
  {
    "name": "铭记之谷",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "翠绿之影",
      "被怜爱的少女"
    ]
  },
  {
    "name": "仲夏庭园",
    "type": "圣遗物",
    "hasOrder": false,
    "list": [
      "如雷的盛怒",
      "平息鸣雷的尊者"
    ]
  }
];
const domains = ref([])
const fetchDomains = async () => {
  isLoading.value = true;
  try {
    // const response = await service.get('/auto/plan/domain/json/all');
    const response = await getBaseJsonAll()
    console.log('response', response)
    if (response&&response.length>0) {
      domains.value = response;
    }else {
      domains.value = defaultDomains;
      ElMessage({
        type: 'warning',
        message: '无数据存储，使用默认秘境数据。',
      });
    }
  } catch (error) {
    console.error('请求失败:', error);
    domains.value = defaultDomains;
    ElMessage({
      type: 'warning',
      message: '使用默认秘境数据。',
    });
  } finally {
    isLoading.value = false;
  }
};
const submitConfigToBackend = async () => {
  if (!uid.value) {
    ElMessage.warning("请先设置 UID");
    return;
  }
/*  const jsonData = getFinalConfigsMap(); // 获取 JSON 配置
  const json= jsonData?.get(uid.value)||jsonData*/
  const json= getFinalConfigs()
  await postUidJson(uid.value,JSON.stringify(json))
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
    // domains.value = defaultDomains;
    ElMessage({
      type: 'error',
      message: error.message,
    });
  } finally {
  }
};
onMounted(() => {
  fetchDomains();
})

/*
const selectedType = ref(""); // 当前选择的秘境类型

// 根据 selectedType 过滤秘境列表
const filteredDomains = computed((selectedType) => {
  if (!selectedType) return [];
  return domains.value.filter(d => d.type === selectedType.value);
});
*/


const uid = ref("")
// 新增一条空白配置
const addConfig = () => {
  const newOrder = configs.value.length === 0
      ? 1
      : Math.max(...configs.value.map(c => c.order)) + 1

  configs.value.push({
    order: newOrder,
    day: undefined,
    dayName: undefined,
    selectedType: "", // 新增字段
    autoFight: {
      domainName: undefined,
      partyName: undefined,
      sundaySelectedValue: undefined,
      // sundaySelectedName: undefined,
      DomainRoundNum: undefined
    }
  })
}

// 删除某一条
const removeConfig = (order) => {
  configs.value = configs.value.filter(c => c.order !== order)
  // 可选：重新排序 order（如果前端需要显示连续的序号）
  // configs.value.forEach((c, i) => { c.order = i + 1 })
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
const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
// 监听每一项的 domainName 变化 → 自动填充 sundaySelectedValue
watchEffect(
    () => configs.value,
    (newConfigs) => {
      newConfigs.forEach(config => {
        const domainName = config.autoFight.domainName
        if (!domainName) {
          config.autoFight.sundaySelectedValue = undefined
          // config.autoFight.sundaySelectedName = undefined
          return
        }

        const domain = domainMap.value.get(domainName)
        if (!domain) return

        // 处理 sundaySelectedValue 和 sundaySelectedName
        // if (typeof config.autoFight.sundaySelectedValue === 'number') {
        //   const index = config.autoFight.sundaySelectedValue - 1;
        //   config.autoFight.sundaySelectedName = domain.list?.[index] || '';
        // } else {
        //   config.autoFight.sundaySelectedName = config.autoFight.sundaySelectedValue || '';
        // }
        if (typeof config.day === 'number') {
          config.dayName = weekDays[config.day] || '';
        } else {
          config.dayName = config.day || '';
        }

        if (domain.hasOrder && domain.list?.length > 0) {
          // 自动选第一个（也可改为 undefined，让用户手动选）
          if (!config.autoFight.sundaySelectedValue) {
            config.autoFight.sundaySelectedValue = domain.list[0]
            // config.autoFight.sundaySelectedName = domain.list[0]
          }
        } else {
          config.autoFight.sundaySelectedValue = config.autoFight.sundaySelectedValue || undefined
          // config.autoFight.sundaySelectedName = config.autoFight.sundaySelectedName || ''
        }
      })
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

    let json = {
      order: c.order,
      day: c.day,
      dayName: c.dayName,
      selectedType: c.selectedType, // 新增字段
      autoFight: autoFight
    };
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
    const autoFight = item.autoFight;
    key += (autoFight.partyName || "")
    key += "|"
    key += (autoFight.domainName)
    key += "|"
    key += (autoFight.DomainRoundNum || "")
    key += "|"
    key += (autoFight.sundaySelectedValue || 1)
    key += "|"
    key += (item.day || "")
    key += "|"
    key += (item.order || 1) + ","
  })
  if (key.endsWith(",")) {
    key = key.substring(0, key.length - 1);
  }
  return key
}

const copyToClipboard = (text) => {
  CopyToClipboard(text)
};
</script>

<template>
  <div class="home">
    <div class="container">
      <h2 class="title">自动秘境计划配置列表</h2>
      <div class="config-header">
        <input type="text" v-model="uid" placeholder="设置 UID" class="uid-input"/>
        <!-- 添加配置按钮 -->
        <button @click="addConfig" class="btn btn-add">➕ 添加一条配置</button>
        <button @click="submitConfigToBackend" class="btn btn-submit">提交配置</button>
        <button @click="findDomains" class="btn btn-submit">查询UID配置</button>
      </div>
      <div class="config-list">
        <div v-for="config in configs" :key="config.order" class="config-item">
          <h3>#{{ config.order }} 配置</h3>
          <!-- 删除按钮 -->
          <button @click="removeConfig(config.order)" class="btn danger">🗑️ 删除</button>
          <div class="form-group">
            <label>执行顺序：</label>
            <input class="limited-input" v-model.number="config.order" type="number" min="1" max="99999999"
                   placeholder="建议 1~10"/>
          </div>
          <div class="form-group">
            <label>执行日：</label>
            <select v-model="config.day">
              <option value="">请选择执行日(默认每天执行)</option>
              <option
                  v-for="(d, index) in weekDays"
                  :key="d"
                  :value="index"
              >
                {{ d }}
              </option>
            </select>
          </div>
          <!-- 秘境选择 -->
          <!-- 新增 type 选择器 -->
          <div class="form-group">
            <label>秘境类型：</label>
            <select v-model="config.selectedType">
              <option value="">请选择类型</option>
              <option value="天赋">天赋</option>
              <option value="武器">武器</option>
              <option value="圣遗物">圣遗物</option>
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
            <input class="limited-input" v-model.number="config.autoFight.DomainRoundNum" type="number" min="1" max="99"
                   placeholder="建议 1~10"/>
          </div>

          <!--          <hr/>-->
        </div>
      </div>
      <div class="result-all">
        <label class="result-key">Json配置:</label>
        <pre class="result">{{ getFinalConfigsMapShow() || '暂无返回数据' }}</pre>
        <button @click="copyToClipboard(getFinalConfigsMapShow())" class="copy-btn">📋 复制</button>
      </div>
      <div class="result-all">
        <label class="result-key">语法key:</label>
        <pre class="result">{{ getFinalConfigsToKey() || '暂无返回数据' }}</pre>
        <button @click="copyToClipboard(getFinalConfigsToKey())" class="copy-btn">📋 复制</button>
      </div>
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
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
  overflow: hidden; /* 禁止内容超出容器 */
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

/* 主标题美化 */
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
</style>