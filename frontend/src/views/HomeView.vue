<template>


  <div class="home">
    <div v-if="RestartClick" class="restart-overlay" @keydown.esc.prevent tabindex="0">
      <div class="restart-modal">
        <div class="warning-header">
          <span class="warning-icon">!</span>
          <h3>系统正在重启</h3>
        </div>

        <div class="spinner"></div>

        <div class="loading-text">正在执行重启</div>

        <p class="hint">
          请勿关闭界面或刷新<br>
          预计需要 10–60 秒，完成後將自動跳轉
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
import {getApplicationIds, restartService} from "@api/sys/sys.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {restart} from "@api/web/web.js";

let iconAsMap = iconAsMapDefault()

const currentRoute = ref(router.currentRoute)
// 统一管理所有功能项
const featureGroup = ref([]);
const list = [
  // {isLink: true, name: 'API 调试链接', value: 'API 调试链接'},
  {isSwagger: true, name: 'Swagger 文档入口', value: 'doc.html'},
  // {isRote: true, name: '路由管理面板', value: '路由管理面板'},
  {name: '退出登录', value: 'Logout'},
  {name: '重启', value: 'Restart'},
  {name: '设置', value: 'Settings'},
]
let index = 1
let initJson = {
  title: '功能列表',
  children: []
}
list.forEach(item => {
  initJson.children.push({
    id: index,
    position: index % 2 === 1 ? "left" : "right",
    isRote: item.isRote,
    isLink: item.isLink,
    isSwagger: item.isSwagger,
    isUi: (item.isSwagger || item.isRote || item.isLink),
    icon: item.icon || iconAsMap.get(item.value),
    name: item.name,
    value: item.value
  });
  index++
})
featureGroup.value.push(initJson);
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
  'rgba(116,181,181,0.56)',
  '#e1c7ba',
  'rgba(255,141,195,0.54)',
  '#ced4da'
];
const applicationIds = ref([])
onMounted(async () => {
  // try {
  //   const applicationIds1 = await getApplicationIds();
  //   applicationIds.value = applicationIds1.data
  // } catch (e) {
  //   ElMessage.warning(e.message)
  // }
  /*================*/
  let index = 1
  let routerJson = {
    title: '扩展功能列表',
    children: []
  }

  router.getRoutes().filter(route => (!route?.meta?.excludeInMenu) && route.name !== 'home' && route.name !== 'login' && route?.meta?.isRoot).forEach(route => {
    routerJson.children.push({
      id: index,
      position: index % 2 === 1 ? "left" : "right",
      isRote: true,
      isUi: true,
      icon: route?.meta?.icon || iconAsMap.get(route?.name),
      name: route?.meta?.title,
      value: route.path
    });
    index++
  });
  // console.log('getRoutes', router.getRoutes().filter(route => route.name !== 'home'))
  // console.log('routerJson', routerJson)
  featureGroup.value.push(routerJson);

  const homeRoute = router.getRoutes().find(route => route.name === 'home')
  index = 1
  let homeJson = {
    title: homeRoute?.meta?.asSubParentTitle,
    children: []
  }

  homeRoute.children.forEach(route => {
    routerJson.children.push({
      id: index,
      position: index % 2 === 1 ? "left" : "right",
      isRote: true,
      isUi: true,
      icon: route?.meta?.icon || iconAsMap.get(route?.name),
      name: route?.meta?.title,
      value: route.path
    });
    index++
  });
  featureGroup.value.push(homeJson);

  // 初始化按钮背景色
  let colorIndex = 0;

  featureGroup.value.forEach((group) => {
    group.children.forEach((item) => {
      buttonBackgrounds.value[item.id] = lightColors[colorIndex % lightColors.length];
      colorIndex++;
    });
  });
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
    try {
      await router.push(item.value);
    } catch (error) {
      console.error('路由跳转失败:', error);
    }
  } else if (item?.isSwagger) {
    const basePath = import.meta.env.VITE_BASE_API_PATH || '/bgi/';
    window.open(`${basePath}${item.value}`, '_blank');
  } else if (item?.isLink) {
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
  }else if(value === 'Settings'){
    await ElMessageBox.confirm('确定要前往设置吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    router.push('/settings')
  } else if (value === 'Restart') {
    await restart(RestartClick)
  }
}

</script>
<style scoped>

/* 页面全屏背景 */
.home {
  /*  display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    width: 100vw;*/
  /*  background: linear-gradient(135deg, #a1c4fd, #c2e9fb);*/
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url("@assets/MHY_XTLL.png");
  /* 关键：固定背景，不随滚动重复或变形 */
  background-attachment: fixed; /* ← 核心属性 */
  background-size: cover; /* 覆盖整个容器 */
  background-position: center;
  /*  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;*/
}

/* 中间卡片 */
.welcome-card {
  background: rgba(255, 255, 255, 0.95);
  padding: 50px 50px;
  border-radius: 25px;
  box-shadow: 0 15px 35px rgba(0, 255, 246, 0.2);
  text-align: center;
  max-width: 600px;
  width: 80%;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.welcome-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(255, 0, 181, 0.3);
}

/* Logo 圆角 */
.logo {
  /* width: 50px;
   height: 50px;*/
  object-fit: cover;
  border-radius: 50%;
  margin-bottom: 25px;
  border: 3px solid #6a89cc;
}

/* 主标题美化 */
.title {
  /* font-size: 36px;*/
  /*font-weight: 800;*/
  /*  margin-bottom: 5px;*/
  color: transparent;
  background: linear-gradient(90deg, #6a89cc, #3498db);
  -webkit-background-clip: text;
  background-clip: text;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.title:hover {
  transform: scale(1.05);
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

/* 副标题美化 */
.subtitle {
  /* font-size: 20px;*/
  color: #7f8c8d;
  /*margin-bottom: 40px;*/
  opacity: 0;
  animation: fadeIn 1s ease-in-out forwards;
  font-style: italic;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 功能区域 */
.feature-section {
  /*margin-top: 10px;*/
}

/* 美化 section-title */
.section-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 20px;
  color: transparent;
  background: linear-gradient(90deg, #6a89cc, #3498db);
  -webkit-background-clip: text;
  background-clip: text;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  text-align: center;
}

.section-title:hover {
  transform: scale(1.05);
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.feature-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.feature-column {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.feature-item {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 12px;
  padding: 15px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.feature-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.icon {
  display: inline-block;
  width: 1.2em;
  height: 1.2em;
  line-height: 1;
}

.icon svg {
  width: 100%;
  height: 100%;
  fill: currentColor; /* 让颜色跟随 CSS color */
}

.name {
  border: none;
  background: transparent;
  font-size: 16px;
  color: #3498db;
  cursor: pointer;
  font-weight: 500;
}

/* 类型区分 */
.link-item {
  background: #e8f8f5;
  color: #27ae60;
}

.swagger-item {
  background: #fef9e7;
  color: #f39c12;
}

.routes-item {
  background: #fadbd8;
  color: #e74c3c;
}

.restart-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.75);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  user-select: none; /* 防止選取文字 */
  -webkit-user-select: none;
}

.restart-modal {
  background: white;
  padding: 2.5rem;
  border-radius: 12px;
  text-align: center;
  min-width: 320px;
  max-width: 420px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4);
  pointer-events: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .feature-container {
    grid-template-columns: 1fr;
  }

  .welcome-card {
    padding: 30px 40px;
  }

  .title {
    font-size: 36px;
    font-weight: 800;
    color: transparent;
    background: linear-gradient(90deg, #6a89cc, #3498db);
    -webkit-background-clip: text;
    background-clip: text;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .title:hover {
    transform: scale(1.05);
    text-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  }

  .subtitle {
    font-size: 20px;
  }

  .restart-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.75);
    z-index: 9999;
    display: flex;
    align-items: center;
    justify-content: center;
    user-select: none; /* 防止選取文字 */
    -webkit-user-select: none;
  }

  .restart-modal {
    background: white;
    padding: 2.5rem;
    border-radius: 12px;
    text-align: center;
    min-width: 320px;
    max-width: 420px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4);
    pointer-events: auto;
  }

  /*
    !* 禁止點擊 overlay 本身關閉（點 modal 內部還是可以操作） *!
    .restart-overlay {
      pointer-events: auto;
    }

    .restart-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.85);
      backdrop-filter: blur(8px); !* 強毛玻璃，增加沉浸感 *!
      z-index: 9999;
      display: flex;
      align-items: center;
      justify-content: center;
      user-select: none;
      pointer-events: all; !* 完全攔截互動 *!
    }

    .restart-modal {
      background: rgba(20, 20, 28, 0.95); !* 深黑半透，與紅色對比強 *!
      border: 2px solid #ff4d4f; !* 紅色邊框警示 *!
      border-radius: 16px;
      padding: 2.5rem 4rem 3.5rem;
      min-width: 420px;
      max-width: 520px;
      text-align: center;
      box-shadow: 0 30px 80px rgba(255, 77, 79, 0.25), !* 紅色光暈陰影 *! 0 0 0 1px rgba(255, 77, 79, 0.15) inset,
      inset 0 0 40px rgba(0, 0, 0, 0.6);
      animation: modalPop 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
      pointer-events: auto;
    }*/
  @keyframes modalPop {
    0% {
      opacity: 0;
      transform: scale(0.7) translateY(40px);
    }
    60% {
      opacity: 1;
      transform: scale(1.05) translateY(-10px);
    }
    100% {
      opacity: 1;
      transform: scale(1) translateY(0);
    }
  }

  .warning-header {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 1.8rem;
    gap: 1rem;
  }

  .warning-icon {
    width: 60px;
    height: 60px;
    background: #ff4d4f;
    color: white;
    font-size: 2.8rem;
    font-weight: bold;
    line-height: 60px;
    border-radius: 50%;
    box-shadow: 0 0 30px rgba(255, 77, 79, 0.6);
    animation: pulse 2s infinite;
  }

  @keyframes pulse {
    0%, 100% {
      box-shadow: 0 0 20px rgba(255, 77, 79, 0.4);
    }
    50% {
      box-shadow: 0 0 40px rgba(255, 77, 79, 0.8);
    }
  }

  .warning-header h3 {
    color: #ff4d4f;
    font-size: 1.9rem;
    font-weight: 800;
    margin: 0;
    text-shadow: 0 2px 10px rgba(255, 77, 79, 0.5);
  }

  .spinner {
    width: 90px;
    height: 90px;
    border: 10px solid rgba(255, 77, 79, 0.2);
    border-top: 10px solid #ff4d4f;
    border-radius: 50%;
    animation: spin 1.3s linear infinite;
    margin: 0 auto 2rem;
  }

  @keyframes spin {
    to {
      transform: rotate(360deg);
    }
  }

  .loading-text {
    font-size: 1.65rem;
    font-weight: 700;
    color: #ffcccc;
    margin-bottom: 1.2rem;
    letter-spacing: 1px;
  }

  .hint {
    color: #ff9999;
    font-size: 1.1rem;
    line-height: 1.7;
    margin: 0;
    opacity: 0.95;
  }
}
</style>


