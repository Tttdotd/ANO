package com.tdotd.ano.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdotd.ano.common.exception.BusinessException;
import com.tdotd.ano.domain.vo.NoteDisplayVo;
import com.tdotd.ano.service.NoteService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * NoteController 接口测试：验证 POST/GET/PUT /api/v1/notes 的路由与契约。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NoteService noteService;

    @Test
    void postNote_withValidTaskId_shouldReturnCode0AndVo() throws Exception {
        var vo = new NoteDisplayVo("n-1", "", "t-1", 0);
        when(noteService.createNote(any())).thenReturn(vo);

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("task_id", "t-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("n-1"))
                .andExpect(jsonPath("$.data.taskId").value("t-1"));
    }

    @Test
    void postNote_withBlankTaskId_shouldReturnCode400() throws Exception {
        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("task_id", ""))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void getNote_withValidTaskId_shouldReturnCode0AndVo() throws Exception {
        var vo = new NoteDisplayVo("n-1", "内容", "t-1", 1);
        when(noteService.getNoteByTaskId("t-1")).thenReturn(vo);

        mockMvc.perform(get("/api/v1/notes").param("task_id", "t-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.content").value("内容"));
    }

    @Test
    void getNote_withMissingTaskId_shouldReturnCode400() throws Exception {
        mockMvc.perform(get("/api/v1/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void putNote_withValidBody_shouldReturnNoteId() throws Exception {
        when(noteService.updateNote(any())).thenReturn("n-1");

        mockMvc.perform(put("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("id", "n-1", "content", "内容", "state", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("n-1"));
    }

    @Test
    void putNote_withMissingId_shouldReturnCode400() throws Exception {
        mockMvc.perform(put("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("content", "内容", "state", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void putNote_whenServiceThrowsBusinessException_shouldReturnErrorResult() throws Exception {
        when(noteService.updateNote(any())).thenThrow(new BusinessException("笔记状态仅支持草稿(0)或完成(1)"));

        mockMvc.perform(put("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("id", "n-1", "content", "x", "state", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("笔记状态仅支持草稿(0)或完成(1)"));
    }
}
