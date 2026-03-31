import service from "@utils/request.js";
import {ApiService} from "@utils/ApiRequest.js";

/**
 * 交换密钥的异步函数
 * @param {Object} keyInfo - 包含密钥名称和值的对象
 * @param {string} keyInfo.name - 密钥名称
 * @param {string} keyInfo.value - 密钥值
 * @returns {Promise<any>} 返回响应数据，如果响应不存在则返回undefined
 */
export async function postKey(keyInfo) {
    const res = await ApiService.post('/key/exchangeKey', {}, {
        headers: {[keyInfo.name]: keyInfo.value},
        config: {
            noDoubleEncryption: true// 禁用双重加密
        }
    })
    return res?.data
}