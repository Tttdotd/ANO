package com.tdotd.ano.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tdotd.ano.common.constant.NoteStates;
import com.tdotd.ano.common.exception.BusinessException;
import com.tdotd.ano.domain.dto.NoteCreateDto;
import com.tdotd.ano.domain.dto.NoteUpdateDto;
import com.tdotd.ano.domain.entity.Note;
import com.tdotd.ano.domain.vo.NoteDisplayVo;
import com.tdotd.ano.mapper.NoteMapper;
import com.tdotd.ano.service.NoteService;
import com.tdotd.ano.service.TaskOwnershipGuard;
import com.tdotd.ano.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;
    private final TaskOwnershipGuard ownershipGuard;
    private final TaskService taskService;

    public NoteServiceImpl(
            NoteMapper noteMapper,
            TaskOwnershipGuard ownershipGuard,
            TaskService taskService) {
        this.noteMapper = noteMapper;
        this.ownershipGuard = ownershipGuard;
        this.taskService = taskService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteDisplayVo createNote(NoteCreateDto dto) {
        ownershipGuard.requireOwnedTask(dto.taskId());
        long exists = noteMapper.selectCount(new LambdaQueryWrapper<Note>().eq(Note::getTaskId, dto.taskId()));
        if (exists > 0) {
            throw new BusinessException("该任务已存在笔记，请先更新现有笔记");
        }
        Note note = new Note();
        note.setTaskId(dto.taskId());
        note.setContent("");
        note.setState(NoteStates.DRAFT);
        noteMapper.insert(note);
        taskService.promoteTaskToDoing(dto.taskId());
        return new NoteDisplayVo(note.getId(), note.getContent(), note.getTaskId(), note.getState());
    }

    @Override
    public NoteDisplayVo getNoteByTaskId(String taskId) {
        ownershipGuard.requireOwnedTask(taskId);
        Note note = noteMapper.selectOne(new LambdaQueryWrapper<Note>().eq(Note::getTaskId, taskId));
        if (note == null) {
            throw new BusinessException("该任务下尚未创建笔记");
        }
        return new NoteDisplayVo(note.getId(), note.getContent(), note.getTaskId(), note.getState());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateNote(NoteUpdateDto dto) {
        Note note = noteMapper.selectById(dto.id());
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        ownershipGuard.requireOwnedTask(note.getTaskId());
        int st = dto.state();
        if (st != NoteStates.DRAFT && st != NoteStates.DONE) {
            throw new BusinessException("笔记状态仅支持草稿(0)或完成(1)");
        }
        String content = dto.content();
        if (st == NoteStates.DONE && (content == null || content.isBlank())) {
            throw new BusinessException("完成状态的笔记需填写有效内容");
        }
        note.setContent(content);
        note.setState(st);
        noteMapper.updateById(note);
        if (st == NoteStates.DONE && content != null && !content.isBlank()) {
            taskService.promoteTaskToNoted(note.getTaskId());
        }
        return note.getId();
    }
}
