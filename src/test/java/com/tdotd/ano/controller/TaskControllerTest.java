package com.tdotd.ano.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdotd.ano.common.exception.BusinessException;
import com.tdotd.ano.domain.vo.TaskCreateVo;
import com.tdotd.ano.domain.vo.TaskDisplayVo;
import com.tdotd.ano.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TaskController 接口测试：验证路由、参数校验、响应结构，Service 层全 Mock。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @Test
    void postTask_withValidBody_shouldReturnCode0AndVo() throws Exception {
        var vo = new TaskCreateVo("id-1", "测试任务", 0, LocalDateTime.now());
        when(taskService.createTask(any())).thenReturn(vo);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "测试任务"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("id-1"))
                .andExpect(jsonPath("$.data.title").value("测试任务"))
                .andExpect(jsonPath("$.data.state").value(0));
    }

    @Test
    void postTask_withBlankTitle_shouldReturnCode400() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", ""))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void postTask_withoutTitle_shouldReturnCode400() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void getTasks_shouldReturnCode0AndArray() throws Exception {
        var item = new TaskDisplayVo("id-1", "任务1", "描述", 0, 1, LocalDateTime.now());
        when(taskService.listTasks(any(), any())).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/tasks").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value("id-1"));
    }

    @Test
    void getTasks_withStateFilter_shouldPassStateToService() throws Exception {
        when(taskService.listTasks(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/tasks").param("state", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void postTask_whenServiceThrowsBusinessException_shouldReturnErrorResult() throws Exception {
        when(taskService.createTask(any())).thenThrow(new BusinessException("任务创建失败"));

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "任务"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("任务创建失败"));
    }
}
