import service from "@utils/request.js";

async function restartService() {
    const response = await service.post('/jwt/application/restart')
    return response;
}

export {
    restartService
}