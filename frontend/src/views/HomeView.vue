<template>
  <div class="home">
    <div v-if="RestartClick" class="restart-overlay" @keydown.esc.prevent tabindex="0">
      <div class="restart-modal">
        <div class="warning-header">
          <h3>系统正在重启</h3>
        </div>

        <div class="spinner"></div>

        <div class="loading-text">正在执行重启</div>

        <p class="hint">
          请勿关闭界面或刷新<br>
          预计需要 1–5 分钟，完成后将自动跳转
        </p>
      </div>
    </div>

    <div class="welcome-card">
      <img class="logo" src="@assets/logo.svg" alt="Logo"/>
      <h2 class="title">{{ currentRoute.meta.title || 'HOME' }}</h2>
      <p class="subtitle">欢迎使用扩展工具</p>

      <!-- 外层结构遍历 -->
      <div v-for="group in featureGroup" :key="group.title" class="feature-section">
        <h3 class="section-title" v-if="group.children.length > 0">{{ group.title }}</h3>
        <div class="feature-container">
          <!-- 左侧功能列表 -->
          <div class="feature-column">
            <div
                v-for="item in getItemsByPosition(group.children, 'left')"
                :key="item.id"
                :style="{ backgroundColor: buttonBackgrounds[item.id] }"
                :class="['feature-item', getItemClass(item)]"
            >
              <!--              <span class="icon">{{ getIcon(item) }}</span>-->
              <span v-html="getIcon(item)" class="icon"></span>
              <button class="name" v-if="item.isUi" @click="togo(item)"
              >
                {{ item.name }}
              </button>
              <button class="name" v-else @click="toClick(item)"
              >{{ item.name }}
              </button>
            </div>
          </div>

          <!-- 右侧功能列表 -->
          <div class="feature-column">
            <div
                v-for="item in getItemsByPosition(group.children, 'right')"
                :key="item.id"
                :style="{ backgroundColor: buttonBackgrounds[item.id] }"
                :class="['feature-item', getItemClass(item)]"
            >
              <!--              <span class="icon">{{ getIcon(item) }}</span>-->
              <span v-html="getIcon(item)" class="icon"></span>
              <button class="name" v-if="item.isUi" @click="togo(item)"
              >{{ item.name }}
              </button>
              <button class="name" v-else @click="toClick(item)"
              >{{ item.name }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <router-view/>
  </div>
</template>

<script setup>
import {ref, onMounted} from "vue";
import router from "@router/router";
import {iconAsMapDefault} from "@utils/defaultdata.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {restart} from "@api/web/web.js";

let iconAsMap = iconAsMapDefault()

const currentRoute = ref(router.currentRoute)
// 统一管理所有功能项
const featureGroup = ref([]);
// 存储每个按钮的随机背景色
const buttonBackgrounds = ref({});

// 生成随机浅色函数
const getRandomLightColor = () => {
  const r = Math.floor(Math.random() * 106) + 150; // 150-255
  const g = Math.floor(Math.random() * 106) + 150; // 150-255
  const b = Math.floor(Math.random() * 106) + 150; // 150-255
  return `rgb(${r}, ${g}, ${b})`;
};
const lightColors = [
  '#f8d7da',
  '#d1ecf1',
  'rgba(116,181,181,0.56)',
  '#e1c7ba',
  'rgba(255,141,195,0.54)',
  '#ced4da',
];

async function loadUi() {
  const order_group_map_json = new Map([
    ['JS扩展功能', {order:3,env:['prod','dev']}],
    ['系统', {order:1,env: ['prod','dev']}],
    ['演示', {order:4,env: ['dev']}],
    ['配置分析', {order:2,env: ['prod','dev']}]
  ]);

  const group_list = new Array();
  let index = 1
  const list = [
    // {isLink: true, name: 'API 调试链接', value: 'API 调试链接'},
    {isSwagger: true, group: "系统", name: 'Swagger 文档入口', value: 'doc.html'},
    {isLink: true, group: "系统", name: 'BGI 仓库', value: 'https://bgi.sh'},
    // {isRote: true, name: '路由管理面板', value: '路由管理面板'},
    {name: '退出登录', group: "系统", value: 'Logout'},
    {name: '重启', group: "系统", value: 'Restart'},
    // {name: '设置', group: "系统", value: 'Settings'},
    // {name: '日志', group: "系统", value: 'Logs'},
  ]

  list.forEach(item => {
    group_list.push({
      group: item?.group || '扩展功能列表',
      isRote: item.isRote || false,
      isLink: item.isLink || false,
      isSwagger: item.isSwagger || false,
      icon: item.icon || iconAsMap.get(item.value),
      name: item.name,
      value: item.value
    });
    index++
  })

  router.getRoutes().filter(route => !route?.meta?.excludeInMenu).forEach(route => {
    group_list.push({
      group: route?.meta?.group || '扩展功能列表',
      isRote: true,
      isLink: false,
      isSwagger: false,
      icon: route?.meta?.icon || iconAsMap.get(route?.name),
      name: route?.meta?.title,
      value: route.path
    });
  });
  // console.log('group_list:'+JSON.stringify(group_list))

  const group = [...new Set(group_list.map(item => item?.group).filter(item => item))].sort((a, b) => {
    return  (order_group_map_json.get(a).order || 999) - (order_group_map_json.get(b).order || 999)
  });
  group.filter(o => order_group_map_json.get(o).env.includes(import.meta.env.VITE_BASE_ENV)).forEach((groupName) => {
    let groupJson = {
      title: groupName,
      children: []
    }
    let childIndex = 1
    group_list.filter(item => item.group === groupName).forEach(item => {
      groupJson.children.push({
        id: index,
        position: childIndex % 2 === 1 ? "left" : "right",
        isRote: item.isRote,
        isLink: item.isLink,
        isSwagger: item.isSwagger,
        isUi: (item.isSwagger || item.isRote || item.isLink),
        icon: item.icon || iconAsMap.get(item.value),
        name: item.name,
        value: item.value
      })
      index++
      childIndex++
    })

    featureGroup.value.push(groupJson);
  })


  // 初始化按钮背景色
  let colorIndex = 0;

  featureGroup.value.forEach((group) => {
    group.children.forEach((item) => {
      buttonBackgrounds.value[item.id] = lightColors[colorIndex % lightColors.length];
      colorIndex++;
    });
  });
}

onMounted(async () => {
  await loadUi();
});

// 获取图标
const getIcon = (item) => {
  // 优先使用 meta.icon，没有则根据类型给默认 emoji
  let rawIcon = item?.icon;
  if (rawIcon) {
    // 字符串处理
    if (typeof rawIcon === "string") {
      const trimmed = rawIcon.trim();
      // 如果是 img 字符串
      if (trimmed.trim().startsWith('<img')) {
        return trimmed.trim() // 直接返回字符串
      }
      // 如果是 PNG 图片路径或 Base64 数据
      if (trimmed.endsWith('.png') || trimmed.endsWith('.jpg') || trimmed.startsWith('data:image/png')) {
        return `<img src="${trimmed}" class="icon-png" />`;
      }
      // 如果是 SVG 字符串
      if (trimmed.trim().startsWith('<svg')) {
        return trimmed.trim() // 直接返回字符串
      }
      // 优先级 2：从 iconMap 中根据别名查找（新加的部分）
      const alias = item?.icon; // 假设别名放在 meta.iconAlias，或用 key/name
      if (alias && iconAsMap.has(trimmed)) {
        const svgOrEmoji = iconAsMap.get(trimmed);

        // 如果是 SVG 字符串
        if (typeof svgOrEmoji === "string" && svgOrEmoji.trim().startsWith("<svg")) {
          return svgOrEmoji.trim() // 直接返回字符串
        }
        // 如果是 emoji 或其他字符串
        return svgOrEmoji;
      }
    }
    return rawIcon;
  }
  rawIcon = item.isLink ? "🔗" : item.isSwagger ? "📖" : item.isRote ? "🚀" : "";
  // 其他情况兜底（比如传了奇怪的东西）
  return rawIcon;
};
// 获取样式类
const getItemClass = (item) => {
  return {
    "link-item": item.isLink,
    "swagger-item": item.isSwagger,
    "routes-item": item.isRote,
  };
};
// 根据 position 分组
const getItemsByPosition = (featureGroup, position) => {
  return featureGroup.filter((item) => item.position === position);
};

// 点击跳转
const togo = async (item) => {
  if (item?.isRote) {
    await ElMessageBox.confirm(`确定要访问${item.name}吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    try {
      await router.push(item.value);
    } catch (error) {
      console.error('路由跳转失败:', error);
    }
  } else if (item?.isSwagger) {
    const basePath = import.meta.env.VITE_BASE_API_PATH || '/bgi/';

    await ElMessageBox.confirm(`确定要访问文档吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    window.open(`${basePath}${item.value}`, '_blank');
  } else if (item?.isLink) {
    await ElMessageBox.confirm(`确定要访问[${item.name}]:${item.value}吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    window.open(item.value, '_blank');
  }
};
const RestartClick = ref(false)
const toClick = async (item) => {
  const value = item.value;
  if (value === 'Logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const token_name = import.meta.env.VITE_BASE_TOKEN_NAME || 'bgi_tools_token'
    localStorage.removeItem(token_name)
    router.push('/login')
  } else if (value === 'Settings') {
    await ElMessageBox.confirm('确定要前往设置吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    router.push('/settings')
  } else if (value === 'Restart') {
    await restart(RestartClick)
  } else {
    await ElMessageBox.confirm(`确定要前往${item.name}吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    router.push(value)
  }
}

</script>
<style scoped>
@import '@css/home.css';
</style>


