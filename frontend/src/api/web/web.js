import {ElMessage, ElMessageBox} from "element-plus";
import {getApplicationIds, getSystemInfo, restartService} from "@api/sys/sys.js";
import router from "@router/router.js";

/**
 * 重启应用程序的异步函数
 * @param {boolean} restartClick - 防止重复点击的标志
 * @param {Array} applicationIds - 需要重启的应用程序ID列表
 * @param {number} restartTimeout - 重启超时时间，默认为3分钟（毫秒）
 */
export async function restart(restartClickRef, applicationIds, restartTimeout = 3 * 60 * 1000) {
// 可選：二次確認（看需求加不加）
    await ElMessageBox.confirm('确定要重启系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    })
    if ((!applicationIds) || applicationIds?.length === 0) {
        try {
            const applicationIds1 = await getApplicationIds();
            if (applicationIds1.data)
                applicationIds = applicationIds1.data;
        } catch (error) {
        }
    }
    // 防止重复点击
    if (restartClickRef.value) {
        ElMessage.warning('正在重启中，请勿重复点击');
        return;
    }
    restartClickRef.value = true
    try {
        // 设置超时时间（毫秒）
        // const restartTimeout = 5*2*30 * 1000;
        const startTime = Date.now();
        const list = applicationIds;
        // console.log("ids:",JSON.stringify(list))
        let ids = [...list]
        // console.log("ids:",JSON.stringify(ids))
        while (ids.length > 0) {
            // 检查是否超时
            const currentTime = Date.now();
            const elapsedTime = currentTime - startTime;

            if (elapsedTime > restartTimeout) {
                ElMessage.error(`重启超时（超过${restartTimeout / 1000}秒），强制退出`);
                throw new Error(`Restart timeout after ${restartTimeout / 1000} seconds`);
            }
            //分布式重启
            // 发送重启指令
            const result = await restartService(ids);
            if (result.code === 200) {
                ids = ids.filter(id => id !== result.data)
                // ElMessage.info('重启指令发送成功');
            } else {
                // ElMessage.error('重启指令发送失败');
            }
        }
        //等10s
        await new Promise(resolve => setTimeout(resolve, 10 * 1000));
        //等待重启完成

        let restartApplicationIds = []
        let restartIds = [...list]
        while (restartIds.length !== restartApplicationIds.length) {
            // 检查是否超时
            const currentTime = Date.now();
            const elapsedTime = currentTime - startTime;

            if (elapsedTime > restartTimeout) {
                ElMessage.error(`重启超时（超过${restartTimeout / 1000}秒），强制退出`);
                throw new Error(`Restart timeout after ${restartTimeout / 1000} seconds`);
            }
            try {
                const applicationIds1 = await getApplicationIds();
                if (applicationIds1.data)
                    restartApplicationIds = applicationIds1.data;

            } catch (e) {
                //忽略异常，继续执行
            }
        }
        ElMessage.success('重启成功');
    } catch (error) {
        if (!error.toString().includes("后端接口连接异常")) {
            console.error('重启请求失败:', error);
            ElMessage.error('重启请求异常，请稍后再试');
        }
    } finally {
        // 无论成功与否，都恢复状态
        restartClickRef.value = false;
    }

}

/**
 * 获取所有系统信息
 * @param {Array} applicationIds - 应用程序ID列表
 * @param {Number} restartTimeout - 重启超时时间（毫秒），默认为3分钟
 * @returns {Array} 返回系统信息列表
 */
export async function getAllSystemInfo(applicationIds, restartTimeout = 3 * 60 * 1000) {
    let systemInfoList = []; // 存储系统信息的列表
    // 如果没有提供applicationIds或applicationIds为空数组，则获取所有应用程序ID
    if ((!applicationIds) || applicationIds?.length === 0) {
        try {
            const applicationIds1 = await getApplicationIds(); // 获取应用程序ID
            if (applicationIds1.data)
                applicationIds = applicationIds1.data; // 更新applicationIds
        } catch (error) {
            // 错误处理
        }
    }
    const list = applicationIds; // 获取应用程序ID列表

    let ids = [...list]; // 复制应用程序ID列表
    const startTime = Date.now(); // 记录开始时间
    // 循环获取系统信息，直到所有应用程序ID都被处理
    while (ids.length > 0) {
        // 检查是否超时
        const currentTime = Date.now(); // 获取当前时间
        const elapsedTime = currentTime - startTime; // 计算经过的时间

        // 如果超过超时时间，抛出错误
        if (elapsedTime > restartTimeout) {
            ElMessage.error(`重启超时（超过${restartTimeout / 1000}秒），强制退出`);
            throw new Error(`Restart timeout after ${restartTimeout / 1000} seconds`);
        }
        const key = ids.join(",")
        const systemInfo = await getSystemInfo(key); // 获取系统信息
        if (systemInfo) {
            ids = ids.filter(id => id !== systemInfo.applicationId)
            systemInfoList.push(systemInfo); // 添加系统信息到列表
        }
    }

    return systemInfoList; // 返回系统信息列表
}


/**
 * 前往主页的异步函数
 * 使用ElMessageBox显示确认对话框，用户确认后跳转到主页
 */
export async function toHomePage(confirm = true) {
    // 使用Element Plus的MessageBox显示确认对话框
    // 包含确认、取消按钮和警告类型图标
    if (confirm) {
        await ElMessageBox.confirm('确定前往主页吗？', '提示', {
            confirmButtonText: '确定',    // 确认按钮文本
            cancelButtonText: '取消',    // 取消按钮文本
            type: 'warning'             // 提示类型为警告
        })
    }

    // 用户确认后，使用router进行页面导航到主页路径'/'
    router.push('/'); // 假设主页路径是 '/'
}

/**
 * 返回上一页
 */
export async function goBack(confirm = true) {
    if (confirm) {
        await ElMessageBox.confirm('确定返回上级目录吗？', '提示', {
            confirmButtonText: '确定',    // 确认按钮文本
            cancelButtonText: '取消',    // 取消按钮文本
            type: 'warning'             // 提示类型为警告
        })
    }
    router.go(-1);
}

/**
 * 设置本地存储的令牌
 * @param {string} token - 需要存储的令牌值
 */
export async function setLocalToken(token) {
    // 如果没有提供token值，则直接返回
    if (!token) {
        return
    }
    // 获取令牌名称，使用环境变量中的VITE_BASE_TOKEN_NAME，如果不存在则使用默认值'bgi_tools_token'
    const token_name = await getLocalTokenName()// 从环境变量获取令牌名称，如果不存在则使用默认名称'bgi_tools_token'
    // 将令牌存储到localStorage中
    localStorage.setItem(token_name, token)
}

/**
 * 移除本地存储的认证令牌
 * 该函数会根据环境变量或默认名称移除localStorage中的令牌
 */
export async function removeLocalToken() {
    const token_name = await getLocalTokenName()// 从环境变量获取令牌名称，如果不存在则使用默认名称'bgi_tools_token'// 从环境变量获取令牌名称，如果不存在则使用默认名称'bgi_tools_token'
    localStorage.removeItem(token_name) // 从localStorage中移除指定名称的令牌
}

export async function removeLocalVersion() {
    const versionName = await getLocalVersionName();
    localStorage.removeItem(versionName)
}

export async function setLocalVersion(version) {
    const versionName = await getLocalVersionName();
    localStorage.setItem(versionName, version)
}

export async function getLocalVersion() {
    const versionName = await getLocalVersionName();
    return localStorage.getItem(versionName)
}

export async function getLocalVersionName() {
    return "bgi-tools-version"
}


/**
 * 从localStorage获取本地存储的令牌
 * @returns {string|null} 返回存储的令牌值，如果不存在则返回null
 */
export async function getLocalToken() {
    const token_name = await getLocalTokenName()// 从环境变量获取令牌名称，如果不存在则使用默认名称'bgi_tools_token'
    return localStorage.getItem(token_name)
}

/**
 * 获取本地存储的令牌名称
 * 该函数会尝试从环境变量中获取令牌名称，如果环境变量不存在，则使用默认名称
 * @returns {Promise<string>} 返回令牌名称的Promise对象
 */
export async function getLocalTokenName() {
    const token_name = import.meta.env.VITE_BASE_TOKEN_NAME || 'bgi_tools_token' // 从环境变量获取令牌名称，如果不存在则使用默认名称'bgi_tools_token'
    return token_name
}

export async function uploadFile(file){
    return {
        name: file.name,
        size: file.size,
        type: file.type,
        bytes: await file.arrayBuffer()
    }
}