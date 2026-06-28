import service from "@utils/request.js";

/**
 * 查询所有授权
 * @returns {Promise} 返回所有授权列表
 */
export async function getAccessAll() {
    const {code,data} = await service.get('/jwt/ws-proxy/access/all');
    return data;
}

/**
 * 查询单个授权
 * @param {string} uid - 用户唯一标识
 * @returns {Promise} 返回授权信息
 */
export async function getAccess(uid) {
    const {code,data} = await service.get('/jwt/ws-proxy/access', { params: { uid:uid } });
    return data;
}

/**
 * 保存授权
 * @param {Object} wsProxyAccess - 授权对象
 * @returns {Promise} 返回操作结果
 */
export async function saveAccess(wsProxyAccess) {
    const {code,data} = await service.post('/jwt/ws-proxy/access', wsProxyAccess);
    return data;
}

/**
 * 删除授权
 * @param {string|string[]} uids - 用户唯一标识，支持单个或逗号分隔的多个值
 * @returns {Promise} 返回操作结果
 */
export async function deleteAccess(uids) {
    const {code,data} = await service.delete('/jwt/ws-proxy/access', { params: { uids:uids } });
    return data;
}
