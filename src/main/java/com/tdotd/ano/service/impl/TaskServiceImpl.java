package com.tdotd.ano.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tdotd.ano.common.constant.TaskStates;
import com.tdotd.ano.domain.converter.TaskConverter;
import com.tdotd.ano.domain.dto.TaskCreateDto;
import com.tdotd.ano.domain.entity.Task;
import com.tdotd.ano.domain.vo.TaskCreateVo;
import com.tdotd.ano.domain.vo.TaskDisplayVo;
import com.tdotd.ano.infrastructure.security.UserIdProvider;
import com.tdotd.ano.mapper.TaskMapper;
import com.tdotd.ano.service.TaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final UserIdProvider userIdProvider;

    public TaskServiceImpl(TaskMapper taskMapper, UserIdProvider userIdProvider) {
        this.taskMapper = taskMapper;
        this.userIdProvider = userIdProvider;
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
}
