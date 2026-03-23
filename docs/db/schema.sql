CREATE DATABASE IF NOT EXISTS ano_pkm CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ano_pkm;

-- 1. 任务主表 (Action)
CREATE TABLE `ano_task` (
                            `id` BIGINT UNSIGNED NOT NULL COMMENT '分布式唯一ID',
                            `user_id` BIGINT UNSIGNED NOT NULL COMMENT '所属用户ID',
                            `title` VARCHAR(255) NOT NULL COMMENT '任务标题',
                            `description` TEXT COMMENT '任务详细描述',
                            `state` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-Todo, 1-Doing, 2-Noted, 3-Done, 4-Archived',
                            `version` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '版本号',
                            `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父任务ID(版本迭代溯源)',
                            `iteration_note` VARCHAR(500) DEFAULT NULL COMMENT '本次迭代的改良动机',
                            `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                            `archived_time` DATETIME DEFAULT NULL COMMENT '最终归档时间',
                            `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否, 1-是',
                            PRIMARY KEY (`id`),
                            INDEX `idx_user_state` (`user_id`, `state`),
                            INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务主表-行动核心';

-- 2. 笔记表 (Note)
CREATE TABLE `ano_note` (
                            `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
                            `task_id` BIGINT UNSIGNED NOT NULL COMMENT '关联任务ID',
                            `content` LONGTEXT COMMENT 'Markdown笔记内容',
                            `state` TINYINT NOT NULL DEFAULT 0 COMMENT '笔记状态: 0-草稿, 1-完成',
                            `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否, 1-是',
                            PRIMARY KEY (`id`),
                            UNIQUE INDEX `uk_task_id` (`task_id`) COMMENT '一个任务在当前版本只能有一个核心笔记'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务关联笔记-思考沉淀';

-- 3. 产出物表 (Output)
CREATE TABLE `ano_output` (
                              `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
                              `task_id` BIGINT UNSIGNED NOT NULL COMMENT '关联任务ID',
                              `url` VARCHAR(1024) NOT NULL COMMENT '成果链接(知乎/B站/GitHub等)',
                              `platform` VARCHAR(50) DEFAULT NULL COMMENT '发布平台名称',
                              `state` TINYINT NOT NULL DEFAULT 0 COMMENT '产出状态: 0-链接有效, 1-失效',
                              `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否, 1-是',
                              PRIMARY KEY (`id`),
                              INDEX `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务产出物-价值证明';