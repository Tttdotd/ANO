package com.tdotd.ano.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdotd.ano.common.constant.OutputStates;
import com.tdotd.ano.common.exception.BusinessException;
import com.tdotd.ano.domain.vo.OutputVo;
import com.tdotd.ano.service.OutputService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OutputController 接口测试：验证 POST/GET /api/v1/outputs 的路由与契约。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OutputControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OutputService outputService;

    @Test
    void postOutput_withValidBody_shouldReturnCode0AndId() throws Exception {
        when(outputService.createOutput(any())).thenReturn("o-1");

        mockMvc.perform(post("/api/v1/outputs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("taskId", "t-1", "platform", "github", "url", "https://github.com/x"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("o-1"));
    }

    @Test
    void postOutput_withMissingTaskId_shouldReturnCode400() throws Exception {
        mockMvc.perform(post("/api/v1/outputs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("platform", "github", "url", "https://github.com/x"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void postOutput_withMissingUrl_shouldReturnCode400() throws Exception {
        mockMvc.perform(post("/api/v1/outputs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("taskId", "t-1", "platform", "github"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void postOutput_whenServiceThrowsBusinessException_shouldReturnErrorResult() throws Exception {
        when(outputService.createOutput(any())).thenThrow(new BusinessException("产出链接需以 http:// 或 https:// 开头"));

        mockMvc.perform(post("/api/v1/outputs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("taskId", "t-1", "platform", "ftp", "url", "ftp://x.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("产出链接需以 http:// 或 https:// 开头"));
    }

    @Test
    void getOutput_withTaskId_shouldReturnCode0AndVo() throws Exception {
        var vo = new OutputVo("o-1", "https://github.com/x", OutputStates.VALID, "github");
        when(outputService.getOutputByTask("t-1")).thenReturn(vo);

        mockMvc.perform(get("/api/v1/outputs").param("taskId", "t-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("o-1"))
                .andExpect(jsonPath("$.data.url").value("https://github.com/x"))
                .andExpect(jsonPath("$.data.platform").value("github"));
    }

    @Test
    void putOutput_withValidBody_shouldReturnCode0AndId() throws Exception {
        when(outputService.reviseOutput(any())).thenReturn("o-1");

        mockMvc.perform(put("/api/v1/outputs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("id", "o-1", "platform", "github", "url", "https://github.com/x"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("o-1"));
    }

    @Test
    void putOutput_withMissingUrl_shouldReturnCode400() throws Exception {
        mockMvc.perform(put("/api/v1/outputs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("id", "o-1", "platform", "github"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}
