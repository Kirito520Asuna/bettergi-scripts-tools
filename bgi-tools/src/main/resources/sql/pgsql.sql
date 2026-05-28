-- =========================================================
-- 表 1: auto_plan_config
-- =========================================================
CREATE TABLE IF NOT EXISTS auto_plan_config (
    id                     BIGSERIAL    PRIMARY KEY,                     -- 自增主键
    uid                    VARCHAR(64),
    col_order              INT,
    days                   VARCHAR(255),
    day_name               VARCHAR(255),
    selected_type          VARCHAR(255),
    run_type               VARCHAR(255),
    enable                 BOOLEAN,                                     -- TINYINT(1) → BOOLEAN
    record                 BOOLEAN,                                     -- TINYINT(1) → BOOLEAN
    auto_fight             TEXT,
    auto_ley_line_outcrop  TEXT,
    auto_stygian_onslaught TEXT,
    -- 通用审计字段
    create_by              VARCHAR(64),
    create_time            TIMESTAMP,
    update_by              VARCHAR(64),
    update_time            TIMESTAMP,
    remark                 TEXT
    );

-- 添加表注释
COMMENT ON TABLE auto_plan_config IS '自动执行计划配置表';

-- 添加列注释（保留原有中文注释）
COMMENT ON COLUMN auto_plan_config.id                     IS '主键ID';
COMMENT ON COLUMN auto_plan_config.uid                    IS '用户唯一标识';
COMMENT ON COLUMN auto_plan_config.col_order              IS '排序';
COMMENT ON COLUMN auto_plan_config.days                   IS '执行日期（逗号分隔）';
COMMENT ON COLUMN auto_plan_config.day_name               IS '日期名称';
COMMENT ON COLUMN auto_plan_config.selected_type          IS '选中类型';
COMMENT ON COLUMN auto_plan_config.run_type               IS '运行类型';
COMMENT ON COLUMN auto_plan_config.enable                 IS '是否启用';
COMMENT ON COLUMN auto_plan_config.record                 IS '是否记录';
COMMENT ON COLUMN auto_plan_config.auto_fight             IS '秘境配置';
COMMENT ON COLUMN auto_plan_config.auto_ley_line_outcrop  IS '自动地脉花配置';
COMMENT ON COLUMN auto_plan_config.auto_stygian_onslaught IS '自动幽境配置';
COMMENT ON COLUMN auto_plan_config.create_by              IS '创建者';
COMMENT ON COLUMN auto_plan_config.create_time            IS '创建时间';
COMMENT ON COLUMN auto_plan_config.update_by              IS '更新者';
COMMENT ON COLUMN auto_plan_config.update_time            IS '更新时间';
COMMENT ON COLUMN auto_plan_config.remark                 IS '备注';

-- =========================================================
-- 表 2: ws_proxy_access_config
-- =========================================================
CREATE TABLE IF NOT EXISTS ws_proxy_access_config (
    uid         VARCHAR(64)  NOT NULL PRIMARY KEY,           -- 主键（用户标识）
    action      VARCHAR(64),
    ws_url      VARCHAR(500),
    proxy_url   VARCHAR(500),
    ws_token    VARCHAR(255),
    at_list     VARCHAR(500),
    user_id     VARCHAR(64),
    group_id    VARCHAR(64),
    -- 通用审计字段
    create_by   VARCHAR(64),
    create_time TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP,
    remark      TEXT
    );

COMMENT ON TABLE ws_proxy_access_config IS 'WebSocket代理接入配置表';

COMMENT ON COLUMN ws_proxy_access_config.uid         IS '主键（用户标识）';
COMMENT ON COLUMN ws_proxy_access_config.action      IS '操作类型';
COMMENT ON COLUMN ws_proxy_access_config.ws_url      IS 'WebSocket地址';
COMMENT ON COLUMN ws_proxy_access_config.proxy_url   IS 'WebSocket代理地址';
COMMENT ON COLUMN ws_proxy_access_config.ws_token    IS '授权Token';
COMMENT ON COLUMN ws_proxy_access_config.at_list     IS 'AT列表';
COMMENT ON COLUMN ws_proxy_access_config.user_id     IS '用户ID';
COMMENT ON COLUMN ws_proxy_access_config.group_id    IS '群ID';
COMMENT ON COLUMN ws_proxy_access_config.create_by   IS '创建者';
COMMENT ON COLUMN ws_proxy_access_config.create_time IS '创建时间';
COMMENT ON COLUMN ws_proxy_access_config.update_by   IS '更新者';
COMMENT ON COLUMN ws_proxy_access_config.update_time IS '更新时间';
COMMENT ON COLUMN ws_proxy_access_config.remark      IS '备注';

-- =========================================================
-- 表 3: uid_info_config
-- =========================================================
CREATE TABLE IF NOT EXISTS uid_info_config (
    uid         VARCHAR(64) NOT NULL PRIMARY KEY,
    col_as    VARCHAR(64),                                 -- 双引号转义保留字 as
-- 通用审计字段
    create_by   VARCHAR(64),
    create_time TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP,
    remark      TEXT
    );

COMMENT ON TABLE uid_info_config IS 'UID信息配置表';

