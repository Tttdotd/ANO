package com.tdotd.ano.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tdotd.ano.common.constant.KnowledgeNodeTypeConstants;
import com.tdotd.ano.common.exception.BusinessException;
import com.tdotd.ano.common.utils.VectorUtils;
import com.tdotd.ano.domain.dto.KnowledgeArchiveRequest;
import com.tdotd.ano.domain.entity.KnowledgeNode;
import com.tdotd.ano.domain.event.KnowledgeExtractedEvent;
import com.tdotd.ano.domain.entity.Note;
import com.tdotd.ano.domain.entity.Task;
import com.tdotd.ano.infrastructure.ai.KnowledgeRefiner;
import com.tdotd.ano.infrastructure.ai.VectorService;
import com.tdotd.ano.infrastructure.persistence.RedisVectorRepository;
import com.tdotd.ano.mapper.KnowledgeNodeMapper;
import com.tdotd.ano.mapper.NoteMapper;
import com.tdotd.ano.mapper.TaskMapper;
import com.tdotd.ano.service.KnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    private final TaskMapper taskMapper;
    private final NoteMapper noteMapper;
    private final KnowledgeNodeMapper knowledgeNodeMapper;
    private final KnowledgeRefiner knowledgeRefiner;
    private final VectorService vectorService;
    private final RedisVectorRepository redisVectorRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public KnowledgeServiceImpl(
            TaskMapper taskMapper,
            NoteMapper noteMapper,
            KnowledgeNodeMapper knowledgeNodeMapper,
            KnowledgeRefiner knowledgeRefiner,
            VectorService vectorService,
            RedisVectorRepository redisVectorRepository,
            ApplicationEventPublisher applicationEventPublisher) {
        this.taskMapper = taskMapper;
        this.noteMapper = noteMapper;
        this.knowledgeNodeMapper = knowledgeNodeMapper;
        this.knowledgeRefiner = knowledgeRefiner;
        this.vectorService = vectorService;
        this.redisVectorRepository = redisVectorRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String archiveKnowledge(KnowledgeArchiveRequest request) {
        KnowledgeNode existing = knowledgeNodeMapper.selectOne(
                new LambdaQueryWrapper<KnowledgeNode>()
                        .eq(KnowledgeNode::getSourceTaskId, request.taskId())
                        .eq(KnowledgeNode::getNodeType, KnowledgeNodeTypeConstants.TASK_EXTRACTED));
        if (existing != null) {
            log.info("knowledge archive skipped(already exists): taskId={}, nodeId={}",
                    request.taskId(), existing.getId());
            return existing.getId();
        }

        Task task = taskMapper.selectById(request.taskId());
        if (task == null) {
            throw new BusinessException("任务不存在，无法归档知识节点");
        }
        Note note = noteMapper.selectOne(new LambdaQueryWrapper<Note>().eq(Note::getTaskId, request.taskId()));
        if (note == null || note.getContent() == null || note.getContent().isBlank()) {
            throw new BusinessException("任务缺少有效笔记内容，无法归档知识节点");
        }

        String refinedContent = knowledgeRefiner.refine(task.getTitle(), task.getDescription(), note.getContent());
        float[] vector = vectorService.getVector(refinedContent);

        KnowledgeNode node = new KnowledgeNode();
        node.setSourceTaskId(request.taskId());
        node.setNodeType(KnowledgeNodeTypeConstants.TASK_EXTRACTED);
        node.setTitle(task.getTitle());
        node.setContent(refinedContent);
        node.setVector(VectorUtils.toBuffer(vector));

        knowledgeNodeMapper.insert(node);
        redisVectorRepository.save(node);
        applicationEventPublisher.publishEvent(new KnowledgeExtractedEvent(node.getId()));
        log.info("knowledge archived: taskId={}, nodeId={}", request.taskId(), node.getId());
        return node.getId();
    }
}
