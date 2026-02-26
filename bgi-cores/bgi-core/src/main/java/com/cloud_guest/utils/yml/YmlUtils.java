package com.cloud_guest.utils.yml;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.enums.OSType;
import com.cloud_guest.properties.auth.AuthProperties;
import com.cloud_guest.utils.object.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
        test03();
        //test02();
        //test01();
    }

    private static void test03() throws IOException {
        String path = "G:\\code\\bettergi-scripts-tools\\bgi-cores\\bgi-core\\src\\main\\java\\com\\cloud_guest\\utils\\yml\\application.yml";
        String path1 = "G:\\code\\bettergi-scripts-tools\\bgi-cores\\bgi-core\\src\\main\\java\\com\\cloud_guest\\utils\\yml\\application-prod.yml";
        String authKey = "auth";
        String usersKey = "users";
        String authUsersKey = authKey + "." + usersKey;
        String username = "username";
        String password = "username";
        ArrayList<String> list = new ArrayList<>();
        //list.add(path1);
        list.add("/app/a.yml");
        //list.add(path);
        OSType currentOSType = OSType.getCurrentOSType();
        for (String yamlPath : list) {
            OSType osType = OSType.detectByPathFormat(yamlPath);
            if(ObjectUtils.equals(OSType.UNKNOWN, osType)){
                log.debug("{}是相对路径", yamlPath);
            }else if(OSType.isUnixLike(osType)){
                if(!OSType.isUnixLike(null)){
                    //地址 linux ,系统非linux
                    log.debug("[{}]{}是{}绝对路径",currentOSType, yamlPath,"[非类unix系统]");
                    continue;
                }
            }else if (!OSType.isUnixLike(null)){
                if(OSType.isUnixLike(osType)){
                    //地址 非linux ,系统linux
                    log.debug("[{}]{}是{}绝对路径",currentOSType, yamlPath,"[非类unix系统]");
                    continue;
                }
            }

            JSONObject jsonObject = YmlUtils.readValueToJSONObject(yamlPath);
            File file = FileUtil.newFile(yamlPath);

            if (file == null || !file.exists()) {
                continue;
            }
            JSONObject auth = (JSONObject) jsonObject.getByPath(authKey);
            if (auth == null || true) {
                auth = new JSONObject();
            }
            JSONObject users = new JSONObject();

            ArrayList arrayList = new ArrayList();

            AuthProperties.User value = new AuthProperties.User(username, password);
            arrayList.add(value);
            String jsonStr = JSONUtil.toJsonStr(arrayList);


            JSONArray jsonArray = JSONUtil.parseArray(jsonStr);
            users.put(usersKey, jsonArray);
            auth.put(authKey, users);
            jsonObject.putAll(auth);
            YmlUtils.writeValue(file, jsonObject);
        }

    }

    private static void test02() {
        /**
         * check:
         *   token:
         *     name: "token"
         *     value: "123456"
         */
        String name = "token";
        String value = "555";
        String yamlPath = "G:\\code\\bettergi-scripts-tools\\bgi-cores\\bgi-core\\src\\main\\java\\com\\cloud_guest\\utils\\yml\\application.yml";
        try {
            File file = FileUtil.newFile(yamlPath);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject = YmlUtils.readValue(file, JSONObject.class);
            } catch (MismatchedInputException e) {
                log.warn("{}", e.getMessage());
            }

            String checkName = "check";
            String tokenName = "token";
            String nameKey = "name";
            String valueKey = "value";

            JSONObject check = (JSONObject) jsonObject.getByPath(checkName);

            if (check == null) {
                JSONObject token = new JSONObject();
                JSONObject tokenValue = new JSONObject();

                tokenValue.put(nameKey, name);
                tokenValue.put(valueKey, value);

                token.put(tokenName, tokenValue);
                jsonObject.put(checkName, token);
                check = (JSONObject) jsonObject.getByPath(checkName);
                jsonObject.put(checkName, check);
            }
            JSONObject token = (JSONObject) jsonObject.getByPath(checkName + "." + tokenName);
            if (token == null) {

                token = new JSONObject();
                JSONObject tokenValue = new JSONObject();

                tokenValue.put(nameKey, name);
                tokenValue.put(valueKey, value);

                token.put(tokenName, tokenValue);
                jsonObject.put(checkName, token);

                token = (JSONObject) jsonObject.getByPath(checkName + "." + tokenName);
            }
            token.put(nameKey, name);
            token.put(valueKey, value);
            YmlUtils.writeValue(file, jsonObject);
        } catch (Exception e) {
        }
    }

    private static void test01() throws IOException {
        try {
            String absolutePath = FileUtil.getAbsolutePath("application.yml");

            File file = FileUtil.newFile("application.yml");
            JSONObject jsonObject = readValue(file, JSONObject.class);
            log.debug(JSONUtil.toJsonStr(jsonObject));
            JSONObject byPath = (JSONObject) jsonObject.getByPath("check.token");
            if (byPath == null) {
                byPath = new JSONObject();
            }
            byPath.put("name", "token");
            byPath.put("value", "123456");
            log.debug(JSONUtil.toJsonStr(jsonObject));
            writeValue(file, jsonObject);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            //throw new RuntimeException(e);
            if (!e.getMessage().contains("文件不存在或为空")) {
                throw e;
            }
        }
    }
    public static JSONObject readValueToJSONObject(String yamlPath) throws IOException {
        JSONObject jsonObject = new JSONObject();
        File file = FileUtil.newFile(yamlPath);
        if (file == null || !file.exists()) {
            // 创建文件及其父目录
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try {
                YmlUtils.writeValue(file, jsonObject);
            }catch (Exception e){
                log.warn("{}", e.getMessage());
            }
        }else {
            try {
                jsonObject = YmlUtils.readValue(file, JSONObject.class);
            } catch (MismatchedInputException e) {
                log.warn("{}", e.getMessage());
            } catch (Exception e) {
                if (e.getMessage().contains("文件不存在或为空")) {
                    //jsonObject=null;
                }else {
                    throw e;
                }
            }
        }

        return jsonObject;
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
    // 检查文件是否存在或为空
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在或为空: " + (file != null ? file.getPath() : "null"));
        }
        try {
        // 记录开始读取文件的日志
            log.debug("开始读取 YAML 文件: {}", file.getAbsolutePath());
        // 使用 YAML_MAPPER 将文件内容读取为目标类型对象
            T result = YAML_MAPPER.readValue(file, clazz);
        // 记录成功读取文件的日志
            log.debug("成功读取 YAML 文件: {}", file.getAbsolutePath());
            return result;
        } catch (IOException e) {
        // 记录读取文件失败的错误日志
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

    /**
     * 将对象写入 OutputStream
     *
     * @param out
     * @param value
     * @throws IOException
     */
    public static void writeValue(OutputStream out, Object value) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("输出流不能为空");
        }
        if (value == null) {
            throw new IllegalArgumentException("写入值不能为空");
        }
        try {
            log.debug("开始写入 YAML 文件");
            YAML_MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(out, value);
            log.debug("成功写入 YAML 文件");
        } catch (IOException e) {
            log.error("写入 YAML 文件失败: {}", e.getMessage());
            throw e;
        }
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
