package com.tdotd.ano.service;

import com.tdotd.ano.common.constant.TaskStates;
import com.tdotd.ano.common.exception.BusinessException;
import com.tdotd.ano.domain.dto.NoteCreateDto;
import com.tdotd.ano.domain.dto.NoteUpdateDto;
import com.tdotd.ano.domain.dto.OutputCreateDto;
import com.tdotd.ano.domain.dto.TaskCreateDto;
import com.tdotd.ano.mapper.TaskMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AnoServiceStateIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private NoteService noteService;

    @Autowired
    private OutputService outputService;

    @Autowired
    private TaskMapper taskMapper;

    // ─────────────────── 完整正向流程 ───────────────────

    @Test
    void createNotePromotesTaskToDoing() {
        var task = taskService.createTask(new TaskCreateDto("t0", "d0"));
        assertEquals(TaskStates.TODO, taskMapper.selectById(task.id()).getState());
        noteService.createNote(new NoteCreateDto(task.id()));
        assertEquals(TaskStates.DOING, taskMapper.selectById(task.id()).getState());
    }

    @Test
    void updateNoteToCompletedPromotesTaskToNoted() {
        var task = taskService.createTask(new TaskCreateDto("t", "d"));
        var note = noteService.createNote(new NoteCreateDto(task.id()));
        assertEquals(TaskStates.DOING, taskMapper.selectById(task.id()).getState());
        noteService.updateNote(new NoteUpdateDto(note.id(), "hello", 1));
        assertEquals(TaskStates.NOTED, taskMapper.selectById(task.id()).getState());
    }

    @Test
    void createOutputPromotesTaskToDone() {
        var task = taskService.createTask(new TaskCreateDto("t2", "d2"));
        var note = noteService.createNote(new NoteCreateDto(task.id()));
        noteService.updateNote(new NoteUpdateDto(note.id(), "思考内容", 1));
        assertEquals(TaskStates.NOTED, taskMapper.selectById(task.id()).getState());
        outputService.createOutput(new OutputCreateDto(task.id(), "github", "https://example.com/p"));
        assertEquals(TaskStates.DONE, taskMapper.selectById(task.id()).getState());
    }

    // ─────────────────── 状态机约束校验 ───────────────────

    @Test
    void createOutputWhenTaskNotNotedThrows() {
        var task = taskService.createTask(new TaskCreateDto("t-guard", "d"));
        // task 处于 TODO 状态，不能创建 output
        assertThrows(BusinessException.class, () ->
                outputService.createOutput(new OutputCreateDto(task.id(), "github", "https://example.com")));

        // task 推进到 DOING，仍然不能创建 output
        noteService.createNote(new NoteCreateDto(task.id()));
        assertEquals(TaskStates.DOING, taskMapper.selectById(task.id()).getState());
        assertThrows(BusinessException.class, () ->
                outputService.createOutput(new OutputCreateDto(task.id(), "github", "https://example.com")));
    }

    // ─────────────────── 业务规则校验 ───────────────────

    @Test
    void createDuplicateNoteForSameTaskThrows() {
        var task = taskService.createTask(new TaskCreateDto("t", "d"));
        noteService.createNote(new NoteCreateDto(task.id()));
        assertThrows(BusinessException.class, () ->
                noteService.createNote(new NoteCreateDto(task.id())));
    }

    @Test
    void updateNoteWithIllegalStateThrows() {
        var task = taskService.createTask(new TaskCreateDto("t3", "d3"));
        var note = noteService.createNote(new NoteCreateDto(task.id()));
        assertThrows(BusinessException.class, () ->
                noteService.updateNote(new NoteUpdateDto(note.id(), "x", 2)));
    }

    @Test
    void updateNoteToDoneWhenAlreadyDoneDoesNotRollbackState() {
        var task = taskService.createTask(new TaskCreateDto("t4", "d4"));
        var note = noteService.createNote(new NoteCreateDto(task.id()));
        noteService.updateNote(new NoteUpdateDto(note.id(), "思考内容", 1));
        outputService.createOutput(new OutputCreateDto(task.id(), "github", "https://example.com/p"));
        assertEquals(TaskStates.DONE, taskMapper.selectById(task.id()).getState());

        // Done 状态不应被 note 的再次 update 回退为 Noted
        noteService.updateNote(new NoteUpdateDto(note.id(), "更新内容", 1));
        assertEquals(TaskStates.DONE, taskMapper.selectById(task.id()).getState());
    }

    @Test
    void updateNoteWithBlankContentAndDoneStateThrows() {
        var task = taskService.createTask(new TaskCreateDto("t5", "d5"));
        var note = noteService.createNote(new NoteCreateDto(task.id()));
        assertThrows(BusinessException.class, () ->
                noteService.updateNote(new NoteUpdateDto(note.id(), "   ", 1)));
    }

    @Test
    void createOutputWithInvalidUrlThrows() {
        var task = taskService.createTask(new TaskCreateDto("t6", "d6"));
        var note = noteService.createNote(new NoteCreateDto(task.id()));
        noteService.updateNote(new NoteUpdateDto(note.id(), "思考内容", 1));
        assertThrows(BusinessException.class, () ->
                outputService.createOutput(new OutputCreateDto(task.id(), "github", "ftp://invalid.com")));
    }

    @Test
    void createNoteForNonExistentTaskThrows() {
        assertThrows(BusinessException.class, () ->
                noteService.createNote(new NoteCreateDto("999999999999")));
    }
}
