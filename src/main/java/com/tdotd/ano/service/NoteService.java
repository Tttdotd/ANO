package com.tdotd.ano.service;

import com.tdotd.ano.domain.dto.NoteCreateDto;
import com.tdotd.ano.domain.dto.NoteUpdateDto;
import com.tdotd.ano.domain.vo.NoteDisplayVo;

public interface NoteService {

    NoteDisplayVo createNote(NoteCreateDto dto);

    NoteDisplayVo getNoteByTaskId(String taskId);

    /**
     * 更新笔记内容；返回笔记 ID（与 API 文档 data 字段一致）。
     */
    String updateNote(NoteUpdateDto dto);
}
