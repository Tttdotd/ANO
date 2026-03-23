package com.tdotd.ano.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OutputCreateDto(
        @NotBlank(message = "关联任务不能为空")
        String taskId,
        @NotBlank(message = "平台不能为空")
        @Size(max = 50)
        String platform,
        @NotBlank(message = "链接不能为空")
        @Size(max = 1024)
        String url
) {
}
