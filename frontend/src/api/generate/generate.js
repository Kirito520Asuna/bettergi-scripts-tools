import service from "@utils/request.js";

export async function browseDir(path) {
    const {code, data} = await service.get('/gen/browse', {params: {path: path}})
    return data
}

export async function preview1RemoteBat(title,startDir,exeName, startUlid, seconds, fileName) {
   const {code,data}=await service.get('/gen/bat/1Remote/preview', {params: {title:title,startDir:startDir,exeName:exeName, startUlid:startUlid, seconds:seconds, fileName:fileName}})
   return data
}

