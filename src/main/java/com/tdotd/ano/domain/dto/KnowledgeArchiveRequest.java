package com.tdotd.ano.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record KnowledgeArchiveRequest(
        @NotBlank(message = "任务ID不能为空")
        String taskId
) {
}
