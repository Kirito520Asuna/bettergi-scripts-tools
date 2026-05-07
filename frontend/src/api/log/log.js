import service from "@utils/request.js";

export async function getLogAuthToken() {
    const response = await service.get('/jwt/log/auth-token');
    return response
}

export async function getFileNames(applicationId){
   return  await service.get('/jwt/log/file-names', {
        params: { applicationId: applicationId }
    })
}