package com.cloud_guest.runner;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.cloud_guest.utils.object.ObjectUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        closeDevDataSource();
        cleanDevDir();
    }

    private static void closeDevDataSource() {
        String prodProfile = "dev";
        if (!isEnv(prodProfile)) {
            return;
        }
        // 1. 手动关闭所有数据源，释放文件占用
        try {
            DynamicRoutingDataSource drds = SpringUtil.getBean(DynamicRoutingDataSource.class);
            // DynamicRoutingDataSource 继承自 AbstractRoutingDataSource，
            // 其内部实现了 close/destroy 方法，会遍历关闭所有真实数据源
            if (drds != null) {
                // 方式一：直接调用 destroy（如果实现了 DisposableBean）
                ((DisposableBean) drds).destroy();
                // 方式二：或者调用其内部的 destroyAll 方法（根据 dynamic-datasource 源码可能不同）
                // 如果上述不行，可以遍历获取所有数据源后逐个关闭：
                // Map<String, DataSource> dataSourceMap = drds.getDataSources();
                // dataSourceMap.values().forEach(ds -> {
                //     if (ds instanceof DruidDataSource) ((DruidDataSource) ds).close();
                // });
            }
        } catch (Exception e) {
            log.warn("强制关闭数据源失败", e);
        }
    }

    public static void createCacheDir() {
        Environment env = SpringUtil.getBean(Environment.class);
        String CACHE_DIR = env.getProperty("local.cache.dir", "cache");

        File file = FileUtil.newFile(CACHE_DIR);
        if (!file.exists()) {
            file.mkdirs();
            log.info("创建缓存目录：{}", CACHE_DIR);
        } else {
            log.info("已始化缓存目录：{}", CACHE_DIR);
        }
    }

    public static String getActive() {
        Environment env = SpringUtil.getBean(Environment.class);
        String activeProfile = env.getProperty("spring.profiles.active");
        return activeProfile;
    }

    public static boolean isEnv(String envName) {
        String active = getActive();
        String needActive = ObjectUtils.defaultIfEmpty(envName, "prod");
        return StrUtil.equals(active, needActive);
    }

    @SneakyThrows
    public static void cleanDevDir() {
        String prodProfile = "dev";
        if (!isEnv(prodProfile)) {
            return;
        }
        Environment env = SpringUtil.getBean(Environment.class);
        log.info("开发环境，开始清理临时目录...");

        // 获取项目根目录（bgi-tools 模块所在目录）
        String projectRoot = System.getProperty("user.dir");
        log.info("项目根目录：{}", projectRoot);

        String cacheDir = projectRoot + File.separator + env.getProperty("local.cache.dir", "./cache").replace("./", "");
        String logsDir = projectRoot + File.separator + env.getProperty("logging.file.path", "./logs").replace("./", "");
        String backupDir = projectRoot + File.separator + env.getProperty("config.backup-path", "backup").replace("./", "");
        // 2. 精确关闭文件 Appender，保留控制台输出
        //releaseLogFileAppenderList();
        forceReleaseLogFiles();
        TimeUnit.SECONDS.sleep(2);
        record Clean(String path, String name) {
        }
        List.of(
                new Clean(cacheDir, "缓存目录"), new Clean(logsDir, "日志目录"), new Clean(backupDir, "备份目录")
        ).forEach(clean -> cleanDirectory(clean.path, clean.name));
    }


    public static void forceReleaseLogFiles() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (Logger logger : context.getLoggerList()) {
            Iterator<Appender<ch.qos.logback.classic.spi.ILoggingEvent>> it = logger.iteratorForAppenders();
            while (it.hasNext()) {
                Appender<ch.qos.logback.classic.spi.ILoggingEvent> appender = it.next();
                if (appender instanceof RollingFileAppender || appender instanceof FileAppender) {
                    // 1. 正常停止（关闭文件流）
                    appender.stop();
                    // 2. 强制置空输出流，避免残留引用导致句柄不释放
                    try {
                        Field outputStreamField = appender.getClass().getDeclaredField("outputStream");
                        outputStreamField.setAccessible(true);
                        outputStreamField.set(appender, null);
                    } catch (Exception ignore) {
                        // 某些版本可能没有该字段，忽略
                    }
                    // 3. 如果 appender 还包装了内部的 UnsynchronizedAppenderBase，再处理一次
                    if (appender instanceof RollingFileAppender) {
                        RollingFileAppender<?> rfa = (RollingFileAppender<?>) appender;
                        // 如果是 prudent 模式，可能另有锁，但一般不用
                    }
                }
            }
        }
        // 4. 给操作系统一点时间释放文件句柄（关键！）
        try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
    /**
     * 停止所有文件类型的 Logback Appender，释放文件锁，但保留控制台输出
     */
    public static void releaseLogFileAppenderList() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (Logger logger : loggerContext.getLoggerList()) {
            Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders();
            while (it.hasNext()) {
                Appender<ILoggingEvent> appender = it.next();
                if (appender instanceof RollingFileAppender) {
                    var rfa = (RollingFileAppender<?>) appender;
                    // 强制置空输出流（反射，简单暴力）
                    try {
                        Field field = RollingFileAppender.class.getDeclaredField("outputStream");
                        field.setAccessible(true);
                        field.set(rfa, null);
                    } catch (Exception e) {
                        // fallback
                    }
                    //rfa.stop();
                }
                // 只停止写入文件（包括普通文件和滚动文件）的 Appender
                if (appender instanceof FileAppender || appender instanceof RollingFileAppender) {
                    appender.stop();
                    // 可选：从 logger 上移除，避免后续尝试写入已关闭的流
                    // logger.detachAppender(appender);
                }
            }
        }
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
                        log.debug("文件被占用，标记为删除失败：{},error:{}", file.getFileName(),e.getMessage());

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
                        log.debug("目录被占用，无法删除：{},error:{}", dir.getFileName(),e.getMessage());
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
