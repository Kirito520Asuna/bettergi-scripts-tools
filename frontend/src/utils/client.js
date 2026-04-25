// const forge = require('node-forge');
// const axios = require('axios');
import forge from 'node-forge';
import axios from 'axios';
// 1. 生成 RSA 密钥对
function generateKeyPair(bits = 2048) {
    const keypair = forge.pki.rsa.generateKeyPair({ bits: bits });

    const publicKey = forge.pki.publicKeyToPem(keypair.publicKey);
    const privateKey = forge.pki.privateKeyToPem(keypair.privateKey);

    return { publicKey, privateKey };
}
// Base64（去掉 PEM 头尾，和 Java 对齐）
function pemToBase64(pem) {
    return pem
        .replace(/-----.*?-----/g, '')
        .replace(/\r?\n/g, '');
}

// 还原 PEM
function base64ToPrivateKey(base64) {
    return forge.pki.privateKeyFromPem(
        `-----BEGIN PRIVATE KEY-----\n${base64}\n-----END PRIVATE KEY-----`
    );
}

// RSA 解密（对应 Java decryptByPrivateKey）
function decryptByPrivateKey(encryptedBase64, privateKey) {
 /*   const encryptedBytes = forge.util.decode64(encryptedBase64);
    const decrypted = privateKey.decrypt(encryptedBytes, 'RSAES-PKCS1-V1_5');
    return decrypted;*/
    return decryptLong(encryptedBase64, privateKey);
}
function decryptLong(encryptedBase64, privateKey) {
    const encryptedBytes = forge.util.decode64(encryptedBase64);

    // 自动计算位数
    const keyLength = privateKey.n.bitLength(); // 1024 / 2048
    const blockSize = keyLength / 8;

    let result = '';

    for (let i = 0; i < encryptedBytes.length; i += blockSize) {
        const block = encryptedBytes.slice(i, i + blockSize);
        const decrypted = privateKey.decrypt(block, 'RSAES-PKCS1-V1_5');
        result += decrypted;
    }

    return result;
}


/**
 * RSA 加密（对应 Java encryptByPublicKey）
 * @param {string} data - 待加密的原始数据
 * @param {string} publicKeyBase64 - Base64 格式的公钥
 * @returns {string} Base64 编码的加密后字符串
 */
function encryptByPublicKey(data, publicKey) {
    return encryptLong(data, publicKey);
}

/**
 * Base64 转换为公钥对象
 * @param {string} base64 - Base64 格式的公钥
 * @returns {object} Forge 公钥对象
 */
function base64ToPublicKey(base64) {
    return forge.pki.publicKeyFromPem(
        `-----BEGIN PUBLIC KEY-----\n${base64}\n-----END PUBLIC KEY-----`
    );
}

/**
 * 长数据加密（分段加密）
 * @param {string} data - 待加密的长数据
 * @param {string} publicKey - Forge 公钥对象
 * @returns {string} Base64 编码的加密后字符串
 */
function encryptLong(data, publicKey) {
    const keyLength = publicKey.n.bitLength();
    const blockSize = Math.floor(keyLength / 8) - 11;
    let encryptedBytes = '';

    // 直接按字符串分段
    for (let i = 0; i < data.length; i += blockSize) {
        const block = data.slice(i, i + blockSize);
        // 直接加密字符串，forge 自动处理编码
        const encryptedBlock = publicKey.encrypt(block, 'RSAES-PKCS1-V1_5');
        encryptedBytes += encryptedBlock;
    }

    return forge.util.encode64(encryptedBytes);
}


(async () => {
    // 1. 生成密钥
    const { publicKey, privateKey } = generateKeyPair();

    const clientPublicKeyCP = pemToBase64(publicKey);
    const clientPrivateKeyCR = pemToBase64(privateKey);

    console.log("===== 客户端生成密钥 =====");
    console.log("客户端公钥 CP：\n", clientPublicKeyCP);

    // 2. 请求服务端
    const response = await axios.post(
        'http://127.0.0.1:8081/bgi/key/exchangeKey',
        {},
        {
            headers: {
                'client-public-key': clientPublicKeyCP
            }
        }
    );

    const encryptedSP = response.data.data;

    console.log("\n===== 收到加密SP =====");
    console.log(encryptedSP);

    // 3. 私钥解密
    const privateKeyObj = base64ToPrivateKey(clientPrivateKeyCR);
    const serverPublicKeySP = decryptByPrivateKey(encryptedSP, privateKeyObj);

    console.log("\n===== 解密成功 =====");
    console.log("服务端公钥 SP：\n", serverPublicKeySP);
    console.log("===== 待加密数据 =====");
    const number="1230"
    console.log(number);
    const publicKeyT = base64ToPublicKey(clientPublicKeyCP);
    const privateKeyT = base64ToPrivateKey(clientPrivateKeyCR);
    console.log("===== 加密 =====");
    const encrypt = encryptByPublicKey(number, publicKeyT);
    console.log(encrypt);
    console.log("===== 解密 =====");
    const decrypt = decryptByPrivateKey(encrypt, privateKeyT);
    console.log(decrypt);
})();