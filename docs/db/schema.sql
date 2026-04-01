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

-- 4. 知识节点表
CREATE TABLE `ano_knowledge_node` (
                                      `id` varchar(32) NOT NULL,
                                      `source_task_id` varchar(32) DEFAULT NULL COMMENT '来源任务ID，任务提炼节点必填，涌现节点可空',
                                      `node_type` varchar(32) NOT NULL DEFAULT 'TASK_EXTRACTED' COMMENT '节点类型: TASK_EXTRACTED/EMERGENT',
                                      `title` varchar(255) NOT NULL,         -- AI生成的标题，方便在列表展示
                                      `content` text NOT NULL,               -- 知识点核心，用于语义搜索
                                      `vector` blob NOT NULL,                -- 灵魂字段，没有它做不了向量搜索
                                      PRIMARY KEY (`id`),
                                      INDEX `idx_source_task_id` (`source_task_id`),
                                      INDEX `idx_node_type` (`node_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 知识关系边表
CREATE TABLE `ano_knowledge_edge` (
                                      `id` varchar(32) NOT NULL COMMENT '关系边ID',
                                      `from_node_id` varchar(32) NOT NULL COMMENT '起点知识节点ID',
                                      `to_node_id` varchar(32) NOT NULL COMMENT '终点知识节点ID',
                                      `relation_type` varchar(32) NOT NULL COMMENT '关系类型: SUPPORTS/CONTRADICTS/SIMILAR_TO/CAUSES/PREREQUISITE_OF/TRADEOFF_WITH',
                                      `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      PRIMARY KEY (`id`),
                                      INDEX `idx_from_node` (`from_node_id`),
                                      INDEX `idx_to_node` (`to_node_id`),
                                      INDEX `idx_relation_type` (`relation_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识关系边';

-- 6. 知识涌现血缘表
CREATE TABLE `ano_knowledge_lineage` (
                                         `id` varchar(32) NOT NULL COMMENT '血缘记录ID',
                                         `child_node_id` varchar(32) NOT NULL COMMENT '涌现节点ID',
                                         `parent_a_id` varchar(32) NOT NULL COMMENT '父知识节点A',
                                         `parent_b_id` varchar(32) NOT NULL COMMENT '父知识节点B',
                                         `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         PRIMARY KEY (`id`),
                                         INDEX `idx_child_node` (`child_node_id`),
                                         INDEX `idx_parent_a` (`parent_a_id`),
                                         INDEX `idx_parent_b` (`parent_b_id`),
                                         UNIQUE INDEX `uk_child_pair` (`child_node_id`, `parent_a_id`, `parent_b_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识涌现血缘关系';