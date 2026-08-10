import service from "@utils/request.js";
import {ApiService} from "@utils/ApiRequest.js";

async function restartService(ids = []) {
    const re = await service.post('/jwt/application/restart', {
        ids: ids
    }, { silentError: true })
    return re;
}

async function getApplicationIds() {
    const re = await service.get('/jwt/application/applicationIds',{
        silentError: true
    })
    return re;
}

async function getVersion() {
    const {code,data} = await service.get('/context/bgi-tools/version')
    return data
}

async function getSystemInfo(ids="") {
    const {code,data} = await service.get('/jwt/application/sys/info',{params:{ids:ids}})
    return data
}
export async function getGithubTagLatest() {
    const {code,data} = await service.get('/jwt/application/github/tag/latest')
    return data
}
export async function getGithub1RemoteTags() {
    const {code,data} = await ApiService.get('/api/application/github/1Remote/tags')
    return data
}
export {
    restartService,
    getApplicationIds,
    getVersion, getSystemInfo
}