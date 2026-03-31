package com.tdotd.ano.service;

import com.tdotd.ano.common.exception.BusinessException;
import com.tdotd.ano.domain.entity.Task;
import com.tdotd.ano.infrastructure.security.UserIdProvider;
import com.tdotd.ano.mapper.TaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 任务归属校验组件：确认任务存在且属于当前用户。
 * 消除 Service 层中重复的 requireOwnedTask 私有方法。
 */
@Slf4j
@Component
public class TaskOwnershipGuard {

    private final TaskMapper taskMapper;
    private final UserIdProvider userIdProvider;

    public TaskOwnershipGuard(TaskMapper taskMapper, UserIdProvider userIdProvider) {
        this.taskMapper = taskMapper;
        this.userIdProvider = userIdProvider;
    }

    /**
     * 查找任务并校验归属权，失败时抛出 {@link BusinessException}。
     *
     * @return 已校验通过的 Task 实体
     */
    public Task requireOwnedTask(String taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        if (!task.getUserId().equals(userIdProvider.currentUserId())) {
            throw new BusinessException("无权操作该任务");
        }
        return task;
    }
}
