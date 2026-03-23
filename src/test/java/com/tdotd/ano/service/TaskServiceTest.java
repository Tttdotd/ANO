package com.tdotd.ano.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tdotd.ano.common.constant.TaskStates;
import com.tdotd.ano.common.exception.BusinessException;
import com.tdotd.ano.domain.dto.TaskCreateDto;
import com.tdotd.ano.domain.entity.Note;
import com.tdotd.ano.domain.entity.Task;
import com.tdotd.ano.domain.vo.TaskCreateVo;
import com.tdotd.ano.infrastructure.security.UserIdProvider;
import com.tdotd.ano.mapper.NoteMapper;
import com.tdotd.ano.mapper.TaskMapper;
import com.tdotd.ano.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TaskServiceImpl 单元测试：所有外部依赖均 Mock，专注于方法内部逻辑分支。
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private NoteMapper noteMapper;

    @Mock
    private UserIdProvider userIdProvider;

    @Mock
    private TaskOwnershipGuard ownershipGuard;

    @InjectMocks
    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        lenient().when(userIdProvider.currentUserId()).thenReturn("user-1");
    }

    // ─────────────────── createTask ───────────────────

    @Test
    void createTask_shouldInsertAndReturnVo() {
        TaskCreateVo vo = taskService.createTask(new TaskCreateDto("学习 Spring", "每天一章"));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).insert(captor.capture());

        Task inserted = captor.getValue();
        assertEquals("user-1", inserted.getUserId());
        assertEquals("学习 Spring", inserted.getTitle());
        assertEquals(TaskStates.TODO, inserted.getState());
        assertEquals(1, inserted.getVersion());

        assertEquals("学习 Spring", vo.title());
        assertEquals(TaskStates.TODO, vo.state());
    }

    // ─────────────────── promoteTaskToDoing ───────────────────

    @Test
    void promoteTaskToDoing_shouldSetStateToDoing() {
        Task task = makeTask("t-1", "user-1", TaskStates.TODO);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(task);

        taskService.promoteTaskToDoing("t-1");

        assertEquals(TaskStates.DOING, task.getState());
        verify(taskMapper).updateById(task);
    }

    @Test
    void promoteTaskToDoing_whenAlreadyDoing_shouldBeIdempotent() {
        Task task = makeTask("t-1", "user-1", TaskStates.DOING);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(task);

        taskService.promoteTaskToDoing("t-1");

        verify(taskMapper, never()).updateById(any(Task.class));
    }

    @Test
    void promoteTaskToDoing_whenAlreadyNoted_shouldBeIdempotent() {
        Task task = makeTask("t-1", "user-1", TaskStates.NOTED);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(task);

        taskService.promoteTaskToDoing("t-1");

        verify(taskMapper, never()).updateById(any(Task.class));
    }

    @Test
    void promoteTaskToDoing_whenTaskNotFound_shouldThrow() {
        when(ownershipGuard.requireOwnedTask("ghost")).thenThrow(new BusinessException("任务不存在"));

        assertThrows(BusinessException.class, () -> taskService.promoteTaskToDoing("ghost"));
    }

    @Test
    void promoteTaskToDoing_whenNotOwner_shouldThrow() {
        when(ownershipGuard.requireOwnedTask("t-1")).thenThrow(new BusinessException("无权操作该任务"));

        assertThrows(BusinessException.class, () -> taskService.promoteTaskToDoing("t-1"));
        verify(taskMapper, never()).updateById(any(Task.class));
    }

    // ─────────────────── promoteTaskToNoted ───────────────────

    @Test
    void promoteTaskToNoted_shouldSetStateToNoted() {
        Task task = makeTask("t-1", "user-1", TaskStates.TODO);
        Note note = makeNote("t-1", "有效的思考内容");
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(task);
        when(noteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(note);

        taskService.promoteTaskToNoted("t-1");

        assertEquals(TaskStates.NOTED, task.getState());
        verify(taskMapper).updateById(task);
    }

    @Test
    void promoteTaskToNoted_whenAlreadyDone_shouldNotChangeState() {
        Task task = makeTask("t-1", "user-1", TaskStates.DONE);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(task);

        taskService.promoteTaskToNoted("t-1");

        verify(taskMapper, never()).updateById(any(Task.class));
    }

    @Test
    void promoteTaskToNoted_whenNoteIsNull_shouldThrow() {
        Task task = makeTask("t-1", "user-1", TaskStates.TODO);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(task);
        when(noteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThrows(BusinessException.class, () -> taskService.promoteTaskToNoted("t-1"));
        verify(taskMapper, never()).updateById(any(Task.class));
    }

    @Test
    void promoteTaskToNoted_whenNoteContentBlank_shouldThrow() {
        Task task = makeTask("t-1", "user-1", TaskStates.TODO);
        Note note = makeNote("t-1", "   ");
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(task);
        when(noteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(note);

        assertThrows(BusinessException.class, () -> taskService.promoteTaskToNoted("t-1"));
        verify(taskMapper, never()).updateById(any(Task.class));
    }

    @Test
    void promoteTaskToNoted_whenTaskNotFound_shouldThrow() {
        when(ownershipGuard.requireOwnedTask("ghost")).thenThrow(new BusinessException("任务不存在"));

        assertThrows(BusinessException.class, () -> taskService.promoteTaskToNoted("ghost"));
    }

    // ─────────────────── promoteTaskToDone ───────────────────

    @Test
    void promoteTaskToDone_shouldSetStateToDone() {
        Task task = makeTask("t-1", "user-1", TaskStates.NOTED);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(task);

        taskService.promoteTaskToDone("t-1");

        assertEquals(TaskStates.DONE, task.getState());
        verify(taskMapper).updateById(task);
    }

    @Test
    void promoteTaskToDone_whenAlreadyDone_shouldBeIdempotent() {
        Task task = makeTask("t-1", "user-1", TaskStates.DONE);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(task);

        taskService.promoteTaskToDone("t-1");

        verify(taskMapper, never()).updateById(any(Task.class));
    }

    @Test
    void promoteTaskToDone_whenArchived_shouldBeIdempotent() {
        Task task = makeTask("t-1", "user-1", TaskStates.ARCHIVED);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(task);

        taskService.promoteTaskToDone("t-1");

        verify(taskMapper, never()).updateById(any(Task.class));
    }

    @Test
    void promoteTaskToDone_whenTaskNotNoted_shouldThrow() {
        Task task = makeTask("t-1", "user-1", TaskStates.DOING);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(task);

        assertThrows(BusinessException.class, () -> taskService.promoteTaskToDone("t-1"));
        verify(taskMapper, never()).updateById(any(Task.class));
    }

    @Test
    void promoteTaskToDone_whenNotOwner_shouldThrow() {
        when(ownershipGuard.requireOwnedTask("t-1")).thenThrow(new BusinessException("无权操作该任务"));

        assertThrows(BusinessException.class, () -> taskService.promoteTaskToDone("t-1"));
        verify(taskMapper, never()).updateById(any(Task.class));
    }

    // ─────────────────── 工具方法 ───────────────────

    private Task makeTask(String id, String userId, int state) {
        Task t = new Task();
        t.setId(id);
        t.setUserId(userId);
        t.setState(state);
        return t;
    }

    private Note makeNote(String taskId, String content) {
        Note n = new Note();
        n.setTaskId(taskId);
        n.setContent(content);
        return n;
    }
}
