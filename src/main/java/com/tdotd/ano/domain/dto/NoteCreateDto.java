package com.tdotd.ano.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record NoteCreateDto(
        @NotBlank(message = "关联任务不能为空")
        @JsonProperty("task_id")
        String taskId
) {
}
