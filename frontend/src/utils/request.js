import axios from 'axios'
import {ElNotification, ElMessageBox, ElMessage, ElLoading} from 'element-plus'
import {getLocalToken, getLocalTokenName, removeLocalToken} from "@api/web/web.js";

let downloadLoadingInstance;
// 是否显示重新登录
// export let isRelogin = {show: false};

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'
// 创建axios实例
const service = axios.create({
    // axios中请求配置有baseURL选项，表示请求URL公共部分
    baseURL: import.meta.env.VITE_BASE_API_PATH || '/bgi/',
    // 超时
    timeout: 10000
})

// request拦截器
service.interceptors.request.use(async config => {
    const token_name = await getLocalTokenName()
    const token = await getLocalToken()
    // console.log('Token Name:', token_name);
    // console.log('Token Value:', token);

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
        config.headers[token_name] = token;
        // console.log('Headers Set:', config.headers);
    } else {
        console.warn('No token found in localStorage');
    }
    return config
}, error => {
    console.log(error)
    Promise.reject(error)
})

// 响应拦截器
service.interceptors.response.use(async res => {
        // 未设置状态码则默认成功状态
        const code = res?.data?.code || 200;
        // 获取错误信息
        const msg = res.data.message
        // 二进制数据则直接返回
        if (res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer') {
            return res.data
        }
        if (code === 401) {
            ElMessage({message: msg, type: 'error'})
            // const token_name = import.meta.env.VITE_BASE_TOKEN_NAME || 'bgi_tools_token'
            // localStorage.removeItem(token_name)
            await removeLocalToken()
            return Promise.reject(new Error(msg))
        } else if (code === 500) {
            ElMessage({message: msg, type: 'error'})
            return Promise.reject(new Error(msg))
        } else if (code === 601) {
            ElMessage({message: msg, type: 'warning'})
            return Promise.reject(new Error(msg))
        } else if (code !== 200) {
            ElNotification.error({title: msg})
            return Promise.reject('error')
        } else {
            // ElMessage({message: "请求成功", type: 'success'})
            return Promise.resolve(res.data)
        }
    },
    error => {
        console.log('err' + error)
        let {message} = error;
        if (message == "Network Error") {
            message = "后端接口连接异常";
        } else if (message.includes("timeout")) {
            message = "系统接口请求超时";
        } else if (message.includes("Request failed with status code")) {
            message = "系统接口" + message.substr(message.length - 3) + "异常";
        }
        ElMessage({message: message, type: 'error', duration: 5 * 1000})
        return Promise.reject(error)
    }
)

export default service
