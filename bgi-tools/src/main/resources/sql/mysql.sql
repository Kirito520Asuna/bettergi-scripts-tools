CREATE TABLE IF NOT EXISTS `auto_plan_config` (
    `id`                     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `uid`                    VARCHAR(64)  DEFAULT NULL COMMENT '用户唯一标识',
    `col_order`                  INT          DEFAULT NULL COMMENT '排序',
    `days`                   VARCHAR(255) DEFAULT NULL COMMENT '执行日期（逗号分隔）',
    `day_name`               VARCHAR(255) DEFAULT NULL COMMENT '日期名称',
    `selected_type`          VARCHAR(255) DEFAULT NULL COMMENT '选中类型',
    `run_type`               VARCHAR(255) DEFAULT NULL COMMENT '运行类型',
    `enable`                 TINYINT(1)   DEFAULT NULL COMMENT '是否启用',
    `auto_fight`             TEXT         DEFAULT NULL COMMENT '秘境配置',
    `auto_ley_line_outcrop`  TEXT         DEFAULT NULL COMMENT '自动地脉花配置',
    `auto_stygian_onslaught` TEXT         DEFAULT NULL COMMENT '自动幽境配置',
    -- ↓ 通用审计字段 ↓
    `create_by`              VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`            TIMESTAMP     DEFAULT NULL COMMENT '创建时间',
    `update_by`              VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`            TIMESTAMP     DEFAULT NULL COMMENT '更新时间',
    `remark`                 TEXT         DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自动执行计划配置表';

CREATE TABLE IF NOT EXISTS `ws_proxy_access_config` (
    `uid`         VARCHAR(64)  NOT NULL COMMENT '主键（用户标识）',
    `action`      VARCHAR(64)  DEFAULT NULL COMMENT '操作类型',
    `ws_url`      VARCHAR(500) DEFAULT NULL COMMENT 'WebSocket地址',
    `proxy_url`   VARCHAR(500) DEFAULT NULL COMMENT 'WebSocket代理地址',
    `ws_token`    VARCHAR(255) DEFAULT NULL COMMENT '授权Token',
    `at_list`     VARCHAR(500) DEFAULT NULL COMMENT 'AT列表',
    `user_id`     VARCHAR(64)  DEFAULT NULL COMMENT '用户ID',
    `group_id`    VARCHAR(64)  DEFAULT NULL COMMENT '群ID',
    -- 通用审计字段
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time` TIMESTAMP     DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time` TIMESTAMP     DEFAULT NULL COMMENT '更新时间',
    `remark`      TEXT         DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`uid`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WebSocket代理接入配置表';

CREATE TABLE IF NOT EXISTS `uid_info_config`
(
    `uid`         varchar(64) NOT NULL COMMENT '用户唯一标识',
    `col_as`          varchar(64) DEFAULT NULL COMMENT 'AS字段（注意：as是保留字，需反引号）',
    -- 通用审计字段
    `create_by`   VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` TIMESTAMP   DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` TIMESTAMP   DEFAULT NULL COMMENT '更新时间',
    `remark`      TEXT        DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='UID信息配置表';

CREATE TABLE IF NOT EXISTS `db_kv` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type`        VARCHAR(64)  DEFAULT NULL COMMENT '键值类型',
    `key_name`         VARCHAR(128) NOT NULL COMMENT '键名',
    `value`       TEXT         DEFAULT NULL COMMENT '键值',
    -- 通用审计字段
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time` TIMESTAMP     DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time` TIMESTAMP     DEFAULT NULL COMMENT '更新时间',
    `remark`      TEXT         DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_type_key` (`type`, `key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用键值对存储表';