<template>
  <div class="home">
    <div class="home-card">
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
                  :class="['feature-item', getItemClass(item)]"
              >
                <!--              <span class="icon">{{ getIcon(item) }}</span>-->
                <span v-html="getIcon(item)" class="icon"></span>
                <button class="name" @click="togo(item)">{{ item.name }}</button>
              </div>
            </div>

            <!-- 右侧功能列表 -->
            <div class="feature-column">
              <div
                  v-for="item in getItemsByPosition(group.children, 'right')"
                  :key="item.id"
                  :class="['feature-item', getItemClass(item)]"
              >
                <!--              <span class="icon">{{ getIcon(item) }}</span>-->
                <span v-html="getIcon(item)" class="icon"></span>
                <button class="name" @click="togo(item)">{{ item.name }}</button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <router-view/>
    </div>
  </div>
</template>

<script>
import {ref, onMounted} from "vue";
import router from "@router/router";

const iconAsMap = new Map();

iconAsMap.set('Markdown',
    (`<svg viewBox="0 0 32 32" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
<path fill="#444444" d="M25.674 9.221h-19.348c-0.899 0-1.63 0.731-1.63 1.63v10.869c0 0.899 0.731 1.63 1.63 1.63h19.348c0.899 0 1.63-0.731 1.63-1.63v-10.869c0-0.899-0.731-1.63-1.63-1.63zM17.413 20.522l-2.826 0.003v-4.239l-2.12 2.717-2.12-2.717v4.239h-2.826v-8.478h2.826l2.12 2.826 2.12-2.826 2.826-0.003v8.478zM21.632 21.229l-3.512-4.943h2.119v-4.239h2.826v4.239h2.119l-3.553 4.943z"></path>
</svg>`.trim())
);

const currentRoute = ref(router.currentRoute)
export default {
  name: 'HomeView',
  setup() {
    // 统一管理所有功能项
    const featureGroup = ref([]);
    const list = [
      // {isLink: true, name: 'API 调试链接', value: 'API 调试链接'},
      {isSwagger: true, name: 'Swagger 文档入口', value: 'doc.html'},
      // {isRote: true, name: '路由管理面板', value: '路由管理面板'},
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
        icon: undefined,
        name: item.name,
        value: item.value
      });
      index++
    })
    featureGroup.value.push(initJson);
    onMounted(() => {
      let index = 1
      let routerJson = {
        title: '基础路由功能列表',
        children: []
      }

      router.getRoutes().filter(route => route.name !== 'home' && route?.meta?.isRoot).forEach(route => {
        routerJson.children.push({
          id: index,
          position: index % 2 === 1 ? "left" : "right",
          isRote: true,
          icon: route?.meta?.icon,
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
          icon: route?.meta?.icon,
          name: route?.meta?.title,
          value: route.path
        });
        index++
      });
      featureGroup.value.push(homeJson);

    });

    // 获取图标
    const getIcon = (item) => {
      // 优先使用 meta.icon，没有则根据类型给默认 emoji
      let rawIcon = item?.icon;
      if (rawIcon) {
        // 字符串处理
        if (typeof rawIcon === "string") {
          const trimmed = rawIcon.trim();

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
      rawIcon = item.isLink ? "🔗" : item.isSwagger ? "📖" : item.isRote ? "🛤️" : "";
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
        const basePath = import.meta.env.VITE_BASE_PATH || '/bgi/';
        window.open(`${basePath}${item.value}`, '_blank');
      } else if (item?.isLink) {
        window.open(item.value, '_blank');
      }
    };

    return {
      currentRoute,
      featureGroup,
      togo,
      getIcon,
      getItemClass,
      getItemsByPosition
      // goFeature1,
      // goFeature2
    };
  }
};

</script>
<style scoped>
.home {
  min-height: 100vh;
  /*  padding: 20px;*/
  /*margin: 0 auto;*/
  background: url("@assets/MHY_XTLL.png");
  /* 关键：固定背景，不随滚动重复或变形 */
  background-attachment: fixed; /* ← 核心属性 */
  background-size: cover; /* 覆盖整个容器 */
  background-position: center;
}

/* 页面全屏背景 */
.home-card {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #a1c4fd, #c2e9fb);
  background-size: cover;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

/* 中间卡片 */
.welcome-card {
  background: rgba(255, 255, 255, 0.95);
  padding: 50px 70px;
  border-radius: 25px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  text-align: center;
  max-width: 600px;
  width: 100%;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.welcome-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

/* Logo 圆角 */
.logo {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 50%;
  margin-bottom: 25px;
  border: 3px solid #6a89cc;
}

/* 主标题美化 */
.title {
  font-size: 36px;
  font-weight: 800;
  margin-bottom: 15px;
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
  font-size: 20px;
  color: #7f8c8d;
  margin-bottom: 40px;
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
  margin-top: 30px;
}

/* 美化 section-title */
.section-title {
  font-size: 28px;
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
  gap: 30px;
}

.feature-column {
  display: flex;
  flex-direction: column;
  gap: 15px;
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

}
</style>


