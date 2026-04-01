package com.tdotd.ano.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tdotd.ano.common.constant.TaskStates;
import com.tdotd.ano.common.exception.BusinessException;
import com.tdotd.ano.domain.converter.TaskConverter;
import com.tdotd.ano.domain.dto.KnowledgeArchiveRequest;
import com.tdotd.ano.domain.dto.TaskArchiveDto;
import com.tdotd.ano.domain.dto.TaskCreateDto;
import com.tdotd.ano.domain.dto.TaskUpdateDto;
import com.tdotd.ano.domain.entity.Task;
import com.tdotd.ano.domain.vo.TaskCreateVo;
import com.tdotd.ano.domain.vo.TaskDisplayVo;
import com.tdotd.ano.infrastructure.security.UserIdProvider;
import com.tdotd.ano.mapper.TaskMapper;
import com.tdotd.ano.service.KnowledgeService;
import com.tdotd.ano.service.TaskOwnershipGuard;
import com.tdotd.ano.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final UserIdProvider userIdProvider;
    private final TaskOwnershipGuard ownershipGuard;
    private final KnowledgeService knowledgeService;

    public TaskServiceImpl(
            TaskMapper taskMapper,
            UserIdProvider userIdProvider,
            TaskOwnershipGuard ownershipGuard,
            KnowledgeService knowledgeService) {
        this.taskMapper = taskMapper;
        this.userIdProvider = userIdProvider;
        this.ownershipGuard = ownershipGuard;
        this.knowledgeService = knowledgeService;
    }

    @Override
    public TaskCreateVo createTask(TaskCreateDto dto) {
        Task task = new Task();
        task.setUserId(userIdProvider.currentUserId());
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setState(TaskStates.TODO);
        task.setVersion(1);
        taskMapper.insert(task);
        log.info("task created: taskId={}, userId={}", task.getId(), task.getUserId());
        return TaskConverter.INSTANCE.toCreateVo(task);
    }

    @Override
    public List<TaskDisplayVo> listTasks(LocalDate date, Integer state) {
        String uid = userIdProvider.currentUserId();
        LambdaQueryWrapper<Task> w = new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, uid)
                .orderByDesc(Task::getCreateTime);
        if (state != null) {
            w.eq(Task::getState, state);
        }
        if (date != null) {
            w.ge(Task::getCreateTime, date.atStartOfDay())
             .lt(Task::getCreateTime, date.plusDays(1).atStartOfDay());
        }
        return taskMapper.selectList(w).stream()
                .map(TaskConverter.INSTANCE::toDisplayVo)
                .toList();
    }

    /**
     * 幂等：仅当 task 处于 Todo(0) 时推进，否则静默跳过。
     * 调用方（NoteServiceImpl）已完成归属权校验，此处无需重复 SELECT。
     */
    @Override
    public void promoteTaskToDoing(String taskId) {
        Task update = new Task();
        update.setState(TaskStates.DOING);
        taskMapper.update(update, new LambdaUpdateWrapper<Task>()
                .eq(Task::getId, taskId)
                .eq(Task::getState, TaskStates.TODO));
    }

    /**
     * 幂等：仅当 task 处于 Doing(1) 时推进，否则静默跳过。
     */
    @Override
    public void promoteTaskToNoted(String taskId) {
        Task update = new Task();
        update.setState(TaskStates.NOTED);
        taskMapper.update(update, new LambdaUpdateWrapper<Task>()
                .eq(Task::getId, taskId)
                .eq(Task::getState, TaskStates.DOING));
    }

    /**
     * 幂等：仅当 task 处于 Noted(2) 时推进，否则静默跳过。
     */
    @Override
    public void promoteTaskToDone(String taskId) {
        Task update = new Task();
        update.setState(TaskStates.DONE);
        taskMapper.update(update, new LambdaUpdateWrapper<Task>()
                .eq(Task::getId, taskId)
                .eq(Task::getState, TaskStates.NOTED));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String reviseTask(TaskUpdateDto dto) {
        Task task = ownershipGuard.requireOwnedTask(dto.id());
        if (task.getState() == TaskStates.ARCHIVED) {
            log.warn("task revise rejected(archived): taskId={}", dto.id());
            throw new BusinessException("已归档任务不可修改");
        }
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        taskMapper.updateById(task);
        log.info("task revised: taskId={}", task.getId());
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String archiveTask(TaskArchiveDto dto) {
        Task task = ownershipGuard.requireOwnedTask(dto.id());
        if (task.getState() == TaskStates.ARCHIVED) {
            log.warn("task archive skipped(idempotent): taskId={}", task.getId());
            return task.getId();
        }
        task.setState(TaskStates.ARCHIVED);
        task.setArchivedTime(LocalDateTime.now());
        taskMapper.updateById(task);
        String knowledgeNodeId = knowledgeService.archiveKnowledge(new KnowledgeArchiveRequest(task.getId()));
        log.info("knowledge archive pipeline done: taskId={}, knowledgeNodeId={}", task.getId(), knowledgeNodeId);
        log.info("task archived: taskId={}", task.getId());
        return task.getId();
    }
}
