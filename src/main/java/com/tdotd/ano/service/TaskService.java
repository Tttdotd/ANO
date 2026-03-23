package com.tdotd.ano.service;

import com.tdotd.ano.domain.dto.TaskCreateDto;
import com.tdotd.ano.domain.vo.TaskCreateVo;
import com.tdotd.ano.domain.vo.TaskDisplayVo;

import java.time.LocalDate;
import java.util.List;

public interface TaskService {

    /**
     * 创建新任务，初始状态为 Todo。
     */
    TaskCreateVo createTask(TaskCreateDto dto);

    /**
     * 按当前用户列出任务，可按日期与状态过滤。
     */
    List<TaskDisplayVo> listTasks(LocalDate date, Integer state);

    /**
     * 将任务推进至「进行中」({@code DOING})。
     * 已处于 Doing 及以上状态时幂等返回，不降级。
     */
    void promoteTaskToDoing(String taskId);

    /**
     * 将任务推进至「已沉淀」({@code NOTED})：要求已存在非空笔记内容。
     * 已处于 Done/Archived 的任务不会回退（只前进不回退）。
     */
    void promoteTaskToNoted(String taskId);

    /**
     * 将任务推进至「已完成」({@code DONE})。
     * 已处于 Done/Archived 的任务幂等，不回退。
     * URL 格式校验由调用方（OutputService）在写库前完成。
     */
    void promoteTaskToDone(String taskId);
}
