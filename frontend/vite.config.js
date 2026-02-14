// vite.config.js
import {defineConfig, loadEnv} from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'


export default defineConfig(({mode}) => {
    // 加载对应模式下的 .env 文件
    const env = loadEnv(mode, process.cwd());
    // 获取端口号，默认为 3000
    let SERVER_PORT = env.VITE_SERVER_PORT || 5137;
    let VITE_BASE_PATH = (env.VITE_BASE_PATH || '/bgi/ui/');
    console.log("mode:", mode);  //
    return {
        base: VITE_BASE_PATH,
        // base: '/ui/',
        resolve: {
            alias: {
                '@views': path.resolve(__dirname, 'src/views'), // 添加 @views 别名指向 views 目录
                '@style': path.resolve(__dirname, 'src/assets/style'), // 添加 @views 别名指向 views 目录
                '@css': path.resolve(__dirname, 'src/assets/style/css'), // 添加 @css 别名指向 css 目录
                '@scss': path.resolve(__dirname, 'src/assets/style/scss'), // 添加 @scss 别名指向 css/scss 目录
                '@assets': path.resolve(__dirname, 'src/assets'), // 添加 @assets 别名指向 assets 目录
                '@api': path.resolve(__dirname, 'src/api'), // 添加 @api 别名指向 api 目录
                '@utils': path.resolve(__dirname, 'src/utils'), // 添加 @util 别名指向 util 目录
                '@router': path.resolve(__dirname, 'src/router'), // 添加 @router 别名指向 router 目录
                '@store': path.resolve(__dirname, 'src/store'), // 添加 @store 别名指向 store 目录
                '@plugins': path.resolve(__dirname, 'src/plugins'), // 添加 @plugins 别名指向 plugins 目录
                '@main': path.resolve(__dirname, 'src'), // 添加 @main 别名指向 src 目录
                '@': path.resolve(__dirname, 'src/'), // 假设 src/ 目录位于项目根目录下
            },
            extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue'],
        },
        plugins: [
            vue(),
            ],
        // vite 相关配置
        server: {
            //port: 80：服务器监听端口为80。
            // host: true：允许所有IP地址访问服务器。
            // open: true：启动服务器时自动打开浏览器。
            // proxy: {...}：设置代理规则，将请求路径以/dev-api开头的请求代理到http://localhost:8080，并移除/dev-api前缀。

            // 修改这里的端口号
            port: SERVER_PORT,
            // 其他的服务器配置...
            host: true,
            open: true,
            // proxy: {
            //     // https://cn.vitejs.dev/config/#server-proxy
            //     '/dev-api': {
            //         target: 'http://localhost:8080',
            //         changeOrigin: true,
            //         rewrite: (p) => p.replace(/^\/dev-api/, '')
            //     }
            // }
        },
        define: {
            'process.env': process.env,  // 这一步确保 process.env 在客户端代码中可访问
        },
    }
});