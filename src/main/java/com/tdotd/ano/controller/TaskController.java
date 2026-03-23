package com.tdotd.ano.controller;

import com.tdotd.ano.common.result.Result;
import com.tdotd.ano.domain.dto.TaskCreateDto;
import com.tdotd.ano.domain.vo.TaskCreateVo;
import com.tdotd.ano.domain.vo.TaskDisplayVo;
import com.tdotd.ano.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@Validated
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public Result<TaskCreateVo> create(@Valid @RequestBody TaskCreateDto dto) {
        return Result.ok(taskService.createTask(dto));
    }

    @GetMapping
    public Result<List<TaskDisplayVo>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer state) {
        return Result.ok(taskService.listTasks(date, state));
    }
}
