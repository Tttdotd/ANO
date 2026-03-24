package com.tdotd.ano.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tdotd.ano.common.constant.TaskStates;
import com.tdotd.ano.domain.dto.TaskCreateDto;
import com.tdotd.ano.domain.entity.Task;
import com.tdotd.ano.domain.vo.TaskCreateVo;
import com.tdotd.ano.infrastructure.security.UserIdProvider;
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
 * promoteTask* 方法不再调用 ownershipGuard，改为直接执行条件化 UPDATE，
 * 此处仅验证传入 taskMapper.update() 的实体状态字段是否正确。
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private UserIdProvider userIdProvider;

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

    /**
     * 验证 promoteTaskToDoing 向 taskMapper.update 传入的实体已将 state 设置为 DOING(1)。
     * 实际状态条件约束由 LambdaUpdateWrapper 携带，由数据库层保证原子性。
     */
    @Test
    @SuppressWarnings("unchecked")
    void promoteTaskToDoing_shouldCallUpdateWithDoingState() {
        taskService.promoteTaskToDoing("t-1");

        ArgumentCaptor<Task> entityCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).update(entityCaptor.capture(), any(LambdaUpdateWrapper.class));

        assertEquals(TaskStates.DOING, entityCaptor.getValue().getState());
    }

    // ─────────────────── promoteTaskToNoted ───────────────────

    /**
     * 验证 promoteTaskToNoted 向 taskMapper.update 传入的实体已将 state 设置为 NOTED(2)。
     */
    @Test
    @SuppressWarnings("unchecked")
    void promoteTaskToNoted_shouldCallUpdateWithNotedState() {
        taskService.promoteTaskToNoted("t-1");

        ArgumentCaptor<Task> entityCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).update(entityCaptor.capture(), any(LambdaUpdateWrapper.class));

        assertEquals(TaskStates.NOTED, entityCaptor.getValue().getState());
    }

    // ─────────────────── promoteTaskToDone ───────────────────

    /**
     * 验证 promoteTaskToDone 向 taskMapper.update 传入的实体已将 state 设置为 DONE(3)。
     */
    @Test
    @SuppressWarnings("unchecked")
    void promoteTaskToDone_shouldCallUpdateWithDoneState() {
        taskService.promoteTaskToDone("t-1");

        ArgumentCaptor<Task> entityCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).update(entityCaptor.capture(), any(LambdaUpdateWrapper.class));

        assertEquals(TaskStates.DONE, entityCaptor.getValue().getState());
    }
}
