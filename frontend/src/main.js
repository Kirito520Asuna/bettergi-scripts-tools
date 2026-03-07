import {createApp} from 'vue'
import App from '@main/App'
import router from '@router/router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css';
import {zhCn} from "element-plus/es/locale/index";
import '@/assets/css/home.css';
import {getVersion} from "@api/sys/sys.js";
import {getLocalVersion, setLocalVersion} from "@api/web/web.js";

// 动态设置 favicon（使用 Vite 的 BASE_URL）
function setupFavicon() {
    try {
        const link = document.querySelector("link[rel~='icon']");
        if (link && import.meta.env.BASE_URL) {
            const baseUrl = import.meta.env.BASE_URL.endsWith('/')
                ? import.meta.env.BASE_URL
                : import.meta.env.BASE_URL + '/';
            link.href = `${baseUrl}favicon.ico`;
            console.log('[Favicon] Set to:', link.href);
        }
    } catch (error) {
        console.error('[Favicon] Setup failed:', error);
    }
}

async function updateTitleWithVersion() {
    let version = await getLocalVersion();

    if (!version) {
        try {
            const response = await getVersion();
            version = response?.data || response?.result || response;
        } catch (e) {
        }
    }

    if (version) {
        await setLocalVersion(version);
        document.title = `BetterGI 工具集 [v${version}]`;
    }
}


// 先尝试获取版本号并更新 title（不阻塞应用启动）
updateTitleWithVersion();
setupFavicon();
const app = createApp(App)

// 等 Vue 挂载完再隐藏欢迎屏
app.use(router)
    .use(ElementPlus, {locale: zhCn})
    .mount('#app')
