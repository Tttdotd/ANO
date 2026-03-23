package com.tdotd.ano.controller;

import com.tdotd.ano.common.result.Result;
import com.tdotd.ano.domain.dto.NoteCreateDto;
import com.tdotd.ano.domain.dto.NoteUpdateDto;
import com.tdotd.ano.domain.vo.NoteDisplayVo;
import com.tdotd.ano.service.NoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notes")
@Validated
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public Result<NoteDisplayVo> create(@Valid @RequestBody NoteCreateDto dto) {
        return Result.ok(noteService.createNote(dto));
    }

    @GetMapping
    public Result<NoteDisplayVo> get(@RequestParam("task_id") @NotBlank(message = "task_id 不能为空") String taskId) {
        return Result.ok(noteService.getNoteByTaskId(taskId));
    }

    @PutMapping
    public Result<String> update(@Valid @RequestBody NoteUpdateDto dto) {
        return Result.ok(noteService.updateNote(dto));
    }
}
