package com.tdotd.ano.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskUpdateDto(
        @NotBlank(message = "任务ID不能为空")
        String id,
        @NotBlank(message = "任务标题不能为空")
        @Size(max = 255, message = "标题过长")
        String title,
        @NotNull(message = "任务描述不能为空")
        @Size(max = 8000, message = "描述过长")
        String description
) {
}
