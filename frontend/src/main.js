import {createApp} from 'vue'
import App from '@main/App'
import router from '@router/router'
import {initApiPrefix} from '@utils/http'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css';
import {zhCn} from "element-plus/es/locale/index";
import '@/assets/css/home.css';
// 初始化前端应用
async function bootstrap() {
    // await initApiPrefix() // 获取动态 API 前缀
    const app = createApp(App)

    // 等 Vue 挂载完再隐藏欢迎屏
    app.use(router)
        .use(ElementPlus, {locale: zhCn})
        .mount('#app')
}

bootstrap()