COMMENT ON COLUMN uid_info_config.uid         IS '用户唯一标识';
COMMENT ON COLUMN uid_info_config.col_as     IS 'AS字段';
COMMENT ON COLUMN uid_info_config.create_by   IS '创建者';
COMMENT ON COLUMN uid_info_config.create_time IS '创建时间';
COMMENT ON COLUMN uid_info_config.update_by   IS '更新者';
COMMENT ON COLUMN uid_info_config.update_time IS '更新时间';
COMMENT ON COLUMN uid_info_config.remark      IS '备注';

-- =========================================================
-- 表 4: db_kv
-- =========================================================
CREATE TABLE IF NOT EXISTS db_kv (
                                     id          BIGSERIAL    PRIMARY KEY,
                                     type        VARCHAR(64),
    key_name         VARCHAR(128) NOT NULL,
    value       TEXT,
    -- 通用审计字段
    create_by   VARCHAR(64),
    create_time TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP,
    remark      TEXT,
    CONSTRAINT uk_type_key UNIQUE (type, key_name)                -- 唯一约束替代 MySQL 的 UNIQUE INDEX
    );

COMMENT ON TABLE db_kv IS '通用键值对存储表';

COMMENT ON COLUMN db_kv.id          IS '主键ID';
COMMENT ON COLUMN db_kv.type        IS '键值类型';
COMMENT ON COLUMN db_kv.key_name         IS '键名';
COMMENT ON COLUMN db_kv.value       IS '键值';
COMMENT ON COLUMN db_kv.create_by   IS '创建者';
COMMENT ON COLUMN db_kv.create_time IS '创建时间';
COMMENT ON COLUMN db_kv.update_by   IS '更新者';
COMMENT ON COLUMN db_kv.update_time IS '更新时间';
COMMENT ON COLUMN db_kv.remark      IS '备注';



-- =========================================================
-- 表 5: sys_job
-- =========================================================
CREATE TABLE IF NOT EXISTS sys_job (
                                       job_id          BIGINT       PRIMARY KEY,
                                       job_name        VARCHAR(255),
    job_group       VARCHAR(64),
    invoke_target   VARCHAR(500) NOT NULL,
    cron_expression VARCHAR(255),
    misfire_policy  CHAR(1)      DEFAULT '3',
    concurrent      CHAR(1)      DEFAULT '1',
    status          CHAR(1)      DEFAULT '0',
    create_by       VARCHAR(64),
    create_time     TIMESTAMP,
    update_by       VARCHAR(64),
    update_time     TIMESTAMP,
    remark          TEXT
    );

COMMENT ON TABLE sys_job IS '定时任务调度表';

COMMENT ON COLUMN sys_job.job_id           IS '任务ID';
COMMENT ON COLUMN sys_job.job_name         IS '任务名称';
COMMENT ON COLUMN sys_job.job_group        IS '任务组名';
COMMENT ON COLUMN sys_job.invoke_target    IS '调用目标字符串';
COMMENT ON COLUMN sys_job.cron_expression  IS 'cron执行表达式';
COMMENT ON COLUMN sys_job.misfire_policy   IS '计划执行错误策略（1立即执行 2执行一次 3放弃执行）';
COMMENT ON COLUMN sys_job.concurrent       IS '是否并发执行（0允许 1禁止）';
COMMENT ON COLUMN sys_job.status           IS '状态（0正常 1暂停）';
COMMENT ON COLUMN sys_job.create_by        IS '创建者';
COMMENT ON COLUMN sys_job.create_time      IS '创建时间';
COMMENT ON COLUMN sys_job.update_by        IS '更新者';
COMMENT ON COLUMN sys_job.update_time      IS '更新时间';
COMMENT ON COLUMN sys_job.remark           IS '备注';

CREATE INDEX IF NOT EXISTS idx_job_group_status ON sys_job (job_group, status);
CREATE INDEX IF NOT EXISTS idx_job_name ON sys_job (job_name);

-- ... existing code ...

-- =========================================================
-- 表 6: backup_info
-- =========================================================
CREATE TABLE IF NOT EXISTS backup_info (
                                           id            BIGINT       PRIMARY KEY,
                                           backup_name   VARCHAR(255),
                                           backup_path   VARCHAR(500),
                                           backup_json   TEXT,
                                           backup_time   TIMESTAMP,
                                           backup_size   BIGINT,
                                           create_by     VARCHAR(64),
                                           create_time   TIMESTAMP,
                                           update_by     VARCHAR(64),
                                           update_time   TIMESTAMP,
                                           remark        TEXT
);

COMMENT ON TABLE backup_info IS '备份信息表';

COMMENT ON COLUMN backup_info.id           IS '主键';
COMMENT ON COLUMN backup_info.backup_name  IS '备份名称';
COMMENT ON COLUMN backup_info.backup_path  IS '备份路径';
COMMENT ON COLUMN backup_info.backup_json  IS '备份信息';
COMMENT ON COLUMN backup_info.backup_time  IS '备份时间';
COMMENT ON COLUMN backup_info.backup_size  IS '备份大小';
COMMENT ON COLUMN backup_info.create_by    IS '创建者';
COMMENT ON COLUMN backup_info.create_time  IS '创建时间';
COMMENT ON COLUMN backup_info.update_by    IS '更新者';
COMMENT ON COLUMN backup_info.update_time  IS '更新时间';
COMMENT ON COLUMN backup_info.remark       IS '备注';
