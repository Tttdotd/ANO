package com.tdotd.ano.domain.vo;

import java.time.LocalDateTime;

public record TaskCreateVo(
        String id,
        String title,
        Integer state,
        LocalDateTime createTime
) {
}
