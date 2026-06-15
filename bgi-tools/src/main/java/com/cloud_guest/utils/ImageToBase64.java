package com.cloud_guest.utils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import java.io.ByteArrayOutputStream;
/**
 * @Author yan
 * @Date 2026/4/29 12:08:00
 * @Description
 */
@Slf4j 
public class ImageToBase64 {

    /**
     * 核心方法：将字节数组直接转换为 Base64 编码字符串。
     *
     * @param data 原始字节数据
     * @return Base64 编码后的字符串，不会返回 null
     */
    public static String encodeBytesToBase64(byte[] data) {
        String encoded = Base64.getEncoder().encodeToString(data);
        log.debug("Base64 编码: {}", encoded);
        return encoded;
    }

    /**
     * 将文件内容全部读入字节数组，然后调用核心方法进行 Base64 编码。
     * 适用于图片、文本等任何二进制文件。
     *
     * @param filePath 文件路径
     * @return Base64 字符串，若发生错误则返回 null
     */
    public static String encodeFileToBase64(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            log.error("文件不存在或路径无效: {}", filePath);
            return null;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = readAllBytes(fis, (int) file.length());
            return encodeBytesToBase64(data);
        } catch (IOException e) {
            log.error("读取文件失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将输入流中的所有字节读取为字节数组，然后调用核心方法编码。
     * 不依赖预估的文件大小，适用于任意 InputStream。
     *
     * @param inputStream 输入流，调用后会被关闭
     * @return Base64 字符串，若发生错误则返回 null
     */
    public static String encodeInputStreamToBase64(InputStream inputStream) {
        try (InputStream is = inputStream) {
            byte[] data = readAllBytes(is);
            return encodeBytesToBase64(data);
        } catch (IOException e) {
            log.error("读取输入流失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将输入流中的字节全部读取到字节数组（已知预估大小的版本）。
     * 内部使用，当已知文件大小时可避免缓冲区动态扩容。
     */
    private static byte[] readAllBytes(InputStream is, int estimatedSize) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(estimatedSize > 0 ? estimatedSize : 8192);
        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }

    /**
     * 将输入流中的字节全部读取到字节数组（未知大小版本）。
     */
    private static byte[] readAllBytes(InputStream is) throws IOException {
        return readAllBytes(is, 0);
    }

    // ---------- 旧方法保留兼容性，内部直接调用 encodeFileToBase64 ----------
    /**
     * @deprecated 请使用 {@link #encodeFileToBase64(String)} 代替，功能完全相同。
     */
    @Deprecated
    public static String encodeImageToBase64(String imagePath) {
        return encodeFileToBase64(imagePath);
    }

/*    // 简单测试入口
    public static void main(String[] args) {
        String path = "D:\\Administrator\\Pictures\\Snipaste_2026-04-29_12-08-19.jpg";
        String result = encodeFileToBase64(path);
        if (result != null) {
            log.info("Base64 编码长度: {}", result.length());
            // 可输出前 100 个字符预览
            log.info("{}", result);
        }
    }*/
}