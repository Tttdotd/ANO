package com.tdotd.ano.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskCreateDto(
        @NotBlank(message = "任务标题不能为空")
        @Size(max = 255, message = "标题过长")
        String title,
        @Size(max = 8000, message = "描述过长")
        String description
) {
}
