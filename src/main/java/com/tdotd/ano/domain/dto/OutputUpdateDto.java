package com.tdotd.ano.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OutputUpdateDto(
        @NotBlank(message = "产出ID不能为空")
        String id,
        @NotBlank(message = "平台不能为空")
        @Size(max = 50)
        String platform,
        @NotBlank(message = "链接不能为空")
        @Size(max = 1024)
        String url
) {
}
