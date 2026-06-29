-- 自动执行计划配置表
CREATE TABLE IF NOT EXISTS auto_plan_config (
    id                     INTEGER PRIMARY KEY AUTOINCREMENT,  -- 主键ID，自增
    uid                    TEXT,                                 -- 用户唯一标识
    col_order                INTEGER,                             -- 排序（order 是保留字，用双引号）
    days                   TEXT,                                 -- 执行日期（逗号分隔）
    day_name               TEXT,                                 -- 日期名称
    selected_type          TEXT,                                 -- 选中类型
    run_type               TEXT,                                 -- 运行类型
    enable                 INTEGER DEFAULT 0,                   -- 是否启用（0/1）
    record                 INTEGER DEFAULT 0,
    auto_fight             TEXT,                                -- 秘境配置
    auto_ley_line_outcrop  TEXT,                                -- 自动地脉花配置
    auto_stygian_onslaught TEXT,                                -- 自动幽境配置
    auto_boss              TEXT,                                -- 自动Boss配置
    create_by              TEXT,                                -- 创建者
    create_time            TEXT DEFAULT (datetime('now','localtime')), -- 创建时间
    update_by              TEXT,                                -- 更新者
    update_time            TEXT DEFAULT (datetime('now','localtime')), -- 更新时间
    remark                 TEXT                                 -- 备注
    );

-- WebSocket代理接入配置表
CREATE TABLE IF NOT EXISTS ws_proxy_access_config (
          uid         TEXT PRIMARY KEY,                               -- 主键（用户标识）
          action      TEXT,                                           -- 操作类型
          ws_url      TEXT,                                           -- WebSocket地址
          proxy_url   TEXT,                                           -- WebSocket代理地址
          ws_token    TEXT,                                           -- 授权Token
          at_list     TEXT,                                           -- AT列表
          user_id     TEXT,                                           -- 用户ID
          group_id    TEXT,                                           -- 群ID
          create_by   TEXT,
          create_time TEXT DEFAULT (datetime('now','localtime')),
          update_by   TEXT,
          update_time TEXT DEFAULT (datetime('now','localtime')),
          remark      TEXT
    );


CREATE TABLE IF NOT EXISTS auto_plan_uid_global_config
(
    uid         TEXT PRIMARY KEY,
    cultivate   INTEGER DEFAULT 0,
    create_by   TEXT,
    create_time TEXT DEFAULT (datetime('now','localtime')),
    update_by   TEXT,
    update_time TEXT DEFAULT (datetime('now','localtime')),
    remark      TEXT
);

-- UID信息配置表
CREATE TABLE IF NOT EXISTS uid_info_config (
    uid         TEXT PRIMARY KEY,                               -- 用户唯一标识
    col_as        TEXT,                                           -- AS字段（as 是保留字，需用双引号）
    username   TEXT,
    password    TEXT,
    salt        TEXT,
    create_by   TEXT,
    create_time TEXT DEFAULT (datetime('now','localtime')),
    update_by   TEXT,
    update_time TEXT DEFAULT (datetime('now','localtime')),
    remark      TEXT
    );

-- 通用键值对存储表
CREATE TABLE IF NOT EXISTS db_kv (
                                     id          INTEGER PRIMARY KEY AUTOINCREMENT,              -- 主键ID，自增
                                     type        TEXT,                                           -- 键值类型
                                     key_name         TEXT NOT NULL,                                  -- 键名
                                     value       TEXT,                                           -- 键值
                                     create_by   TEXT,
                                     create_time TEXT DEFAULT (datetime('now','localtime')),
                                     update_by   TEXT,
                                     update_time TEXT DEFAULT (datetime('now','localtime')),
                                     remark      TEXT
                                    );

-- 为 db_kv 创建唯一索引（type, key_name）
CREATE UNIQUE INDEX IF NOT EXISTS uk_type_key ON db_kv (type, key_name);

-- ... existing code ...

-- 定时任务调度表
CREATE TABLE IF NOT EXISTS sys_job (
                                       job_id          INTEGER PRIMARY KEY AUTOINCREMENT,
                                       job_name        TEXT,
                                       job_group       TEXT,
                                       invoke_target   TEXT NOT NULL,
                                       cron_expression TEXT,
                                       misfire_policy  TEXT DEFAULT '3',
                                       concurrent      TEXT DEFAULT '1',
                                       status          TEXT DEFAULT '0',
                                       create_by       TEXT,
                                       create_time     TEXT DEFAULT (datetime('now','localtime')),
                                       update_by       TEXT,
                                       update_time     TEXT DEFAULT (datetime('now','localtime')),
                                       remark          TEXT
);

CREATE INDEX IF NOT EXISTS idx_job_group_status ON sys_job (job_group, status);
CREATE INDEX IF NOT EXISTS idx_job_name ON sys_job (job_name);

-- ... existing code ...

-- 备份信息表
CREATE TABLE IF NOT EXISTS backup_info (
                                           id            INTEGER PRIMARY KEY AUTOINCREMENT,
                                           backup_name   TEXT,
                                           backup_path   TEXT,
                                           backup_json   TEXT,
                                           backup_time   TEXT DEFAULT (datetime('now','localtime')),
                                           backup_size   INTEGER,
                                           create_by     TEXT,
                                           create_time   TEXT DEFAULT (datetime('now','localtime')),
                                           update_by     TEXT,
                                           update_time   TEXT DEFAULT (datetime('now','localtime')),
                                           remark        TEXT
);
