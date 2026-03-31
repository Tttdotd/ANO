package com.tdotd.ano.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tdotd.ano.common.constant.OutputStates;
import com.tdotd.ano.common.constant.TaskStates;
import com.tdotd.ano.common.exception.BusinessException;
import com.tdotd.ano.domain.dto.OutputCreateDto;
import com.tdotd.ano.domain.dto.OutputUpdateDto;
import com.tdotd.ano.domain.entity.Output;
import com.tdotd.ano.domain.entity.Task;
import com.tdotd.ano.domain.vo.OutputVo;
import com.tdotd.ano.mapper.OutputMapper;
import com.tdotd.ano.service.impl.OutputServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OutputServiceImpl 单元测试：覆盖 createOutput / getOutputByTask 的全部逻辑分支。
 */
@ExtendWith(MockitoExtension.class)
class OutputServiceTest {

    @Mock
    private OutputMapper outputMapper;

    @Mock
    private TaskOwnershipGuard ownershipGuard;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private OutputServiceImpl outputService;

    // ─────────────────── createOutput ───────────────────

    @Test
    void createOutput_withValidHttpsUrl_shouldInsertAndPromoteTask() {
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(makeTask("t-1", TaskStates.NOTED));
        doNothing().when(taskService).promoteTaskToDone("t-1");

        outputService.createOutput(new OutputCreateDto("t-1", "github", "https://github.com/x"));

        ArgumentCaptor<Output> captor = ArgumentCaptor.forClass(Output.class);
        verify(outputMapper).insert(captor.capture());
        Output inserted = captor.getValue();

        assertEquals("t-1", inserted.getTaskId());
        assertEquals("https://github.com/x", inserted.getUrl());
        assertEquals("github", inserted.getPlatform());
        assertEquals(OutputStates.VALID, inserted.getState());
        verify(taskService).promoteTaskToDone("t-1");
    }

    @Test
    void createOutput_withValidHttpUrl_shouldSucceed() {
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(makeTask("t-1", TaskStates.NOTED));
        doNothing().when(taskService).promoteTaskToDone("t-1");

        assertDoesNotThrow(() ->
                outputService.createOutput(new OutputCreateDto("t-1", "blog", "http://example.com")));
    }

    @Test
    void createOutput_whenTaskNotNoted_shouldThrow() {
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(makeTask("t-1", TaskStates.DOING));

        assertThrows(BusinessException.class, () ->
                outputService.createOutput(new OutputCreateDto("t-1", "github", "https://x.com")));
        verify(outputMapper, never()).insert(any(Output.class));
        verify(taskService, never()).promoteTaskToDone(any(String.class));
    }

    @Test
    void createOutput_withFtpUrl_shouldThrow() {
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(makeTask("t-1", TaskStates.NOTED));

        assertThrows(BusinessException.class, () ->
                outputService.createOutput(new OutputCreateDto("t-1", "ftp", "ftp://files.com")));
        verify(outputMapper, never()).insert(any(Output.class));
        verify(taskService, never()).promoteTaskToDone(any(String.class));
    }

    @Test
    void createOutput_whenTaskArchived_shouldThrow() {
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(makeTask("t-1", TaskStates.ARCHIVED));

        assertThrows(BusinessException.class, () ->
                outputService.createOutput(new OutputCreateDto("t-1", "github", "https://x.com")));
        verify(outputMapper, never()).insert(any(Output.class));
    }

    @Test
    void createOutput_whenTaskNotFound_shouldThrow() {
        when(ownershipGuard.requireOwnedTask("ghost")).thenThrow(new BusinessException("任务不存在"));

        assertThrows(BusinessException.class, () ->
                outputService.createOutput(new OutputCreateDto("ghost", "g", "https://x.com")));
        verify(outputMapper, never()).insert(any(Output.class));
    }

    @Test
    void createOutput_whenNotOwner_shouldThrow() {
        when(ownershipGuard.requireOwnedTask("t-1")).thenThrow(new BusinessException("无权操作该任务"));

        assertThrows(BusinessException.class, () ->
                outputService.createOutput(new OutputCreateDto("t-1", "g", "https://x.com")));
        verify(outputMapper, never()).insert(any(Output.class));
    }

