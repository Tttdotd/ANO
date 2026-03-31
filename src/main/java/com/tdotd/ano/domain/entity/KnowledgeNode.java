package com.tdotd.ano.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ano_knowledge_node")
public class KnowledgeNode {

    @TableId
    private String id;

    @TableField("source_task_id")
    private String sourceTaskId;

    private String title;

    private String content;

    private byte[] vector;
}
