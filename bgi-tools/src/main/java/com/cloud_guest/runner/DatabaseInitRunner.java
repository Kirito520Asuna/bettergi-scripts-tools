package com.cloud_guest.runner;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.utils.ModeUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;


import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private final JdbcTemplate jdbcTemplate;

    private static final Map<String, String> DB_TYPE_TO_SCRIPT = new HashMap<>();
    private static final Map<String, List<String>> DB_TYPE_TO_DB_SCRIPT = new HashMap<>();
    static {
        DB_TYPE_TO_SCRIPT.put("MySQL", "mysql.sql");
        DB_TYPE_TO_SCRIPT.put("PostgreSQL", "pgsql.sql");
        DB_TYPE_TO_SCRIPT.put("SQLite", "sqlite.sql");

        DB_TYPE_TO_DB_SCRIPT.put("SQLite", List.of(
                "ALTER TABLE auto_plan_config ADD COLUMN record INTEGER DEFAULT 0"
        ));

        DB_TYPE_TO_DB_SCRIPT.put("PostgreSQL", List.of("""
            DO $$ 
            BEGIN 
                IF NOT EXISTS (
                    SELECT 1 FROM information_schema.columns 
                    WHERE table_name = 'auto_plan_config' AND column_name = 'record'
                ) THEN 
                    ALTER TABLE auto_plan_config ADD COLUMN record BOOLEAN DEFAULT NULL; 
                    COMMENT ON COLUMN auto_plan_config.record IS '是否记录'; 
                END IF; 
            END $$;
            """));

        DB_TYPE_TO_DB_SCRIPT.put("MySQL", List.of("""
            SET @dbname = DATABASE();
            SET @tablename = 'auto_plan_config';
            SET @columnname = 'record';
            SET @preparedStatement = (SELECT IF(
              (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0, 
              'SELECT 1', 
              CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN `', @columnname, '` TINYINT(1) DEFAULT NULL COMMENT ''是否记录'' AFTER `remark`')
            ));
            PREPARE alterIfNotExists FROM @preparedStatement;
            EXECUTE alterIfNotExists;
            DEALLOCATE PREPARE alterIfNotExists;
            """));
    }

    public DatabaseInitRunner(DataSource dataSource, ResourceLoader resourceLoader, Scheduler scheduler, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.resourceLoader = resourceLoader;
        this.scheduler = scheduler;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        // 1. 检测数据库类型并执行脚本
        String dbType = detectDatabaseType();
        if (dbType != null) {
            log.info("数据库类型：{}", dbType);

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

                    List<String> list = DB_TYPE_TO_DB_SCRIPT.get(dbType);
                    if (CollUtil.isNotEmpty(list)) {
                        for (String sql : list) {
                            try {
                                jdbcTemplate.execute(sql);
                            } catch (Exception e) {
                                String msg = e.getMessage();
                                if (msg != null && (
                                        msg.contains("duplicate column name") ||
                                                msg.contains("Duplicate column name") ||
                                                msg.contains("column already exists") ||
                                                msg.contains("already exists")
                                )) {
                                    log.debug("字段已存在，跳过添加, {}", msg);
                                } else {
                                    log.warn("执行迁移脚本失败: {}", sql, e);
                                }
                            }
                        }
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
            Duration startupDelay = SpringUtil.getBean(QuartzProperties.class).getStartupDelay();
            // 线程休眠实现延迟
            long delaySeconds = startupDelay.toSeconds();
            log.info("Quartz 调度器延迟[{}s]", delaySeconds);
            TimeUnit.SECONDS.sleep(delaySeconds);
            scheduler.start();
            log.info("Quartz 调度器已手动启动");
        } catch (SchedulerException e) {
            log.error("启动 Quartz 调度器失败", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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