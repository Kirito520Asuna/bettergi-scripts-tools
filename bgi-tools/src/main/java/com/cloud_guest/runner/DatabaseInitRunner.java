package com.cloud_guest.runner;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.entitys.pojo.AutoPlanConfig;
import com.cloud_guest.entitys.pojo.UidInfoConfig;
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
import java.util.*;
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

    private static final List<DbScript> DB_SCRIPT_LIST = new ArrayList<>();

    record DbScript(String dbType, String scriptFileName, List<String> scriptSqlList) {
    }

    static {

        DB_SCRIPT_LIST.add(
                new DbScript("SQLite",
                        "classpath:sql/sqlite.sql",
                        List.of(
                                String.format("ALTER TABLE %s ADD COLUMN %s INTEGER DEFAULT 0", AutoPlanConfig.TABLE_NAME, AutoPlanConfig.COL_RECORD),
                                String.format("ALTER TABLE %s ADD COLUMN %s TEXT DEFAULT NULL", UidInfoConfig.TABLE_NAME, UidInfoConfig.COL_USERNAME),
                                String.format("ALTER TABLE %s ADD COLUMN %s TEXT DEFAULT NULL", UidInfoConfig.TABLE_NAME, UidInfoConfig.COL_PASSWORD),
                                String.format("ALTER TABLE %s ADD COLUMN %s TEXT DEFAULT NULL", UidInfoConfig.TABLE_NAME, UidInfoConfig.COL_SALT),
                                StrUtil.EMPTY
                        )
                )
        );

        DB_SCRIPT_LIST.add(
                new DbScript("PostgreSQL",
                        "classpath:sql/pgsql.sql",
                        List.of(
                                String.format("ALTER TABLE %s ADD COLUMN %s BOOLEAN DEFAULT NULL", AutoPlanConfig.TABLE_NAME, AutoPlanConfig.COL_RECORD),
                                String.format("COMMENT ON COLUMN %s.%s IS '是否记录'", AutoPlanConfig.TABLE_NAME, AutoPlanConfig.COL_RECORD),
                                String.format("ALTER TABLE %s ADD COLUMN %s VARCHAR(255) DEFAULT NULL", UidInfoConfig.TABLE_NAME, UidInfoConfig.COL_USERNAME),
                                String.format("COMMENT ON COLUMN %s.%s IS '用户名'", UidInfoConfig.TABLE_NAME, UidInfoConfig.COL_USERNAME),
                                String.format("ALTER TABLE %s ADD COLUMN %s VARCHAR(255) DEFAULT NULL", UidInfoConfig.TABLE_NAME, UidInfoConfig.COL_PASSWORD),
                                String.format("COMMENT ON COLUMN %s.%s IS '密码'", UidInfoConfig.TABLE_NAME, UidInfoConfig.COL_PASSWORD),
                                String.format("ALTER TABLE %s ADD COLUMN %s VARCHAR(255) DEFAULT NULL", UidInfoConfig.TABLE_NAME, UidInfoConfig.COL_SALT),
                                String.format("COMMENT ON COLUMN %s.%s IS '盐值'", UidInfoConfig.TABLE_NAME, UidInfoConfig.COL_SALT),
                                StrUtil.EMPTY
                        )
                )
        );

        DB_SCRIPT_LIST.add(
                new DbScript("MySQL",
                        "classpath:sql/mysql.sql",
                        List.of(
                                String.format("ALTER TABLE %s ADD COLUMN `%s` TINYINT(1) DEFAULT NULL COMMENT '是否记录' AFTER `remark`", AutoPlanConfig.TABLE_NAME, AutoPlanConfig.COL_RECORD),
                                String.format("ALTER TABLE %s ADD COLUMN `%s` varchar(255) DEFAULT NULL COMMENT '用户名' AFTER `remark`", UidInfoConfig.TABLE_NAME, UidInfoConfig.COL_USERNAME),
                                String.format("ALTER TABLE %s ADD COLUMN `%s` varchar(255) DEFAULT NULL COMMENT '密码' AFTER `remark`", UidInfoConfig.TABLE_NAME, UidInfoConfig.COL_PASSWORD),
                                String.format("ALTER TABLE %s ADD COLUMN `%s` varchar(255) DEFAULT NULL COMMENT '盐值' AFTER `remark`", UidInfoConfig.TABLE_NAME, UidInfoConfig.COL_SALT),
                                StrUtil.EMPTY
                        )
                )
        );
    }

    public DatabaseInitRunner(DataSource dataSource, ResourceLoader resourceLoader, Scheduler scheduler, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.resourceLoader = resourceLoader;
        this.scheduler = scheduler;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 检查异常消息中是否包含指定的关键字，以判断是否为列已存在的错误
     *
     * @param e    捕获的异常对象
     * @param keys 需要检查的关键字集合
     * @return 如果异常消息中包含任意一个关键字，则返回true，表示列已存在；否则返回false
     */
    private static boolean isColumnAlreadyExistsError(Throwable e, Collection<String> keys) {
        // 遍历异常链，直到异常为null
        while (e != null) {
            // 获取异常消息
            String msg = e.getMessage();
            // 如果异常消息不为null
            if (msg != null) {
                // 遍历所有关键字
                for (String key : keys) {
                    // 检查异常消息中是否包含当前关键字（不区分大小写）
                    if (msg.toLowerCase().contains(key.toLowerCase())) {
                        return true;
                    }
                }
            }
            e = e.getCause();
        }
        return false;
    }

    @PostConstruct
    public void init() {
        // 1. 检测数据库类型并执行脚本
        String dbType = detectDatabaseType();
        if (dbType != null) {
            //log.info("数据库类型：{}", dbType);

            DbScript dbScript = DB_SCRIPT_LIST.stream()
                    .filter(script -> script.dbType().equals(dbType))
                    .findFirst()
                    .orElse(null);

            if (dbScript != null) {
                String location = dbScript.scriptFileName();
                Resource resource = resourceLoader.getResource(location);

                if (resource.exists()) {
                    log.info("开始执行数据库脚本：{}", location);
                    try (Connection connection = dataSource.getConnection()) {
                        ScriptUtils.executeSqlScript(connection, resource);
                        log.info("脚本执行完成：{}", location);
                    } catch (Exception e) {
                        log.error("执行数据库脚本失败：{}", location, e);
                    }
                } else {
                    log.info("脚本文件 {} 不存在，跳过执行", location);
                }

                List<String> sqlList = dbScript.scriptSqlList();
                for (String sql : sqlList) {
                    if (StrUtil.isBlank(sql)) {
                        //log.warn("SQL 语句为空，跳过执行");
                        continue;
                    }
                    try {
                        jdbcTemplate.execute(sql);
                    } catch (Exception e) {
                        String msg = e.getMessage();
                        if (isColumnAlreadyExistsError(e, List.of("duplicate column", "Duplicate column", "duplicate column name", "already exists", "column already exists"))) {
                            log.debug("字段已存在，跳过添加, {}", msg);
                        } else {
                            log.warn("执行迁移脚本失败: {}", sql, e);
                        }
                    }
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


    /**
     * 检测数据库类型的方法
     * 通过获取数据库连接的元数据信息，提取数据库名称，并与预定义的数据库类型列表进行匹配
     *
     * @return String 返回匹配到的数据库类型，如果无法确定则返回null
     */
    private String detectDatabaseType() {
        // 尝试获取数据库连接
        try (Connection connection = dataSource.getConnection()) {
            // 获取数据库产品名称
            String productName = connection.getMetaData().getDatabaseProductName();
            // 记录调试信息，输出检测到的数据库产品名
            log.debug("检测到数据库产品名：{}", productName);
            // 如果产品名不为空，则进行后续处理
            if (productName != null) {
                // 将产品名转换为小写，以便进行不区分大小写的比较
                String lower = productName.toLowerCase();
                // 遍历预定义的数据库脚本列表
                for (DbScript dbScript : DB_SCRIPT_LIST) {
                    // 检查当前数据库产品名是否包含数据库类型标识
                    if (lower.contains(dbScript.dbType().toLowerCase())) {
                        // 如果匹配成功，返回对应的数据库类型
                        return dbScript.dbType();
                    }
                }
            }
        } catch (Exception e) {
            // 捕获并记录可能发生的异常
            log.error("获取数据库连接失败", e);
        }
        // 如果无法确定数据库类型，返回null
        return null;
    }

}