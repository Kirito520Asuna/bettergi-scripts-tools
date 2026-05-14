// import crypto from 'crypto'
import CryptoJS from 'crypto-js'
/**
 * 将嵌套 JSON 对象扁平化为单层 Map，提取所有叶子节点值
 * @param {Object} obj - 待处理的对象
 * @returns {Map<string, any>} 扁平化后的 Map
 */
export function fieldForSort(obj) {
    const fieldMap = new Map()

    function extractFields(value, prefix = '') {
        if (value === null || value === undefined) {
            return
        }

        if (Array.isArray(value)) {
            // 遍历数组元素
            value.forEach(item => {
                extractFields(item, prefix)
            })
        } else if (typeof value === 'object') {
            // 遍历对象属性
            Object.entries(value).forEach(([key, val]) => {
                const fullKey = prefix ? `${prefix}.${key}` : key
                extractFields(val, fullKey)
            })
        } else {
            // 叶子节点，直接添加值（使用最后一个 key 名）
            const lastKey = prefix.includes('.')
                ? prefix.split('.').pop()
                : prefix
            if (lastKey) {
                fieldMap.set(lastKey, String(value))
            }
        }
    }

    extractFields(obj)
    return fieldMap
}

/**
 * 生成 API 请求签名
 * @param {Object} options - 签名配置
 * @param {string} options.salt - 签名密钥
 * @param {string} options.method - 请求方法
 * @param {string} options.url - 请求 URL
 * @param {Object} [options.params] - 查询参数
 * @param {Object} [options.body] - 请求体数据
 * @param {Array<string>} [options.exCollection] - 需要排除的参数名集合
 * @returns {string} MD5 签名（十六进制）
 */
export function generalSign({ salt, method, url, params, body, exCollection = [] }) {
    console.log('签名URL:', url)
    // 构建用于生成签名的字符串
    let signatureString = `${method.toUpperCase()}${url}`

    // 合并参数（优先使用 params，其次使用 body）
    const sourceData = params || body || {}

    // 过滤并转换参数
    const filteredParams = Object.entries(sourceData)
        .filter(([_, value]) => {
            // 处理数组值（兼容 Java 的 String[]）
            const actualValue = Array.isArray(value) ? value[0] : value
            return actualValue !== null && actualValue !== 'null' && actualValue !== undefined
        })
        .reduce((acc, [key, value]) => {
            const actualValue = Array.isArray(value) ? value[0] : value
            acc[key] = actualValue
            return acc
        }, {})

    // 扁平化嵌套对象并排序
    const flatMap = fieldForSort(filteredParams)
    const sortedMap = new Map([...flatMap.entries()].sort())

    // 拼接参数字符串
    if (sortedMap.size > 0) {
        signatureString += '?'

        for (const [key, value] of sortedMap) {
            if (!exCollection.includes(key)) {
                signatureString += `${key}=${value}&`
            }
        }
    }

    // 追加签名密钥
    signatureString += salt

    // 处理转义字符
    const preContent = signatureString
        .replace(/\\+n/g, '\n')
        .replace(/\\+r/g, '\r')

    // URL 编码
    const encoded = encodeURIComponent(preContent)
        .replace(/\+/g, '%20')
        .replace(/\*/g, '%2A')

    // 计算 MD5
    // return crypto
    //     .createHash('md5')
    //     .update(encoded, 'utf8')
    //     .digest('hex')

    // ✅ 替换 MD5 计算
    return CryptoJS.MD5(encoded).toString()
}

/**
 * 便捷签名方法（接收 SingleSignature 实例）
 * @param {Object} signature - 签名配置对象
 * @returns {string} MD5 签名
 */
export function generalSignWithObject(signature) {
    return generalSign({
        salt: signature.salt,
        method: signature.method,
        url: signature.url,
        params: signature.params,
        body: signature.body,
        exCollection: signature.exCollection || []
    })
}

/**
 * 单次签名配置类
 * 用于封装 API 请求签名所需的所有参数
 */
export class SingleSignature {
    /**
     * 签名密钥
     */
    salt = null

    /**
     * 请求方法
     */
    method = null

    /**
     * 请求 URL
     */
    url = null

    /**
     * 请求参数（查询参数）
     */
    params = null

    /**
     * 请求体（JSON 数据）
     */
    body = null

    /**
     * 排除参数（不参与签名的参数名集合）
     */
    exCollection = null

    /**
     * 构造函数
     * @param {Object} options - 配置选项
     * @param {string} options.salt - 签名密钥
     * @param {string} options.method - 请求方法
     * @param {string} options.url - 请求 URL
     * @param {Object} options.params - 请求参数
     * @param {Object} options.body - 请求体
     * @param {Array<string>} options.exCollection - 排除参数
     */
    constructor(options = {}) {
        Object.assign(this, options)
    }

    /**
     * 设置签名密钥
     * @param {string} salt - 签名密钥
     * @returns {SingleSignature} this
     */
    setSalt(salt) {
        this.salt = salt
        return this
    }

    /**
     * 设置请求方法
     * @param {string} method - 请求方法
     * @returns {SingleSignature} this
     */
    setMethod(method) {
        this.method = method
        return this
    }

    /**
     * 设置请求 URL
     * @param {string} url - 请求 URL
     * @returns {SingleSignature} this
     */
    setUrl(url) {
        this.url = url
        return this
    }

    /**
     * 设置请求参数
     * @param {Object} params - 请求参数
     * @returns {SingleSignature} this
     */
    setParams(params) {
        this.params = params
        return this
    }

    /**
     * 设置请求体
     * @param {Object} body - 请求体
     * @returns {SingleSignature} this
     */
    setBody(body) {
        this.body = body
        return this
    }

    /**
     * 设置排除参数
     * @param {Array<string>} exCollection - 排除参数
     * @returns {SingleSignature} this
     */
    setExCollection(exCollection) {
        this.exCollection = exCollection
        return this
    }
}
