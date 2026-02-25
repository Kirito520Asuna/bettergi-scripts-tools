import {ElMessage, ElMessageBox} from "element-plus";
import {getApplicationIds, restartService} from "@api/sys/sys.js";

/**
 * 重启应用程序的异步函数
 * @param {boolean} restartClick - 防止重复点击的标志
 * @param {Array} applicationIds - 需要重启的应用程序ID列表
 * @param {number} restartTimeout - 重启超时时间，默认为5分钟（毫秒）
 * @returns {Promise<boolean>} 返回重启操作后的状态
 */
async function restart(restartClickRef, applicationIds, restartTimeout = 5 * 60 * 1000) {
// 可選：二次確認（看需求加不加）
    await ElMessageBox.confirm('确定要重启系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    })
    if ((!applicationIds)||applicationIds?.length===0){
        try {
            const applicationIds1 = await getApplicationIds();
            if (applicationIds1.data)
                applicationIds = applicationIds1.data;
        }catch (error) {
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

        ElMessage.success('重启成功');
    } catch (error) {
        // 捕获异常并提示用户
        console.error('重启请求失败:', error);
        ElMessage.error('重启请求异常，请稍后再试');
    } finally {
        // 无论成功与否，都恢复状态
        restartClickRef.value = false;
    }

}

export {
    restart
}