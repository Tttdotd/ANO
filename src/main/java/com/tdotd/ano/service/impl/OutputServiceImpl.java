package com.tdotd.ano.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tdotd.ano.common.constant.OutputStates;
import com.tdotd.ano.common.constant.TaskStates;
import com.tdotd.ano.common.exception.BusinessException;
import com.tdotd.ano.domain.dto.OutputCreateDto;
import com.tdotd.ano.domain.entity.Output;
import com.tdotd.ano.domain.entity.Task;
import com.tdotd.ano.domain.vo.OutputVo;
import com.tdotd.ano.mapper.OutputMapper;
import com.tdotd.ano.service.OutputService;
import com.tdotd.ano.service.TaskOwnershipGuard;
import com.tdotd.ano.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OutputServiceImpl implements OutputService {

    private final OutputMapper outputMapper;
    private final TaskOwnershipGuard ownershipGuard;
    private final TaskService taskService;

    public OutputServiceImpl(
            OutputMapper outputMapper,
            TaskOwnershipGuard ownershipGuard,
            TaskService taskService) {
        this.outputMapper = outputMapper;
        this.ownershipGuard = ownershipGuard;
        this.taskService = taskService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOutput(OutputCreateDto dto) {
        Task task = ownershipGuard.requireOwnedTask(dto.taskId());
        if (task.getState() == null || task.getState() < TaskStates.NOTED) {
            throw new BusinessException("请先完成思考笔记沉淀，再提交产出链接");
        }
        String url = dto.url().trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new BusinessException("产出链接需以 http:// 或 https:// 开头");
        }
        Output out = new Output();
        out.setTaskId(dto.taskId());
        out.setPlatform(dto.platform());
        out.setUrl(url);
        out.setState(OutputStates.VALID);
        outputMapper.insert(out);
        taskService.promoteTaskToDone(dto.taskId());
        return out.getId();
    }

    /**
     * 同一任务多条产出时取 {@code create_time} 最新的一条（与 API 单对象结构对齐）。
     */
    @Override
    public OutputVo getOutputByTask(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            throw new BusinessException("请提供 taskId 以查看产出");
        }
        ownershipGuard.requireOwnedTask(taskId);
        List<Output> list = outputMapper.selectList(
                new LambdaQueryWrapper<Output>()
                        .eq(Output::getTaskId, taskId)
                        .orderByDesc(Output::getCreateTime)
                        .last("LIMIT 1"));
        if (list.isEmpty()) {
            throw new BusinessException("暂无产出记录");
        }
        Output o = list.get(0);
        return new OutputVo(o.getId(), o.getUrl(), o.getState(), o.getPlatform());
    }
}
