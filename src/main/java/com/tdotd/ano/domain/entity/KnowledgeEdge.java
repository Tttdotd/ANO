package com.tdotd.ano.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ano_knowledge_edge")
public class KnowledgeEdge {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("from_node_id")
    private String fromNodeId;

    @TableField("to_node_id")
    private String toNodeId;

    @TableField("relation_type")
    private String relationType;

    @TableField("create_time")
    private LocalDateTime createTime;
}
