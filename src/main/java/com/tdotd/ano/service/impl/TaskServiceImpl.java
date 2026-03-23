package com.tdotd.ano.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tdotd.ano.common.constant.TaskStates;
import com.tdotd.ano.common.exception.BusinessException;
import com.tdotd.ano.domain.dto.TaskCreateDto;
import com.tdotd.ano.domain.entity.Note;
import com.tdotd.ano.domain.entity.Task;
import com.tdotd.ano.domain.vo.TaskCreateVo;
import com.tdotd.ano.domain.vo.TaskDisplayVo;
import com.tdotd.ano.infrastructure.security.UserIdProvider;
import com.tdotd.ano.mapper.NoteMapper;
import com.tdotd.ano.mapper.TaskMapper;
import com.tdotd.ano.service.TaskOwnershipGuard;
import com.tdotd.ano.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final NoteMapper noteMapper;
    private final UserIdProvider userIdProvider;
    private final TaskOwnershipGuard ownershipGuard;

    public TaskServiceImpl(
            TaskMapper taskMapper,
            NoteMapper noteMapper,
            UserIdProvider userIdProvider,
            TaskOwnershipGuard ownershipGuard) {
        this.taskMapper = taskMapper;
        this.noteMapper = noteMapper;
        this.userIdProvider = userIdProvider;
        this.ownershipGuard = ownershipGuard;
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
        return new TaskCreateVo(task.getId(), task.getTitle(), task.getState(), task.getCreateTime());
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
                .map(t -> new TaskDisplayVo(
                        t.getId(),
                        t.getTitle(),
                        t.getDescription(),
                        t.getState(),
                        t.getVersion(),
                        t.getCreateTime()))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void promoteTaskToDoing(String taskId) {
        Task task = ownershipGuard.requireOwnedTask(taskId);
        Integer s = task.getState();
        if (s != null && s >= TaskStates.DOING) {
            return;
        }
        task.setState(TaskStates.DOING);
        taskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void promoteTaskToNoted(String taskId) {
        Task task = ownershipGuard.requireOwnedTask(taskId);
        Integer s = task.getState();
        if (s != null && s >= TaskStates.DONE) {
            return;
        }
        Note note = noteMapper.selectOne(new LambdaQueryWrapper<Note>().eq(Note::getTaskId, taskId));
        if (note == null) {
            throw new BusinessException("请先创建笔记，再沉淀思考内容");
        }
        String content = note.getContent();
        if (content == null || content.isBlank()) {
            throw new BusinessException("请先填写有效的思考笔记内容，再进入已沉淀状态");
        }
        task.setState(TaskStates.NOTED);
        taskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void promoteTaskToDone(String taskId) {
        Task task = ownershipGuard.requireOwnedTask(taskId);
        Integer s = task.getState();
        if (s != null && s >= TaskStates.DONE) {
            return;
        }
        if (s == null || s < TaskStates.NOTED) {
            throw new BusinessException("请先完成思考笔记沉淀，再提交产出链接");
        }
        task.setState(TaskStates.DONE);
        taskMapper.updateById(task);
    }
}
