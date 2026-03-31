package com.tdotd.ano.service;

import com.tdotd.ano.domain.dto.OutputCreateDto;
import com.tdotd.ano.domain.dto.OutputUpdateDto;
import com.tdotd.ano.domain.vo.OutputVo;

public interface OutputService {

    /**
     * 创建产出记录，返回新建产出 ID。
     */
    String createOutput(OutputCreateDto dto);

    /**
     * 按任务查询产出；同一任务多条时取 {@code create_time} 最新的一条。
     */
    OutputVo getOutputByTask(String taskId);

    /**
     * 修改产出平台与链接；任务已归档时不可修改。
     *
     * @return 产出 id
     */
    String reviseOutput(OutputUpdateDto dto);
}
