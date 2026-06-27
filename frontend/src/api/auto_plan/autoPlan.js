import service from "@utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";

/**
 * 异步发送UID和JSON数据到服务器
 * @param {string|number} uid - 用户ID
 * @param {object} json - 要发送的JSON数据对象
 * @returns {Promise<object>} 返回服务器响应的数据
 */
// async function postUidJson(uid, json) {
//     // 构建请求负载对象
//     const payload = {
//         uid: uid,  // 用户ID
//         json: json  // JSON数据
//     };
//     // 发送POST请求到指定端点
//     const response = await service.post("/auto/plan/json", payload);
//     if (code === 200){
//         ElMessage.success("保存成功");
//     }
//     // 返回响应数据
//     return data;
// }

/**
 *
 * @param uid
 * @param autoPlanList
 * @returns {Promise<any>}
 */
async function postUidPlan(uid, autoPlanList = []) {
    // 构建请求负载对象
    const payload = {
        uid: uid,  // 用户ID
        autoPlanList: autoPlanList //体力计划
    };
    // 发送POST请求到指定端点
    const {code, data} = await service.post("/jwt/auto/plan/info", payload);
    if (code === 200) {
        ElMessage.success("保存成功");
    }
    // 返回响应数据
    return data;
}

/**
 * 异步获取指定UID的JSON数据
 * @param {string|number} uid - 用户唯一标识符
 * @returns {Promise<Object>} 返回包含响应数据的Promise对象
 */
async function getUidJson(uid,order=true) {
    const {code, data} = await service.get('/jwt/auto/plan/json', {params: {uid: uid,order: order}})
    if (code === 200) {
        ElMessage.success("加载成功");
    }
    // 返回响应数据
    return data;
}

/**
 * 获取所有用户ID的异步函数
 * @returns {Promise} 返回包含所有用户ID的数据
 */
async function getAllUid() {
    const {code, data} = await service.get('/jwt/auto/plan/uid/all/mapping')  // 发送GET请求获取所有用户ID
    // if (code === 200){  // 检查响应状态码是否为200
    //     ElMessage.success("加载成功");  // 如果成功，显示成功消息
    // }
    // 返回响应数据
    return data;
}

/**
 * 异步删除指定UID的JSON数据
 * @param uidStr
 * @returns {Promise<any>}
 */
async function removeUidList(uidStr) {
    const {code, data}  = await service.delete('/jwt/auto/plan/json', {params: {uidStr: uidStr}});
    if (code === 200) {
        ElMessage.success("删除成功");
    }
    return data;
}

/**
 * 异步获取所有JSON数据
 * @returns {Promise<Object>} 获取所有JSON数据
 */
async function getBaseJsonAll() {
    const {code, data} = await service.get('/jwt/auto/plan/domain/json/all');
    if (code === 200) {
        ElMessage.success("全部加载成功");
    }
    return data;
}

/**
 * 异步获取所有JSON数据
 * @returns {Promise<Object>} 获取所有JSON数据
 */
export async function getBaseBossListJsonAll() {
    const {code, data} = await service.get('/jwt/auto/plan/boss/json/all');
    if (code === 200) {
        ElMessage.success("全部加载成功");
    }
    return data;
}
/**
 *
 * @returns {Promise<any>}
 */
async function getBaseCountryJsonAll() {
    const {code, data} = await service.get('/jwt/auto/plan/country/json/all');
    if (code === 200) {
        ElMessage.success("全部加载成功");
    }
    return data;
}

/**
 * 保存全部 JSON 数据
 * @param list
 * @returns {Promise<any>}
 */
export async function saveBaseJsonAll(list) {
    const source = "WEB_API"
    // const source = "JS_API"
    const {code, data} = await service.post('/jwt/auto/plan/domain/json/all', {json: JSON.stringify(list), source: source});
    if (code === 200) {
        ElMessage.success("保存成功");
    }
    return data;
}
/**
 * 保存全部 JSON 数据
 * @param list
 * @returns {Promise<any>}
 */
export async function saveBaseBossJsonAll(list) {
    const source = "WEB_API"
    // const source = "JS_API"
    const {code, data} = await service.post('/jwt/auto/plan/boss/json/all', {json: JSON.stringify(list), source: source});
    if (code === 200) {
        ElMessage.success("保存成功");
    }
    return data;
}
/**
 * 保存全部国家 JSON 数据
 * @param list
 * @returns {Promise<any>}
 */
export async function saveBaseCountryJsonAll(list) {
    const source = "WEB_API"
    const {code, data} = await service.post('/jwt/auto/plan/country/json/all', {json: JSON.stringify(list), source: source});
    if (code === 200) {
        ElMessage.success("保存成功");
    }
    return data;
}

export {
    // postUidJson,
    postUidPlan,
    getUidJson,
    removeUidList,
    getBaseJsonAll,
    getBaseCountryJsonAll,
    getAllUid
}