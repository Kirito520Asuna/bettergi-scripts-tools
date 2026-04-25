import service from '@utils/request.js'
import {ElMessage} from "element-plus";

/**
 * 查询全部 uid 映射
 */
export async function getAllUid() {
    const response = await service.get('/jwt/uid/all')
    if (response.code === 200) {
        ElMessage.success("加载成功");
    }
    return response.data;
}

/**
 * 查询 uid 映射
 * @param {string} uid
 */
export async function getUid(uid) {
    const response = await service.get('/jwt/uid/info', {params: {uid: uid}})
    if (response.code === 200) {
        ElMessage.success("加载成功");
    }
    return response.data;
}

/**
 * 新增 uid 映射
 * @param {Object} uidInfo
 */
export async function saveUid(uidInfo) {
    const response = await service.post('/jwt/uid/info', uidInfo)
    if (response.code === 200) {
        ElMessage.success("保存成功");
    }
    return response.data;
}

/**
 * 移除 uid 映射
 * @param {string|array} ids - uid 字符串或数组
 */
export async function removeUidList(ids) {
    const idStr = Array.isArray(ids) ? ids.join(',') : ids
    const response = service.delete('/jwt/uid/info', {
        params: {ids: idStr}
    })
    if (response.code === 200) {
        ElMessage.success("移除成功");
    }
    return response.data;
}
