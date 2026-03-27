package com.cloud_guest.utils;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA工具类，提供RSA加密解密相关的功能
 */
public class RSAUtil {
    // RSA加密算法标识
    public static final String RSA = "RSA";
    // RSA加密时最大加密块大小
    private static final int MAX_ENCRYPT_BLOCK = 117;
    // RSA解密时最大解密块大小
    private static final int MAX_DECRYPT_BLOCK = 128;

    /**
     * 生成RSA密钥对
     * @return 返回生成的密钥对
     * @throws Exception 可能抛出异常
     */
    public static KeyPair generateKeyPair() throws Exception {
        // 创建密钥对生成器
        KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA);
        // 初始化密钥长度为1024位
        generator.initialize(1024);
        // 生成并返回密钥对
        return generator.generateKeyPair();
    }

    /**
     * 使用公钥加密数据
     * @param data 要加密的数据
     * @param publicKey 公钥
     * @return 返回Base64编码的加密结果
     * @throws Exception 可能抛出异常
     */
    public static String encryptByPublicKey(String data, PublicKey publicKey) throws Exception {
        // 创建Cipher实例
        Cipher cipher = Cipher.getInstance(RSA);
        // 初始化为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        // 将数据转换为字节数组
        byte[] dataBytes = data.getBytes();
        int inputLen = dataBytes.length;
        // 创建输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;

        // 分块加密处理
        while (inputLen - offSet > 0) {
            // 如果剩余数据大于最大加密块大小，则按最大块大小加密
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(dataBytes, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                // 否则按剩余数据大小加密
                cache = cipher.doFinal(dataBytes, offSet, inputLen - offSet);
            }
            // 将加密结果写入输出流
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        // 获取加密后的字节数组
        byte[] encryptedData = out.toByteArray();
        // 关闭输出流
        out.close();
        // 返回Base64编码的加密结果
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * 使用私钥解密数据
     * @param encryptedData Base64编码的加密数据
     * @param privateKey 私钥
     * @return 返回解密后的原始数据
     * @throws Exception 可能抛出异常
     */
    public static String decryptByPrivateKey(String encryptedData, PrivateKey privateKey) throws Exception {
        // 创建Cipher实例
        Cipher cipher = Cipher.getInstance(RSA);
        // 初始化为解密模式
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        // Base64解码加密数据
        byte[] dataBytes = Base64.getDecoder().decode(encryptedData);
        int inputLen = dataBytes.length;
        // 创建输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;

        // 分块解密处理
        while (inputLen - offSet > 0) {
            // 如果剩余数据大于最大解密块大小，则按最大块大小解密
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(dataBytes, offSet, MAX_DECRYPT_BLOCK);
            } else {
                // 否则按剩余数据大小解密
                cache = cipher.doFinal(dataBytes, offSet, inputLen - offSet);
            }
            // 将解密结果写入输出流
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        // 获取解密后的字节数组
        byte[] decryptedData = out.toByteArray();
        // 关闭输出流
        out.close();
        // 返回解密后的字符串
        return new String(decryptedData);
    }

    /**
     * 将Base64编码的公钥字符串转换为PublicKey对象
     * @param key Base64编码的公钥字符串
     * @return 返回PublicKey对象
     * @throws Exception 可能抛出异常
     */
    public static PublicKey stringToPublicKey(String key) throws Exception {
        // Base64解码公钥字符串
        byte[] keyBytes = Base64.getDecoder().decode(key);
        // 创建X509编码规范
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        // 创建密钥工厂
        KeyFactory factory = KeyFactory.getInstance(RSA);
        // 生成并返回公钥对象
        return factory.generatePublic(spec);
    }

    /**
     * 将Base64编码的私钥字符串转换为PrivateKey对象
     * @param key Base64编码的私钥字符串
     * @return 返回PrivateKey对象
     * @throws Exception 可能抛出异常
     */
    public static PrivateKey stringToPrivateKey(String key) throws Exception {
        // Base64解码私钥字符串
        byte[] keyBytes = Base64.getDecoder().decode(key);
        // 创建PKCS8编码规范
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        // 创建密钥工厂
        KeyFactory factory = KeyFactory.getInstance(RSA);
        // 生成并返回私钥对象
        return factory.generatePrivate(spec);
    }

/**
 * 将公钥转换为Base64编码的字符串表示
 * @param key 要转换的公钥对象
 * @return 返回Base64编码后的公钥字符串
 */
    public static String publicKeyToString(PublicKey key) {
    // 使用Base64编码器将公钥的字节编码转换为字符串
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * 将私钥转换为Base64编码的字符串表示
     * @param key 要转换的私钥对象
     * @return 返回Base64编码后的私钥字符串
     */
    public static String privateKeyToString(PrivateKey key) {
        // 使用Base64编码器将私钥的字节编码转换为字符串
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}