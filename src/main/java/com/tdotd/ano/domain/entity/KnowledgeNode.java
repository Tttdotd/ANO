package com.tdotd.ano.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ano_knowledge_node")
public class KnowledgeNode {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("source_task_id")
    private String sourceTaskId;

    @TableField("node_type")
    private String nodeType;

    @TableField("target")
    private String target;

    private String content;

    private byte[] vector;
}
