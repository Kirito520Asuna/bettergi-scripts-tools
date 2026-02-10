import service from "@utils/request.js";

/**
 * 登录函数
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @returns {Promise<Object>} 返回登录结果数据
 */
async function login(username, password) {
    const res = await service.post('/auth/login', {username: username, password: password})  // 注意 context-path 是 /bgi
    return res.data
}

export {
    login
}