package com.tdotd.ano.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tdotd.ano.common.constant.NoteStates;
import com.tdotd.ano.common.constant.TaskStates;
import com.tdotd.ano.common.exception.BusinessException;
import com.tdotd.ano.domain.dto.NoteCreateDto;
import com.tdotd.ano.domain.dto.NoteUpdateDto;
import com.tdotd.ano.domain.entity.Note;
import com.tdotd.ano.domain.entity.Task;
import com.tdotd.ano.domain.vo.NoteDisplayVo;
import com.tdotd.ano.mapper.NoteMapper;
import com.tdotd.ano.service.impl.NoteServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * NoteServiceImpl 单元测试：覆盖 createNote / getNoteByTaskId / updateNote 的全部逻辑分支。
 */
@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteMapper noteMapper;

    @Mock
    private TaskOwnershipGuard ownershipGuard;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private NoteServiceImpl noteService;

    // ─────────────────── createNote ───────────────────

    @Test
    void createNote_shouldInsertEmptyDraftAndPromoteTaskToDoing() {
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(makeTask("t-1", "u-1", TaskStates.TODO));
        when(noteMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        doNothing().when(taskService).promoteTaskToDoing("t-1");

        NoteDisplayVo vo = noteService.createNote(new NoteCreateDto("t-1"));

        verify(ownershipGuard).requireOwnedTask("t-1");
        verify(noteMapper).insert(any(Note.class));
        verify(taskService).promoteTaskToDoing("t-1");
        assertEquals("t-1", vo.taskId());
        assertEquals(NoteStates.DRAFT, vo.state());
        assertEquals("", vo.content());
    }

    @Test
    void createNote_whenDuplicateNote_shouldThrow() {
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(makeTask("t-1", "u-1", TaskStates.TODO));
        when(noteMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class, () -> noteService.createNote(new NoteCreateDto("t-1")));
        verify(noteMapper, never()).insert(any(Note.class));
        verify(taskService, never()).promoteTaskToDoing(any(String.class));
    }

    @Test
    void createNote_whenTaskNotFound_shouldThrow() {
        when(ownershipGuard.requireOwnedTask("ghost")).thenThrow(new BusinessException("任务不存在"));

        assertThrows(BusinessException.class, () -> noteService.createNote(new NoteCreateDto("ghost")));
        verify(noteMapper, never()).insert(any(Note.class));
        verify(taskService, never()).promoteTaskToDoing(any(String.class));
    }

    @Test
    void createNote_whenNotOwner_shouldThrow() {
        when(ownershipGuard.requireOwnedTask("t-1")).thenThrow(new BusinessException("无权操作该任务"));

        assertThrows(BusinessException.class, () -> noteService.createNote(new NoteCreateDto("t-1")));
        verify(noteMapper, never()).insert(any(Note.class));
        verify(taskService, never()).promoteTaskToDoing(any(String.class));
    }

    // ─────────────────── getNoteByTaskId ───────────────────

    @Test
    void getNoteByTaskId_shouldReturnVo() {
        Note note = makeNote("n-1", "t-1", "思考内容");
        when(noteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(note);

        NoteDisplayVo vo = noteService.getNoteByTaskId("t-1");

        verify(ownershipGuard).requireOwnedTask("t-1");
        assertEquals("n-1", vo.id());
        assertEquals("t-1", vo.taskId());
        assertEquals("思考内容", vo.content());
    }

    @Test
    void getNoteByTaskId_whenNoteAbsent_shouldThrow() {
        when(noteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThrows(BusinessException.class, () -> noteService.getNoteByTaskId("t-1"));
    }

    // ─────────────────── updateNote ───────────────────

    @Test
    void updateNote_withDraftState_shouldSaveContentAndNotPromoteTask() {
        Note note = makeNote("n-1", "t-1", "");
        when(noteMapper.selectById("n-1")).thenReturn(note);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(makeTask("t-1", "u-1", TaskStates.DOING));

        noteService.updateNote(new NoteUpdateDto("n-1", "草稿内容", NoteStates.DRAFT));

        verify(ownershipGuard).requireOwnedTask("t-1");
        verify(noteMapper).updateById(note);
        verify(taskService, never()).promoteTaskToNoted(any(String.class));
    }

    @Test
    void updateNote_withDoneStateAndValidContent_shouldPromoteTask() {
        Note note = makeNote("n-1", "t-1", "");
        when(noteMapper.selectById("n-1")).thenReturn(note);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(makeTask("t-1", "u-1", TaskStates.DOING));
        doNothing().when(taskService).promoteTaskToNoted("t-1");

        noteService.updateNote(new NoteUpdateDto("n-1", "完整的思考内容", NoteStates.DONE));

        verify(noteMapper).updateById(note);
        verify(taskService).promoteTaskToNoted("t-1");
    }

    @Test
    void updateNote_withIllegalState_shouldThrow() {
        Note note = makeNote("n-1", "t-1", "");
        when(noteMapper.selectById("n-1")).thenReturn(note);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(makeTask("t-1", "u-1", TaskStates.DOING));

        assertThrows(BusinessException.class, () ->
                noteService.updateNote(new NoteUpdateDto("n-1", "x", 2)));
        verify(noteMapper, never()).updateById(any(Note.class));
        verify(taskService, never()).promoteTaskToNoted(any(String.class));
    }

    @Test
    void updateNote_whenTaskArchivedAndSubmitDone_shouldThrow() {
        Note note = makeNote("n-1", "t-1", "");
        when(noteMapper.selectById("n-1")).thenReturn(note);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(makeTask("t-1", "u-1", TaskStates.ARCHIVED));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                noteService.updateNote(new NoteUpdateDto("n-1", "有效内容", NoteStates.DONE)));
        assertEquals("已归档任务不可提交笔记", ex.getMessage());
        verify(noteMapper, never()).updateById(any(Note.class));
        verify(taskService, never()).promoteTaskToNoted(any(String.class));
    }

    @Test
    void updateNote_whenTaskArchivedAndDraft_shouldThrow() {
        Note note = makeNote("n-1", "t-1", "");
        when(noteMapper.selectById("n-1")).thenReturn(note);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(makeTask("t-1", "u-1", TaskStates.ARCHIVED));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                noteService.updateNote(new NoteUpdateDto("n-1", "草稿", NoteStates.DRAFT)));
        assertEquals("已归档任务不可修改笔记", ex.getMessage());
        verify(noteMapper, never()).updateById(any(Note.class));
    }

    @Test
    void updateNote_withDoneStateButBlankContent_shouldThrow() {
        Note note = makeNote("n-1", "t-1", "");
        when(noteMapper.selectById("n-1")).thenReturn(note);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(makeTask("t-1", "u-1", TaskStates.DOING));

        assertThrows(BusinessException.class, () ->
                noteService.updateNote(new NoteUpdateDto("n-1", "   ", NoteStates.DONE)));
        verify(noteMapper, never()).updateById(any(Note.class));
    }

    @Test
    void updateNote_whenNoteNotFound_shouldThrow() {
        assertThrows(BusinessException.class, () ->
                noteService.updateNote(new NoteUpdateDto("ghost", "x", NoteStates.DRAFT)));
        verify(noteMapper, never()).updateById(any(Note.class));
    }

    // ─────────────────── 工具方法 ───────────────────

    private Task makeTask(String id, String userId, int state) {
        Task t = new Task();
        t.setId(id);
        t.setUserId(userId);
        t.setState(state);
        return t;
    }

    private Note makeNote(String id, String taskId, String content) {
        Note n = new Note();
        n.setId(id);
        n.setTaskId(taskId);
        n.setContent(content);
        return n;
    }
}
