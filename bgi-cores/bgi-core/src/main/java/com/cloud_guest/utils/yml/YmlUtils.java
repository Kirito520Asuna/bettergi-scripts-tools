package com.cloud_guest.utils.yml;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

/**
 * @Author yan
 * @Date 2026/2/16 13:59:16
 * @Description
 */
@Slf4j
public class YmlUtils {
    private static final ObjectMapper YAML_MAPPER = new YAMLMapper();

    public static void main(String[] args) throws IOException {
        String absolutePath = FileUtil.getAbsolutePath("application.yml");

        File file = FileUtil.newFile("application.yml");
        JSONObject jsonObject = readValue(file, JSONObject.class);
        log.debug(JSONUtil.toJsonStr(jsonObject));
        JSONObject byPath = (JSONObject)jsonObject.getByPath("check.token");
        if (byPath == null) {
            byPath = new JSONObject();
        }
        byPath.put("name", "token");
        byPath.put("value", "123456");
        log.debug(JSONUtil.toJsonStr(jsonObject));
        writeValue(file, jsonObject);
        try {

        } catch (IllegalArgumentException e) {
            //throw new RuntimeException(e);
            if (e.getMessage().contains("文件不存在或为空")) {

            }else {
                throw e;
            }
        }
    }
    /**
     * 从文件读取 YAML 数据并转换为指定类型
     *
     * @param file  YAML 文件
     * @param clazz 目标类型 Class
     * @param <T>   泛型类型
     * @return 转换后的对象
     * @throws IOException 读取或解析异常
     */
    public static <T> T readValue(File file, Class<T> clazz) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在或为空: " + (file != null ? file.getPath() : "null"));
        }
        try {
            log.debug("开始读取 YAML 文件: {}", file.getAbsolutePath());
            T result = YAML_MAPPER.readValue(file, clazz);
            log.debug("成功读取 YAML 文件: {}", file.getAbsolutePath());
            return result;
        } catch (IOException e) {
            log.error("读取 YAML 文件失败: {}", file.getAbsolutePath(), e);
            throw e;
        }
    }

    /**
     * 从路径读取 YAML 数据并转换为指定类型
     *
     * @param path  YAML 文件路径
     * @param clazz 目标类型 Class
     * @param <T>   泛型类型
     * @return 转换后的对象
     * @throws IOException 读取或解析异常
     */
    public static <T> T readValue(Path path, Class<T> clazz) throws IOException {
        return readValue(path.toFile(), clazz);
    }

    /**
     * 从 InputStream 读取 YAML 数据并转换为指定类型
     *
     * @param inputStream 输入流
     * @param clazz       目标类型 Class
     * @param <T>         泛型类型
     * @return 转换后的对象
     * @throws IOException 读取或解析异常
     */
    public static <T> T readValue(InputStream inputStream, Class<T> clazz) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("输入流不能为空");
        }
        try {
            log.debug("开始从输入流读取 YAML 数据");
            T result = YAML_MAPPER.readValue(inputStream, clazz);
            log.debug("成功从输入流读取 YAML 数据");
            return result;
        } catch (IOException e) {
            log.error("从输入流读取 YAML 数据失败", e);
            throw e;
        }
    }
    /**
     * 将对象写入 YAML 文件
     *
     * @param file  目标文件
     * @param value 要写入的对象
     * @throws IOException 写入异常
     */
    public static void writeValue(File file, Object value) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (value == null) {
            throw new IllegalArgumentException("写入值不能为空");
        }
        try {
            log.debug("开始写入 YAML 文件: {}", file.getAbsolutePath());
            YAML_MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(file, value);
            log.debug("成功写入 YAML 文件: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("写入 YAML 文件失败: {}", file.getAbsolutePath(), e);
            throw e;
        }
    }

    /**
     * 将对象写入 YAML 文件路径
     *
     * @param path  目标路径
     * @param value 要写入的对象
     * @throws IOException 写入异常
     */
    public static void writeValue(Path path, Object value) throws IOException {
        writeValue(path.toFile(), value);
    }

    public static void modify(Path path) throws IOException {
        // 1. 读成 Map
        Map<String, Object> config = YAML_MAPPER.readValue(path.toFile(), Map.class);

        // 2. 修改
        Map<String, Object> app = (Map<String, Object>) config.computeIfAbsent("app", k -> new java.util.LinkedHashMap<>());
        app.put("version", "2.1.0");
        app.put("debug", true);

        // 3. 写回（Jackson YAML 会尽量保持美观）
        YAML_MAPPER.writerWithDefaultPrettyPrinter()
                .writeValue(path.toFile(), config);

        System.out.println("修改完成：" + path);
    }
}
