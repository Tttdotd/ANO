package com.tdotd.ano.domain.converter;

import com.tdotd.ano.domain.entity.Task;
import com.tdotd.ano.domain.vo.TaskCreateVo;
import com.tdotd.ano.domain.vo.TaskDisplayVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TaskConverter {

    TaskConverter INSTANCE = Mappers.getMapper(TaskConverter.class);

    TaskCreateVo toCreateVo(Task task);

    TaskDisplayVo toDisplayVo(Task task);
}