    // ─────────────────── getOutputByTask ───────────────────

    @Test
    void getOutputByTask_shouldReturnLatestOutput() {
        Output o = makeOutput("o-1", "t-1", "https://example.com", "github");
        when(outputMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(o));

        OutputVo vo = outputService.getOutputByTask("t-1");

        verify(ownershipGuard).requireOwnedTask("t-1");
        assertEquals("o-1", vo.id());
        assertEquals("https://example.com", vo.url());
        assertEquals("github", vo.platform());
        assertEquals(OutputStates.VALID, vo.state());
    }

    @Test
    void getOutputByTask_whenNoOutput_shouldThrow() {
        when(outputMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        assertThrows(BusinessException.class, () -> outputService.getOutputByTask("t-1"));
    }

    @Test
    void getOutputByTask_whenTaskIdBlank_shouldThrow() {
        assertThrows(BusinessException.class, () -> outputService.getOutputByTask("   "));
        verify(ownershipGuard, never()).requireOwnedTask(any(String.class));
    }

    @Test
    void getOutputByTask_whenTaskIdNull_shouldThrow() {
        assertThrows(BusinessException.class, () -> outputService.getOutputByTask(null));
        verify(ownershipGuard, never()).requireOwnedTask(any(String.class));
    }

    // ─────────────────── reviseOutput ───────────────────

    @Test
    void reviseOutput_shouldUpdatePlatformAndUrl() {
        Output existing = makeOutput("o-1", "t-1", "https://old.com", "x");
        when(outputMapper.selectById("o-1")).thenReturn(existing);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(makeTask("t-1", TaskStates.DONE));

        String id = outputService.reviseOutput(new OutputUpdateDto("o-1", "github", "https://new.com"));

        assertEquals("o-1", id);
        ArgumentCaptor<Output> captor = ArgumentCaptor.forClass(Output.class);
        verify(outputMapper).updateById(captor.capture());
        assertEquals("github", captor.getValue().getPlatform());
        assertEquals("https://new.com", captor.getValue().getUrl());
    }

    @Test
    void reviseOutput_whenOutputMissing_shouldThrow() {
        when(outputMapper.selectById("o-x")).thenReturn(null);

        assertThrows(BusinessException.class, () ->
                outputService.reviseOutput(new OutputUpdateDto("o-x", "p", "https://x.com")));
        verify(outputMapper, never()).updateById(any(Output.class));
    }

    @Test
    void reviseOutput_whenTaskArchived_shouldThrow() {
        Output existing = makeOutput("o-1", "t-1", "https://old.com", "x");
        when(outputMapper.selectById("o-1")).thenReturn(existing);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(makeTask("t-1", TaskStates.ARCHIVED));

        assertThrows(BusinessException.class, () ->
                outputService.reviseOutput(new OutputUpdateDto("o-1", "p", "https://x.com")));
        verify(outputMapper, never()).updateById(any(Output.class));
    }

    @Test
    void reviseOutput_withInvalidUrl_shouldThrow() {
        Output existing = makeOutput("o-1", "t-1", "https://old.com", "x");
        when(outputMapper.selectById("o-1")).thenReturn(existing);
        when(ownershipGuard.requireOwnedTask("t-1")).thenReturn(makeTask("t-1", TaskStates.DONE));

        assertThrows(BusinessException.class, () ->
                outputService.reviseOutput(new OutputUpdateDto("o-1", "p", "ftp://x")));
        verify(outputMapper, never()).updateById(any(Output.class));
    }

    // ─────────────────── 工具方法 ───────────────────

    private Task makeTask(String id, int state) {
        Task t = new Task();
        t.setId(id);
        t.setUserId("user-1");
        t.setState(state);
        return t;
    }

    private Output makeOutput(String id, String taskId, String url, String platform) {
        Output o = new Output();
        o.setId(id);
        o.setTaskId(taskId);
        o.setUrl(url);
        o.setPlatform(platform);
        o.setState(OutputStates.VALID);
        return o;
    }
}
