package com.tdotd.ano.domain.converter;

import com.tdotd.ano.domain.entity.Note;
import com.tdotd.ano.domain.vo.NoteDisplayVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface NoteConverter {

    NoteConverter INSTANCE = Mappers.getMapper(NoteConverter.class);

    NoteDisplayVo toDisplayVo(Note note);
}
