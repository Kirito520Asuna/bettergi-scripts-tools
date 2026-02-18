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
//     if (response.code === 200){
//         ElMessage.success("保存成功");
//     }
//     // 返回响应数据
//     return response.data;
// }

/**
 *
 * @param uid
 * @param autoPlanList
 * @returns {Promise<any>}
 */
async function postUidPlan(uid, autoPlanList=[]) {
    // 构建请求负载对象
    const payload = {
        uid: uid,  // 用户ID
        autoPlanList: autoPlanList //体力计划
    };
    // 发送POST请求到指定端点
    const response = await service.post("/auto/plan/info", payload);
    if (response.code === 200){
        ElMessage.success("保存成功");
    }
    // 返回响应数据
    return response.data;
}
/**
 * 异步获取指定UID的JSON数据
 * @param {string|number} uid - 用户唯一标识符
 * @returns {Promise<Object>} 返回包含响应数据的Promise对象
 */
async function getUidJson(uid){
    const response = await service.get('/auto/plan/json', {params: {uid: uid}})
    if (response.code === 200){
        ElMessage.success("加载成功");
    }
    // 返回响应数据
    return response.data;
}

/**
 * 异步删除指定UID的JSON数据
 * @param uidStr
 * @returns {Promise<any>}
 */
async function removeUidList(uidStr){
    const response = await service.delete('/auto/plan/json',{params: {uidStr: uidStr}});
    if (response.code === 200){
        ElMessage.success("删除成功");
    }
    return response.data;
}

/**
 * 异步获取所有JSON数据
 * @returns {Promise<Object>} 获取所有JSON数据
 */
async function getBaseJsonAll(){
    const response = await service.get('/auto/plan/domain/json/all');
    if (response.code === 200){
        ElMessage.success("全部加载成功");
    }
    return response.data;
}

/**
 *
 * @returns {Promise<any>}
 */
async function getBaseCountryJsonAll(){
    const response = await service.get('/auto/plan/country/json/all');
    if (response.code === 200){
        ElMessage.success("全部加载成功");
    }
    return response.data;
}
export {
    // postUidJson,
    postUidPlan,
    getUidJson,
    removeUidList,
    getBaseJsonAll,
    getBaseCountryJsonAll,
}