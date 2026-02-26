import service from "@utils/request.js";

async function getTokenInfo() {
    const res = await service.get('/jwt/token/info')  // 注意 context-path 是 /bgi
    // console.log("res:", res)
    return res
}

async function updateToken(name,value) {
    const res = await service.post('/jwt/token/info',{tokenName:name,tokenValue:value})  // 注意 context-path 是 /bgi
    // console.log("res:", res)
    return res
}

export {
    getTokenInfo,
    updateToken
}