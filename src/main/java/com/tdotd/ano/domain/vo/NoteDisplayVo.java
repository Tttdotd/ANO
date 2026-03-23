package com.tdotd.ano.domain.vo;

public record NoteDisplayVo(
        String id,
        String content,
        String taskId,
        Integer state
) {
}
