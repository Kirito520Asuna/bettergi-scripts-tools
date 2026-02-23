import service from "@utils/request.js";

async function restartService(ids=[]) {
    const response = await service.post('/jwt/application/restart',{
        ids:ids
    })
    return response;
}
async function getApplicationIds() {
    const response = await service.get('/jwt/application/applicationIds')
    return response;
}
export {
    restartService,
    getApplicationIds
}