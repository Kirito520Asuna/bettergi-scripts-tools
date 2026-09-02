import service from '@utils/request.js'
import {ElMessage} from "element-plus";
import {ApiService} from "@utils/ApiRequest.js";

/**
 * 查询全部 uid 映射
 */
export async function getAllUid() {
    const {code, data} = await service.get('/jwt/uid/all')
    if (code === 200) {
        ElMessage.success("加载成功");
    }
    return data;
}

/**
 * 查询 uid 映射
 * @param {string} uid
 */
export async function getUid(uid) {
    const {code, data} = await service.get('/jwt/uid/info', {params: {uid: uid}})
    if (code === 200) {
        ElMessage.success("加载成功");
    }
    return data;
}

/**
 * 新增 uid 映射
 * @param {Object} uidInfo
 */
export async function saveUid(uidInfo) {
    const {code, data} = await service.post('/jwt/uid/info', uidInfo)
    if (code === 200) {
        ElMessage.success("保存成功");
    }
    return data;
}

/**
 * 移除 uid 映射
 * @param {string|array} ids - uid 字符串或数组
 */
export async function removeUidList(ids) {
    const idStr = Array.isArray(ids) ? ids.join(',') : ids
    const {code, data} = await service.delete('/jwt/uid/info', {
        params: {ids: idStr}
    })
    if (code === 200) {
        ElMessage.success("移除成功");
    }
    return data;
}

/**
 * 分页查询 uid team 映射
 * @param search
 * @param page
 * @returns {list:[{id: undefined, uid: undefined, type: undefined, team: undefined}],pageNumber:1, pageSize:10,pages:1, total: 0}
 */
export async function getTeamInfoPage(search = {
    id: undefined,
    uid: undefined,
    type: undefined,
}, page = {
    pageNumber: 1,
    pageSize: 10
}) {
  const {code, data} = await ApiService.get('/api/uid/team/page', {
        params: {
            ...search,
            page: page.pageNumber,
            size: page.pageSize
        }
    })

  return data
}

/**
 * 查询 uid team 映射
 * @returns {id: undefined, uid: undefined, type: undefined, team: undefined}
 */
export async function getTeamInfo(teamInfo={id:undefined,uid:undefined,type:undefined}){
    const {id,uid,type}=teamInfo
    let params={}
    let url = '';
    if (id) {
        url = '/api/uid/team/info';
        params.id= id
    }else if (uid && type){
        url = '/api/uid/team';
        params.uid= uid
        params.type= type
    }else {
        throw new Error("参数错误")
    }
    const {code, data} = await ApiService.get(url, {
        params: params
    })

    return data
}

/**
 * 更新 uid team 映射
 * @param UidTeam
 * @returns {id: undefined, uid: undefined, type: undefined, team: undefined}
 */
export async function updateTeamInfo(UidTeam={
    id: undefined,
    uid: undefined,
    type: undefined,
    team: undefined
}){
    const url = '/api/uid/team';
    const {code, data} = await ApiService.post(url, UidTeam)
    return data
}

/**
 * 删除 uid team 映射
 * @param ids
 * @returns boolean
 */
export async function deleteTeamInfoIds(ids=[]){
    const url = '/api/uid/team';
    const {code, data} = await ApiService.delete(url, {
        params: {
            ids: ids.join(',')
        }
    })
    return data
}