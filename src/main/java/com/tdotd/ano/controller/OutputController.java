package com.tdotd.ano.controller;

import com.tdotd.ano.common.result.Result;
import com.tdotd.ano.domain.dto.OutputCreateDto;
import com.tdotd.ano.domain.dto.OutputUpdateDto;
import com.tdotd.ano.domain.vo.OutputVo;
import com.tdotd.ano.service.OutputService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/outputs")
@Validated
public class OutputController {

    private final OutputService outputService;

    public OutputController(OutputService outputService) {
        this.outputService = outputService;
    }

    @PostMapping
    public Result<String> create(@Valid @RequestBody OutputCreateDto dto) {
        return Result.ok(outputService.createOutput(dto));
    }

    @GetMapping
    public Result<OutputVo> get(@RequestParam(required = false) String taskId) {
        return Result.ok(outputService.getOutputByTask(taskId));
    }

    @PutMapping
    public Result<String> revise(@Valid @RequestBody OutputUpdateDto dto) {
        return Result.ok(outputService.reviseOutput(dto));
    }
}
