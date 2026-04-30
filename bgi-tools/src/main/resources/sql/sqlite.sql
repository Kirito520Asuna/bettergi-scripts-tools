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
    auto_fight             TEXT,                                -- 秘境配置
    auto_ley_line_outcrop  TEXT,                                -- 自动地脉花配置
    auto_stygian_onslaught TEXT,                                -- 自动幽境配置
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

-- UID信息配置表
CREATE TABLE IF NOT EXISTS uid_info_config (
    uid         TEXT PRIMARY KEY,                               -- 用户唯一标识
    col_as        TEXT,                                           -- AS字段（as 是保留字，需用双引号）
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