import service from "@utils/request.js";

/**
 * 获取下一个符合cron表达式的时间戳
 * @param {string} cronExpression - cron表达式，用于定义时间规则
 * @param {number} startTimestamp - 开始时间戳，计算下一个时间戳的起始点
 * @param {number} endTimestamp - 结束时间戳，计算下一个时间戳的截止点
 * @returns {Promise<any>} 返回服务器响应的数据，包含下一个符合条件的时间戳
 */
async function getNextTimestamp(cronExpression, startTimestamp, endTimestamp){
    // 发送POST请求到服务端，获取下一个符合cron表达式的时间戳
    const response = await service.post("/cron/next-timestamp", {
        // 传入cron表达式
        cronExpression: cronExpression,
        // 传入开始时间戳
        startTimestamp: startTimestamp,
        // 传入结束时间戳
        endTimestamp: endTimestamp,
    })

    // 返回响应数据
    return response.data;
}
/**
 * 获取所有cron表达式的下一个执行时间戳
 * @param {Array} cronList - cron表达式数组，默认为空数组
 * @returns {Promise} 返回一个Promise对象，解析后的数据为所有cron表达式的下一个执行时间戳
 */
async function getNextTimestampAll(cronList=[]){ // 定义异步函数，获取所有cron表达式的下一个执行时间
    const response = await service.post('/cron/next-timestamp/all', { // 发送POST请求获取下一个时间戳
        cronList: cronList, // 传入cron表达式列表
    });
    // 返回响应数据
    return response.data; // 返回服务器响应中的data部分
}


export {
    getNextTimestamp,
    getNextTimestampAll,
}