package com.tdotd.ano.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NoteUpdateDto(
        @NotBlank(message = "笔记ID不能为空")
        String id,
        @NotBlank(message = "笔记内容不能为空")
        String content,
        @NotNull(message = "笔记状态不能为空")
        Integer state
) {
}
