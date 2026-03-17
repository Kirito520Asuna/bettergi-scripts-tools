import service from "@utils/request.js";

async function restartService(ids = []) {
    const response = await service.post('/jwt/application/restart', {
        ids: ids
    })
    return response;
}

async function getApplicationIds() {
    const response = await service.get('/jwt/application/applicationIds')
    return response;
}

async function getVersion() {
    const response = await service.get('/context/bgi-tools/version').then(response => response)
    return response.data
}

async function getSystemInfo(ids="") {
    const response = await service.get('/jwt/application/sys/info',{params:{ids:ids}})
    return response.data
}

export {
    restartService,
    getApplicationIds,
    getVersion, getSystemInfo
}