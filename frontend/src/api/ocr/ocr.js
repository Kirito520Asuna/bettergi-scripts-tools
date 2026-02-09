import service from "@utils/request";

/**
 * OCR识别函数，用于对字节数据进行文字识别
 * @param {ArrayBuffer|Blob|Uint8Array} bytes - 需要进行OCR识别的字节数据
 * @returns {Promise<Object>} 返回识别结果数据
 */
async function ocrBytes(bytes) {
    // 发送POST请求到OCR服务端点，传递字节数据
    const response = await service.post('/ocr/bytes', {
        bytes: bytes,  // 将字节数据作为请求参数发送
    })
    // 返回响应数据中的结果部分
    return response.data;
}
export {
    ocrBytes,
}