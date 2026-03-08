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
onMounted(async () => {
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
    await ElMessageBox.confirm(`确定要访问外链接:${item.value}吗？`, '提示', {
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
:root {
  --home-bg-light: url("@assets/MHY_XTLL.png");
  --card-bg-light: rgba(255, 255, 255, 0.95);
  --card-bg-dark: rgba(30, 30, 46, 0.85);
  --card-border-light: rgba(255, 255, 255, 0.25);
  --card-border-dark: rgba(255, 255, 255, 0.1);
  --title-gradient-light: linear-gradient(90deg, #6a89cc, #3498db);
  --title-gradient-dark: linear-gradient(90deg, #74b9ff, #0984e3);
  --text-primary-light: #2c3e50;
  --text-primary-dark: #dfe6e9;
  --text-secondary-light: #7f8c8d;
  --text-secondary-dark: #b2bec3;
  --feature-item-light: #ffffff;
  --feature-item-dark: rgba(50, 50, 70, 0.6);
  --shadow-light: rgba(0, 255, 246, 0.2);
  --shadow-dark: rgba(0, 0, 0, 0.35);
}

.home {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--home-bg-light);
  background-attachment: fixed;
  background-size: cover;
  background-position: center;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .home {
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  }
}

.welcome-card {
  background: var(--card-bg-light);
  padding: 50px 50px;
  border-radius: 25px;
  box-shadow: 0 15px 35px var(--shadow-light);
  text-align: center;
  max-width: 600px;
  width: 80%;
  transition: all 0.3s ease;
  border: 1px solid var(--card-border-light);
}

@media (prefers-color-scheme: dark) {
  .welcome-card {
    background: var(--card-bg-dark);
    box-shadow: 0 15px 35px var(--shadow-dark);
    border-color: var(--card-border-dark);
  }
}

.welcome-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(255, 0, 181, 0.3);
}

@media (prefers-color-scheme: dark) {
  .welcome-card:hover {
    box-shadow: 0 20px 40px rgba(116, 185, 255, 0.3);
  }
}

.logo {
  object-fit: cover;
  border-radius: 50%;
  margin-bottom: 25px;
  border: 3px solid #6a89cc;
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .logo {
    border-color: #74b9ff;
  }
}

.title {
  color: transparent;
  background: var(--title-gradient-light);
  -webkit-background-clip: text;
  background-clip: text;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

@media (prefers-color-scheme: dark) {
  .title {
    background: var(--title-gradient-dark);
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  }
}

.title:hover {
  transform: scale(1.05);
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

@media (prefers-color-scheme: dark) {
  .title:hover {
    text-shadow: 0 4px 8px rgba(0, 0, 0, 0.4);
  }
}

.subtitle {
  color: var(--text-secondary-light);
  opacity: 0;
  animation: fadeIn 1s ease-in-out forwards;
  font-style: italic;
}

@media (prefers-color-scheme: dark) {
  .subtitle {
    color: var(--text-secondary-dark);
  }
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

.feature-section {
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 20px;
  color: transparent;
  background: var(--title-gradient-light);
  -webkit-background-clip: text;
  background-clip: text;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  text-align: center;
}

@media (prefers-color-scheme: dark) {
  .section-title {
    background: var(--title-gradient-dark);
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  }
}

.section-title:hover {
  transform: scale(1.05);
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

@media (prefers-color-scheme: dark) {
  .section-title:hover {
    text-shadow: 0 4px 8px rgba(0, 0, 0, 0.4);
  }
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
  background: var(--feature-item-light);
  border-radius: 12px;
  padding: 15px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

@media (prefers-color-scheme: dark) {
  .feature-item {
    background: var(--feature-item-dark);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
    border-color: rgba(255, 255, 255, 0.1);
  }
}

.feature-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

@media (prefers-color-scheme: dark) {
  .feature-item:hover {
    box-shadow: 0 8px 20px rgba(116, 185, 255, 0.25);
  }
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
  fill: currentColor;
}

.name {
  border: none;
  background: transparent;
  font-size: 16px;
  color: #3498db;
  cursor: pointer;
  font-weight: 500;
}

@media (prefers-color-scheme: dark) {
  .name {
    color: #74b9ff;
  }
}

.link-item {
  background: #e8f8f5;
  color: #27ae60;
}

@media (prefers-color-scheme: dark) {
  .link-item {
    background: rgba(39, 174, 96, 0.2);
    color: #58d68d;
  }
}

.swagger-item {
  background: #fef9e7;
  color: #f39c12;
}

@media (prefers-color-scheme: dark) {
  .swagger-item {
    background: rgba(243, 156, 18, 0.2);
    color: #f5b041;
  }
}

.routes-item {
  background: #fadbd8;
  color: #e74c3c;
}

@media (prefers-color-scheme: dark) {
  .routes-item {
    background: rgba(231, 76, 60, 0.2);
    color: #ec7063;
  }
}

.restart-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.75);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  user-select: none;
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

@media (prefers-color-scheme: dark) {
  .restart-modal {
    background: rgba(30, 30, 46, 0.95);
    border: 1px solid rgba(255, 255, 255, 0.1);
  }
}

/* 手机端适配 */
@media (max-width: 768px) {
  .home {
    background-position: center top;
  }

  .welcome-card {
    width: 90%;
    padding: 30px 25px;
    margin: 20px 15px;
  }

  .logo {
    width: 60px;
    height: 60px;
    margin-bottom: 15px;
  }

  .title {
    font-size: 28px;
  }

  .subtitle {
    font-size: 16px;
    margin-bottom: 25px;
  }

  .section-title {
    font-size: 18px;
    margin-bottom: 15px;
  }

  .feature-container {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .feature-column {
    gap: 8px;
  }

  .feature-item {
    padding: 12px 18px;
    border-radius: 10px;
  }

  .icon {
    width: 1em;
    height: 1em;
    margin-right: 10px;
  }

  .name {
    font-size: 15px;
  }

  .restart-overlay {
    padding: 15px;
  }

  .restart-modal {
    width: 100%;
    max-width: 380px;
    padding: 2rem;
  }
}

@media (max-width: 480px) {
  .welcome-card {
    width: 95%;
    padding: 25px 20px;
    border-radius: 20px;
  }

  .logo {
    width: 50px;
    height: 50px;
    border-width: 2px;
  }

  .title {
    font-size: 24px;
  }

  .subtitle {
    font-size: 14px;
  }

  .section-title {
    font-size: 16px;
  }

  .feature-item {
    padding: 10px 15px;
  }

  .name {
    font-size: 14px;
  }

  .restart-modal {
    padding: 1.5rem;
    min-width: 280px;
  }

  .warning-icon {
    width: 50px;
    height: 50px;
    font-size: 2.2rem;
    line-height: 50px;
  }

  .spinner {
    width: 70px;
    height: 70px;
    border-width: 8px;
  }

  .loading-text {
    font-size: 1.4rem;
  }

  .hint {
    font-size: 0.95rem;
  }
}

/* 横屏手机适配 */
@media (max-width: 768px) and (orientation: landscape) {
  .welcome-card {
    max-height: 90vh;
    overflow-y: auto;
    padding: 20px 30px;
  }

  .logo {
    width: 50px;
    height: 50px;
    margin-bottom: 10px;
  }

  .title {
    font-size: 24px;
  }

  .subtitle {
    font-size: 14px;
    margin-bottom: 15px;
  }

  .feature-container {
    gap: 6px;
  }

  .feature-item {
    padding: 10px 15px;
  }
}
</style>



