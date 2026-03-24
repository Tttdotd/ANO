package com.tdotd.ano.domain.converter;

import com.tdotd.ano.domain.entity.Output;
import com.tdotd.ano.domain.vo.OutputVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OutputConverter {

    OutputConverter INSTANCE = Mappers.getMapper(OutputConverter.class);

    OutputVo toVo(Output output);
}
