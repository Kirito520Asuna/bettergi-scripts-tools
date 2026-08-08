package com.cloud_guest.runner;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.entitys.pojo.AutoPlanConfig;
import com.cloud_guest.entitys.pojo.UidInfoConfig;
import com.google.common.collect.Maps;
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

    /**
     * 数据库脚本
     * @param dbType 数据库类型
     * @param format 列脚本格式
     * @param formatSize 列脚本格式个数
     * @param remarkFormat 列注释格式
     * @param remarkFormatSize 列注释格式个数
     */
    record SqlFormat(String dbType, String format, int formatSize, String remarkFormat, int remarkFormatSize) {
    }

    /**
     * 数据库脚本
     * @param dbType 数据库类型
     * @param scriptFileName 脚本文件名
     * @param scriptSqlList 脚本
     */
    record DbScript(String dbType, String scriptFileName, List<ColumnSql> scriptSqlList) {
    }

    /**
     * 数据库脚本
     * @param table 表名
     * @param column 列名
     * @param remark 列注释
     * @param sql 列脚本
     */
    record ColumnSql(String table, String column, String remark, String sql) {
    }

    /**
     * 数据库表脚本
     * @param table 表名
     * @param columns 列
     */
    record SqlTable(String table, List<SqlColumn> columns) {
    }

    /**
     * 数据库表脚本
     * @param column 列名
     * @param remark 列注释
     * @param types 数据库列类型
     */
    record SqlColumn(String column, String remark, List<DbSqlType> types) {
    }

    /**
     * 数据库列类型
     * @param db 数据库类型
     * @param type 列类型
     * @param columnDefault 列默认值
     */
    record DbSqlType(String db, String type, String columnDefault) {
    }

    static {
        String SQLite = "SQLite", MySQL = "MySQL", PostgreSQL = "PostgreSQL";

        SqlFormat SQLiteFormat = new SqlFormat(SQLite, "ALTER TABLE %s ADD COLUMN %s %s DEFAULT %s", 4, StrUtil.EMPTY, 0),
                MySQLFormat = new SqlFormat(MySQL, "ALTER TABLE %s ADD COLUMN %s %s DEFAULT %s COMMENT '%s' AFTER `remark`", 5, StrUtil.EMPTY, 0),
                PostgreSQLFormat = new SqlFormat(PostgreSQL, "ALTER TABLE %s ADD COLUMN %s %s DEFAULT %s", 4, "COMMENT ON COLUMN %s.%s IS '%s'", 3);

        Map<String, SqlFormat> SqlFormatMap = Maps.newLinkedHashMap();

        SqlFormatMap.put(SQLite, SQLiteFormat);
        SqlFormatMap.put(MySQL, MySQLFormat);
        SqlFormatMap.put(PostgreSQL, PostgreSQLFormat);
        //=================================================================================================================
        List<SqlTable> sqlTableList = CollUtil.newArrayList();
        //uid
        SqlTable uidSQL = new SqlTable(UidInfoConfig.TABLE_NAME,
                List.of(
                        new SqlColumn(
                                UidInfoConfig.COL_USERNAME, UidInfoConfig.REMARK_COL_USERNAME,
                                List.of(
                                        new DbSqlType(SQLite, "TEXT", "NULL"),
                                        new DbSqlType(MySQL, "VARCHAR(255)", "NULL"),
                                        new DbSqlType(PostgreSQL, "VARCHAR(255)", "NULL")
                                )
                        ),
                        new SqlColumn(
                                UidInfoConfig.COL_PASSWORD, UidInfoConfig.REMARK_COL_PASSWORD,
                                List.of(
                                        new DbSqlType(SQLite, "TEXT", "NULL"),
                                        new DbSqlType(MySQL, "VARCHAR(255)", "NULL"),
                                        new DbSqlType(PostgreSQL, "VARCHAR(255)", "NULL")
                                )
                        ),
                        new SqlColumn(
                                UidInfoConfig.COL_SALT, UidInfoConfig.REMARK_COL_SALT,
                                List.of(
                                        new DbSqlType(SQLite, "TEXT", "NULL"),
                                        new DbSqlType(MySQL, "VARCHAR(255)", "NULL"),
                                        new DbSqlType(PostgreSQL, "VARCHAR(255)", "NULL")
                                )
                        )
                )
        ),
                autoPlanSQL = new SqlTable(
                        AutoPlanConfig.TABLE_NAME,
                        List.of(
                                new SqlColumn(
                                        AutoPlanConfig.COL_AUTO_BOSS, AutoPlanConfig.REMARK_COL_AUTO_BOSS,
                                        List.of(
                                                new DbSqlType(SQLite, "TEXT", "NULL"),
                                                new DbSqlType(MySQL, "TEXT", "NULL"),
                                                new DbSqlType(PostgreSQL, "TEXT", "NULL")
                                        )
                                ),
                                new SqlColumn(
                                        AutoPlanConfig.COL_RECORD, AutoPlanConfig.REMARK_COL_RECORD,
                                        List.of(
                                                new DbSqlType(SQLite, "INTEGER", "0"),
                                                new DbSqlType(MySQL, "TINYINT(1)", "1"),
                                                new DbSqlType(PostgreSQL, "BOOLEAN", "false")
                                        )
                                ),
                                new SqlColumn(
                                        AutoPlanConfig.COL_CULTIVATE, AutoPlanConfig.REMARK_COL_CULTIVATE,
                                        List.of(
                                                new DbSqlType(SQLite, "INTEGER", "0"),
                                                new DbSqlType(MySQL, "TINYINT(1)", "0"),
                                                new DbSqlType(PostgreSQL, "BOOLEAN", "false")
                                        )
                                )
                        )
                );
        sqlTableList.add(uidSQL);
        sqlTableList.add(autoPlanSQL);
        //=================================================================================================================
        Map<String, List<ColumnSql>> SqlScriptMap = Maps.newLinkedHashMap();
        List<ColumnSql> SQLiteScripts = CollUtil.newArrayList(),
                MySQLScripts = CollUtil.newArrayList(),
                PostgreSQLScripts = CollUtil.newArrayList();
        SqlScriptMap.put(SQLite, SQLiteScripts);
        SqlScriptMap.put(MySQL, MySQLScripts);
        SqlScriptMap.put(PostgreSQL, PostgreSQLScripts);
        //=================================================================================================================
        sqlTableList.forEach(sqlTable -> {
            String table = sqlTable.table;
            sqlTable.columns.forEach(sqlColumn -> {
                String column = sqlColumn.column;
                String remark = sqlColumn.remark;
                List<DbSqlType> types = sqlColumn.types;
                types.stream().forEach(type -> {
                    String db = type.db,
                            typeName = type.type,
                            columnDefault = type.columnDefault;
                    SqlFormat sqlFormat = SqlFormatMap.get(db);

                    String format = sqlFormat.format, remarkFormat = sqlFormat.remarkFormat;
                    int formatSize = sqlFormat.formatSize, remarkFormatSize = sqlFormat.remarkFormatSize;

                    List<String> list = CollUtil.newArrayList(table, column, typeName);
                    if (formatSize == 5) {
                        // MySQL 的格式需要 5 个参数：表名, 列名, 类型, DEFAULT值, COMMENT值
                        list.add(columnDefault); // ✅ 第4个参数：DEFAULT 值
                        list.add(remark);       // ✅ 第5个参数：COMMENT 内容
                    } else {
                        // SQLite、PostgreSQL 等格式只有 4 个参数，不需要 COMMENT，直接放 columnDefault
                        list.add(columnDefault);
                    }

                    List<ColumnSql> sqlList = CollUtil.newArrayList();
                    String sql = String.format(format, list.toArray(new String[formatSize]));
                    sqlList.add(new ColumnSql(table, column, remark, sql));
                    // 如果该数据库支持 COMMENT ON 语句（如 PostgreSQL）
                    if (remarkFormatSize > 0 && !StrUtil.isBlankIfStr(remarkFormat)) {
                        String remarkSql = String.format(remarkFormat, table, column, remark);
                        sqlList.add(new ColumnSql(table, column, remark, remarkSql));
                    }
                    List<ColumnSql> SqlScriptList = SqlScriptMap.get(db);
                    SqlScriptList.addAll(sqlList);
                    //SqlScriptList.add(StrUtil.EMPTY);
                    SqlScriptMap.put(db, SqlScriptList);
                });
            });
        });
        //=================================================================================================================


        DB_SCRIPT_LIST.add(
                new DbScript(SQLite, "classpath:sql/sqlite.sql", SQLiteScripts)
        );

        DB_SCRIPT_LIST.add(
                new DbScript(PostgreSQL, "classpath:sql/pgsql.sql", PostgreSQLScripts)
        );

        DB_SCRIPT_LIST.add(
                new DbScript(MySQL, "classpath:sql/mysql.sql", MySQLScripts)
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
        log.info("====================================");
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
                List<ColumnSql> errorList = CollUtil.newArrayList();
                List<ColumnSql> sqlList = dbScript.scriptSqlList();
                log.info("正在添加字段：{} Size", sqlList.size());
                for (ColumnSql sql : sqlList) {
                    if (StrUtil.isBlank(sql.sql)) {
                        //log.warn("SQL 语句为空，跳过执行");
                        continue;
                    }
                    try {
                        log.info("[添加字段] `{}.{},备注:{}`", sql.table, sql.column,sql.remark);
                        jdbcTemplate.execute(sql.sql);
                        //log.info("[字段添加成功] `{}.{}`", sql.table, sql.column);
                    } catch (Exception e) {
                        String msg = e.getMessage();
                        if (isColumnAlreadyExistsError(e, List.of("duplicate column", "Duplicate column", "duplicate column name", "already exists", "column already exists"))) {
                            //log.warn("[字段存在]`{}.{}`字段已存在，跳过添加", sql.table, sql.column);
                            errorList.add(sql);
                            //log.debug("{}", msg);
                        } else {
                            log.warn("执行迁移脚本失败: {}", sql.sql, e);
                        }
                    }
                }
                if (errorList.size() != sqlList.size()) {
                    log.info("====================================");
                }
                sqlList.stream().filter(sql -> !errorList.contains(sql)).forEach(sql -> log.info("[字段添加成功] `{}.{}`,备注:{}", sql.table, sql.column,sql.remark));
                if (errorList.size() != sqlList.size() || CollUtil.isNotEmpty(errorList)) {
                    log.info("====================================");
                }
                errorList.stream().forEach(sql -> log.warn("[字段存在] `{}.{}`字段已存在，跳过添加 {}", sql.table, sql.column,sql.remark));
                log.info("====================================");
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