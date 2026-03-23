package com.tdotd.ano.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ano_task")
public class Task {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField("user_id")
    private String userId;

    private String title;

    private String description;

    private Integer state;

    private Integer version;

    @TableField("parent_id")
    private String parentId;

    @TableField("iteration_note")
    private String iterationNote;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("archived_time")
    private LocalDateTime archivedTime;

    @TableLogic
    private Integer isDeleted;
}
