import service from "@utils/request.js";
import {ApiService} from "@utils/ApiRequest.js";

/**
 * 查询所有授权
 * @returns {Promise} 返回所有授权列表
 */
export async function getAccessPage(search={uid:undefined},page = {pageNumber: 1, pageSize: 99999}) {
    const {code, data} = await ApiService.get('/jwt/ws-proxy/access/page', {params: {...search, ...page}});
    return data;
}

/**
 * 查询单个授权
 * @param {string} uid - 用户唯一标识
 * @returns {Promise} 返回授权信息
 */
export async function getAccess(uid) {
    const {code, data} = await ApiService.get('/api/ws-proxy/access', {params: {uid: uid}});
    return data;
}

/**
 * 保存授权
 * @param {Object} wsProxyAccess - 授权对象
 * @returns {Promise} 返回操作结果
 */
export async function saveAccess(wsProxyAccess) {
    const {code, data} = await service.post('/jwt/ws-proxy/access', wsProxyAccess);
    return data;
}

/**
 * 删除授权
 * @param {string|string[]} uids - 用户唯一标识，支持单个或逗号分隔的多个值
 * @returns {Promise} 返回操作结果
 */
export async function deleteAccess(uids) {
    const {code, data} = await service.delete('/jwt/ws-proxy/access', {params: {uids: uids}});
    return data;
}
