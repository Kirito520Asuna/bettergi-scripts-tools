package com.cloud_guest.runner;

import com.cloud_guest.utils.ModeUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;


import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库初始化器：在 @PostConstruct 阶段执行建表脚本，并在脚本完成后手动启动 Quartz 调度器。
 * 通过 @DependsOn("dataSource") 确保数据源已就绪。
 */
@Slf4j
@Component
@DependsOn("dataSource")       // 保证 DataSource 已初始化
@ConditionalOnProperty(prefix = "spring.datasource.init", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseInitRunner {

    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;
    private final Scheduler scheduler;

    private static final Map<String, String> DB_TYPE_TO_SCRIPT = new HashMap<>();
    static {
        DB_TYPE_TO_SCRIPT.put("MySQL", "mysql.sql");
        DB_TYPE_TO_SCRIPT.put("PostgreSQL", "pgsql.sql");
        DB_TYPE_TO_SCRIPT.put("SQLite", "sqlite.sql");
    }

    public DatabaseInitRunner(DataSource dataSource, ResourceLoader resourceLoader, Scheduler scheduler) {
        this.dataSource = dataSource;
        this.resourceLoader = resourceLoader;
        this.scheduler = scheduler;
    }

    @PostConstruct
    public void init() {
        // 1. 检测数据库类型并执行脚本
        String dbType = detectDatabaseType();
        if (dbType != null) {
            log.info("数据库类型：{}", dbType);
            if (dbType.equals("SQLite")){
                ModeUtil.setSqlite(true);
            }
            String scriptFileName = DB_TYPE_TO_SCRIPT.get(dbType);
            if (scriptFileName != null) {
                String location = "classpath:sql/" + scriptFileName;
                Resource resource = resourceLoader.getResource(location);
                if (resource.exists()) {
                    log.info("开始执行数据库脚本：{}", location);
                    try (Connection connection = dataSource.getConnection()) {
                        ScriptUtils.executeSqlScript(connection, resource);
                        log.info("脚本执行完成：{}", location);
                    } catch (Exception e) {
                        log.error("执行数据库脚本失败：{}", location, e);
                        // 可根据需求决定是否阻止启动，这里选择继续
                    }
                } else {
                    log.info("脚本文件 {} 不存在，跳过执行", location);
                }
            } else {
                log.info("数据库类型 {} 未配置对应脚本，跳过", dbType);
            }
        } else {
            log.warn("无法识别数据库类型，跳过脚本执行");
        }

        // 2. 手动启动 Quartz 调度器
        try {
            scheduler.start();
            log.info("Quartz 调度器已手动启动");
        } catch (SchedulerException e) {
            log.error("启动 Quartz 调度器失败", e);
        }
    }

    private String detectDatabaseType() {
        try (Connection connection = dataSource.getConnection()) {
            String productName = connection.getMetaData().getDatabaseProductName();
            log.debug("检测到数据库产品名：{}", productName);
            if (productName != null) {
                String lower = productName.toLowerCase();
                for (String key : DB_TYPE_TO_SCRIPT.keySet()) {
                    if (lower.contains(key.toLowerCase())) {
                        return key;
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取数据库连接失败", e);
        }
        return null;
    }
}