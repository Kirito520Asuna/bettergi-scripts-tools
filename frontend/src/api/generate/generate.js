import {ApiService} from "@utils/ApiRequest.js";

export async function browseDir(path) {
    const {code, data} = await ApiService.get('/api/gen/browse', {params: {path: path}})
    return data
}

export async function preview1RemoteBat(title,startDir,exeName, startUlid, seconds, fileName) {
   const {code,data}=await ApiService.get('/api/gen/bat/1Remote/preview', {params: {title:title,startDir:startDir,exeName:exeName, startUlid:startUlid, seconds:seconds, fileName:fileName}})
   return data
}

