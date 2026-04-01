package com.tdotd.ano.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ano_knowledge_lineage")
public class KnowledgeLineage {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("child_node_id")
    private String childNodeId;

    @TableField("parent_a_id")
    private String parentAId;

    @TableField("parent_b_id")
    private String parentBId;

    @TableField("create_time")
    private LocalDateTime createTime;
}
