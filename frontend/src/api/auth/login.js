import service from "@utils/request.js";

/**
 * 登录函数
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @returns {Promise<Object>} 返回登录结果数据
 */
async function login(username, password) {
    const res = await service.post('/auth/login', {username: username, password: password})  // 注意 context-path 是 /bgi
    // console.log("res:", res)
    return res.data
}
/**
 * 更新用户信息的异步函数
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @returns {Promise} 返回请求的响应结果
 */
async function  updateUserInfo(username, password) {
    // 发送POST请求更新用户信息
    const res = await service.post('/auth/info', {username: username, password: password})
    // 返回响应结果
    return res
}
export {
    login,
    updateUserInfo
}