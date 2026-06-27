import service from "@utils/request.js";

/**
 * 数据备份
 * @returns {Promise<Object>} 备份结果
 */
async function backup() {
    try {
        const response = await service.get('/jwt/data/backup');
        return response?.data;
    } catch (error) {
        console.error('备份失败:', error);
        throw error;
    }
}

/**
 * 数据备份 - 下载备份文件
 * @returns {Promise<Blob>} 备份文件的 Blob 对象
 */
async function backupDownload() {
    try {
        const response = await service.get('/jwt/data/backup/download', {
            responseType: 'blob'
        });
        return response;
    } catch (error) {
        console.error('备份失败:', error);
        throw error;
    }
}

/**
 * 数据恢复 - 上传备份文件并恢复
 * @param {File|null} file - 要上传的备份文件（可选）
 * @param {boolean} isLocal - 是否为本地备份
 * @param {string|null} name - 备份名称（本地备份时使用）
 * @param {number|null} id - 备份ID（远程备份时使用）
 * @returns {Promise<Object>} 恢复结果
 */
async function recovery(file = null, isLocal = false, name = null, id = null) {
    if (file) {
        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await service.post('/jwt/data/recovery/file', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            return response;
        } catch (error) {
            console.error('恢复失败:', error);
            throw error;
        }
    } else {
        try {
            const params = {
                isLocal: isLocal
            };
            if (id) {
                params.id = id;
            }
            if (name) {
                params.name = name;
            }

            const response = await service.get('/jwt/data/recovery', {
                params: params
            });
            return response;
        } catch (error) {
            console.error('恢复失败:', error);
            throw error;
        }
    }
}

/**
 * 获取本地备份列表
 * @returns {Promise<Array>} 本地备份列表
 */
async function getLocalBackupList() {
    try {
        const {code,data} = await service.get('/jwt/data/backup/local');
        return data || [];
    } catch (error) {
        console.error('获取本地备份列表失败:', error);
        throw error;
    }
}

/**
 * 获取远程备份分页列表
 * @param {number} pageNumber - 页码
 * @param {number} pageSize - 每页大小
 * @returns {Promise<Object>} 分页结果
 */
async function getRemoteBackupPage(pageNumber, pageSize) {
    try {
        const {code,data} = await service.get('/jwt/data/backup/page', {
            params: {
                pageNumber: pageNumber || 1,
                pageSize: pageSize || 10
            }
        });
        return data || undefined;
    } catch (error) {
        console.error('获取远程备份列表失败:', error);
        throw error;
    }
}

/**
 * 下载指定ID的备份文件
 * @param {number} id - 备份ID
 * @returns {Promise<Blob>} 备份文件的 Blob 对象
 */
async function downloadBackupById(id) {
    try {
        const response = await service.get('/jwt/data/backup/download', {
            params: { id },
            responseType: 'blob'
        });
        return response;
    } catch (error) {
        console.error('下载备份文件失败:', error);
        throw error;
    }
}

/**
 * 批量删除备份
 * @param {Array<number>} ids - 备份ID列表
 * @returns {Promise<Object>} 删除结果
 */
async function deleteBatchBackup(ids) {
    try {
        const response = await service.delete('/jwt/data/backup/batch', {
            params: {
                ids: ids.join(',')
            }
        });
        return response;
    } catch (error) {
        console.error('批量删除备份失败:', error);
        throw error;
    }
}

/**
 * 批量删除本地备份
 * @param {Array<string>} paths - 备份文件路径列表
 * @returns {Promise<Object>} 删除结果
 */
async function deleteBatchBackupLocal(paths) {
    try {
        const response = await service.post('/jwt/data/backup/batch/local', paths);
        return response;
    } catch (error) {
        console.error('批量删除本地备份失败:', error);
        throw error;
    }
}

export {
    backup,
    backupDownload,
    recovery,
    getLocalBackupList,
    getRemoteBackupPage,
    downloadBackupById,
    deleteBatchBackup,
    deleteBatchBackupLocal,
}