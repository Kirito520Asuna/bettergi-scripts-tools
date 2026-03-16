import service from "@utils/request.js";

/**
 * 数据备份 - 下载备份文件
 * @returns {Promise<Blob>} 备份文件的 Blob 对象
 */
async function backup() {
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
 * @param {File} file - 要上传的备份文件
 * @returns {Promise<Object>} 恢复结果
 */
async function recovery(file) {
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
}

export {
    backup,
    recovery,
}