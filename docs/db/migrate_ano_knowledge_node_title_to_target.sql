-- 已有库迁移：将知识节点表 title 列重命名为 target
-- 执行前请备份；执行后需重建 RediSearch 索引 idx:knowledge（字段名由 title 改为 target）

USE ano_pkm;

ALTER TABLE `ano_knowledge_node`
    CHANGE COLUMN `title` `target` VARCHAR(255) NOT NULL COMMENT '知识的对象/目标, 用于说明这是关于什么的知识, 由AI自动判断生成';
