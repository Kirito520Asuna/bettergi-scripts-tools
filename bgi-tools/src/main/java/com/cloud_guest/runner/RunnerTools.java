package com.cloud_guest.runner;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.utils.object.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/5/11 4:04:04
 * @Description
 */
@Slf4j
public class RunnerTools {

    public static void init() {
        createCacheDir();
    }

    public static void destroy() {
        cleanDevDir();
    }

    public static void createCacheDir() {
        Environment env = SpringUtil.getBean(Environment.class);
        String CACHE_DIR = env.getProperty("local.cache.dir", "cache");
        log.info("初始化缓存目录：{}", CACHE_DIR);
        File file = FileUtil.newFile(CACHE_DIR);
        if (!file.exists()) {
            file.mkdirs();
            log.info("创建缓存目录：{}", CACHE_DIR);
        }
    }

    public static void cleanDevDir() {
        Environment env = SpringUtil.getBean(Environment.class);
        String activeProfile = env.getProperty("spring.profiles.active");
        String prodProfile = "prod";
        //log.info("当前环境：{}", activeProfile);
        //log.info("生产环境：{}", prodProfile);
        if (ObjectUtils.equals(activeProfile, prodProfile)) {
            return;
        }

        log.info("开发环境，开始清理临时目录...");

        // 获取项目根目录（bgi-tools 模块所在目录）
        String projectRoot = System.getProperty("user.dir");
        log.info("项目根目录：{}", projectRoot);

        String cacheDir = projectRoot + File.separator + env.getProperty("local.cache.dir", "./cache").replace("./", "");
        String logsDir = projectRoot + File.separator + env.getProperty("logging.file.path", "./logs").replace("./", "");
        String backupDir = projectRoot + File.separator + env.getProperty("config.backup-path", "backup").replace("./", "");

        record Clean(String path, String name) {
        }
        List.of(
                new Clean(cacheDir, "缓存目录"), new Clean(logsDir, "日志目录"), new Clean(backupDir, "备份目录")
        ).forEach(clean -> cleanDirectory(clean.path, clean.name));
    }


    /**
     * 清理指定目录
     *
     * @param dirPath 目录路径
     * @param dirName 目录名称（用于日志）
     */
    public static void cleanDirectory(String dirPath, String dirName) {
        try {
            File dir = new File(dirPath);
            if (!dir.exists()) {
                log.debug("{}不存在，跳过删除：{}", dirName, dirPath);
                return;
            }

            log.info("删除{}：{}", dirName, dirPath);

            // 使用 Files.walkFileTree 强制删除
            Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        // 尝试删除文件
                        Files.deleteIfExists(file);
                    } catch (IOException e) {
                        // 被占用时标记为删除失败，但继续处理其他文件
                        log.debug("文件被占用，标记为删除失败：{}", file.getFileName());
                        // 尝试标记为开机删除（Windows）
                        try {
                            file.toFile().deleteOnExit();
                        } catch (Exception ex) {
                            log.warn("无法标记文件为开机删除：{}", file.getFileName());
                        }

                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    try {
                        Files.deleteIfExists(dir);
                    } catch (IOException e) {
                        log.debug("目录被占用，无法删除：{}", dir.getFileName());
                        // 标记为开机删除
                        try {
                            dir.toFile().deleteOnExit();
                        } catch (Exception ex) {
                            log.warn("无法标记目录为开机删除：{}", dir.getFileName());
                        }

                    }
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e) {
            log.warn("删除{}时发生异常：{}", dirName, dirPath, e);
        }
    }
}
