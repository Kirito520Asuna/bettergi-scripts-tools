import service from "@utils/request.js";
import {generalSignWithObject, SingleSignature} from "@utils/Api.js";
import {
    base64ToPrivateKey,
    base64ToPublicKey,
    decryptByPrivateKey, encryptByPublicKey,
    generateKeyPair,
    pemToBase64
} from "@utils/RSAUtil.js";
import {postKey} from "@api/auth/key.js";

const salt = import.meta.env.VITE_BASE_SALT || ''
const sign_name = import.meta.env.VITE_BASE_SIGN_AS_NAME || 'X-Bgi-Tools-Signature'
const timestamp_name = import.meta.env.VITE_BASE_SIGN_AS_VALUE || 'X-Bgi-Tools-Timestamp'
const client_public_key_name = import.meta.env.VITE_BASE_CLIENT_PUBLIC_KEY || 'X-Bgi-Tools-Client-Public-Key'
const client_id_as_name = import.meta.env.VITE_BASE_CLIENT_ID_AS_NAME || 'X-Bgi-Tools-Encryption-Id'
const enable_double_symmetric_encryption = import.meta.env.VITE_BASE_ENABLE_DOUBLE_SYMMETRIC_ENCRYPTION === 'true'
const exCollection = [sign_name, timestamp_name]

export function getHostPrefix() {
    let basePath = import.meta.env.VITE_BASE_API_PATH || '/bgi/';

    if (/^https?:\/\//i.test(basePath)) {
        return basePath.endsWith('/') ? basePath : basePath + '/';
    }

    const protocol = window.location.protocol;
    const host = window.location.host;
    const re = `${protocol}//${host}${basePath}`;
    return re.endsWith('/') ? re : re + '/';
}

export function general(salt, method, url, params, body, exCollection) {
    const hostPrefix = getHostPrefix();
    if (!url.startsWith(hostPrefix)) {
        url = hostPrefix + url.replace(/^\//, '')//使用 replace(/^\//, '') 正则替换，语义清晰：移除开头的/
    }
    const signature = new SingleSignature()
    signature.setBody(body)
    signature.setMethod(method)
    signature.setParams(params)
    signature.setSalt(salt)
    signature.setUrl(url)
    signature.setExCollection(exCollection)

    const sign = generalSignWithObject(signature);
    return sign
}

/**
 * 通用签名函数
 * @param {string} sign - 需要签名的数据
 * @param {boolean} [doubleEncryption=true] - 是否使用双重加密，默认为true
 * @returns {Object} 返回包含签名信息的对象
 */
export async function generalSign(sign, doubleEncryption = true) {
    let keyInfo
    if (doubleEncryption) {
        // 1. 生成密钥对
        const {publicKey, privateKey} = generateKeyPair();

        // 将公钥和私钥从PEM格式转换为Base64格式
        const clientPublicKeyCP = pemToBase64(publicKey);
        const clientPrivateKeyCR = pemToBase64(privateKey);

        // 发送公钥到服务器并获取密钥信息
        const res = await postKey({name: client_public_key_name, value: clientPublicKeyCP});
        keyInfo = res
        // console.log("密钥信息：", keyInfo)
        const encryptedSP = keyInfo.publicKeyEncryption;
        // 3. 私钥解密
        const privateKeyObj = base64ToPrivateKey(clientPrivateKeyCR);
        const serverPublicKeySP = decryptByPrivateKey(encryptedSP, privateKeyObj);
        const publicKeyT = base64ToPublicKey(serverPublicKeySP);
        // 4. 使用服务器公钥加密
        const encrypt = encryptByPublicKey(sign, publicKeyT);
        sign = encrypt
    }
    // 返回签名相关信息
    return {
        doubleEncryption: doubleEncryption,// 是否使用双重加密
        sign: sign,                    // 原始签名数据
        sign_as_name: sign_name,       // 签名名称
        identifier: keyInfo?.id,        // 密钥标识符
        id_as_name: client_id_as_name, // 客户端ID名称
    }
}

/**
 * 构建签名函数
 * @param {Object} config - 配置对象，包含请求相关配置
 * @param {string} salt - 加密盐值
 * @param {string} method - HTTP请求方法
 * @param {string} url - 请求URL
 * @param {Object} params - 请求参数
 * @param {Object} data - 请求数据
 * @param {Object} exCollection - 额外集合信息
 * @returns {Promise<void>} - 无返回值，通过修改config对象来传递结果
 */
export async function buildSignature(config, salt, method, url, params, data, exCollection) {
    // 确保 config 对象存在
    if (!config) {
        config = {}
    }
    // 确保 headers 对象存在
    if (!config.headers) {
        config.headers = {}
    }
    // 生成基础签名
    let sign = general(salt, method, url, params, data, exCollection)
    // 如果配置中不禁用双重加密
    if (enable_double_symmetric_encryption ) {
        console.log("双重加密已启用")
        if (!config?.config?.noDoubleEncryption) {
            console.log("双重加密未禁用")
            // 执行双重加密处理
            const encryption = await generalSign(sign, enable_double_symmetric_encryption)
            console.log("===========")
            console.log("加密后的签名：", encryption.sign)
            // 将加密标识添加到请求头中
            config.headers[encryption.id_as_name] = encryption.identifier
            console.log("===========")
            // 更新签名为加密后的签名
            sign = encryption.sign
        }

    }

    // 添加时间戳和最终签名到请求头
    config.headers[timestamp_name] = Date.now();  // 当前时间戳作为签名标识
    config.headers[sign_name] = sign;  // 最终签名值
    return config
}

export const ApiService = {
    get: async (url, config) => {
        const params = config?.params;
        const method = 'get';
        const data = null
        config = await buildSignature(config, salt, method, url, params, data, exCollection)
        return await service.get(url, config)
    },
    post: async (url, data, config) => {
        const params = config?.params;
        const method = 'post';
        config = await buildSignature(config, salt, method, url, params, data, exCollection)
        return await service.post(url, data, config)
    },
    put: async (url, data, config) => {
        const params = config?.params;
        const method = 'put';
        config = await buildSignature(config, salt, method, url, params, data, exCollection)
        return await service.put(url, data, config)
    },
    delete: async (url, config) => {
        const params = config?.params;
        const method = 'delete';
        const data = null
        config = await buildSignature(config, salt, method, url, params, data, exCollection)
        return await service.delete(url, config)
    }
}