package com.tdotd.ano.domain.vo;

import java.time.LocalDateTime;

public record TaskDisplayVo(
        String id,
        String title,
        String description,
        Integer state,
        Integer version,
        LocalDateTime createTime
) {
}
