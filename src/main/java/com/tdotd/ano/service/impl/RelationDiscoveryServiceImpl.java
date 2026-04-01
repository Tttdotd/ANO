package com.tdotd.ano.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tdotd.ano.config.KnowledgeMiningProperties;
import com.tdotd.ano.domain.entity.KnowledgeNode;
import com.tdotd.ano.infrastructure.ai.KnowledgeRelationJudge;
import com.tdotd.ano.infrastructure.ai.VectorService;
import com.tdotd.ano.infrastructure.persistence.KnowledgeVectorSearchRepository;
import com.tdotd.ano.mapper.KnowledgeNodeMapper;
import com.tdotd.ano.service.KnowledgeEdgeService;
import com.tdotd.ano.service.KnowledgeEmergenceService;
import com.tdotd.ano.service.RelationDiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RelationDiscoveryServiceImpl implements RelationDiscoveryService {

    private final KnowledgeNodeMapper knowledgeNodeMapper;
    private final KnowledgeVectorSearchRepository vectorSearchRepository;
    private final VectorService vectorService;
    private final KnowledgeRelationJudge knowledgeRelationJudge;
    private final KnowledgeEdgeService knowledgeEdgeService;
    private final KnowledgeEmergenceService knowledgeEmergenceService;
    private final KnowledgeMiningProperties knowledgeMiningProperties;

    public RelationDiscoveryServiceImpl(
            KnowledgeNodeMapper knowledgeNodeMapper,
            KnowledgeVectorSearchRepository vectorSearchRepository,
            VectorService vectorService,
            KnowledgeRelationJudge knowledgeRelationJudge,
            KnowledgeEdgeService knowledgeEdgeService,
            KnowledgeEmergenceService knowledgeEmergenceService,
            KnowledgeMiningProperties knowledgeMiningProperties) {
        this.knowledgeNodeMapper = knowledgeNodeMapper;
        this.vectorSearchRepository = vectorSearchRepository;
        this.vectorService = vectorService;
        this.knowledgeRelationJudge = knowledgeRelationJudge;
        this.knowledgeEdgeService = knowledgeEdgeService;
        this.knowledgeEmergenceService = knowledgeEmergenceService;
        this.knowledgeMiningProperties = knowledgeMiningProperties;
    }

    @Override
    public void discover(String nodeId) {
        if (!knowledgeMiningProperties.isRelationEnabled()) {
            return;
        }
        KnowledgeNode newNode = knowledgeNodeMapper.selectById(nodeId);
        if (newNode == null || newNode.getContent() == null || newNode.getContent().isBlank()) {
            return;
        }

        List<String> candidateIds = vectorSearchRepository.searchTopK(
                vectorService.getVector(newNode.getContent()),
                knowledgeMiningProperties.resolvedRelationTopK() + 1
        ).stream()
                .filter(id -> !Objects.equals(id, nodeId))
                .distinct()
                .collect(Collectors.toList());
        if (candidateIds.isEmpty()) {
            log.info("relation discovery no candidates: nodeId={}", nodeId);
            return;
        }

        List<KnowledgeNode> candidates = knowledgeNodeMapper.selectList(
                new LambdaQueryWrapper<KnowledgeNode>().in(KnowledgeNode::getId, candidateIds)
        );
        int hitCount = 0;
        int emergenceCount = 0;
        int failureCount = 0;

        for (KnowledgeNode candidate : candidates) {
            try {
                if (candidate == null || candidate.getContent() == null || candidate.getContent().isBlank()) {
                    continue;
                }
                if (Objects.equals(newNode.getSourceTaskId(), candidate.getSourceTaskId())
                        && newNode.getSourceTaskId() != null) {
                    continue;
                }
                String relationType = knowledgeRelationJudge.judgeRelationType(newNode.getContent(), candidate.getContent());
                if (relationType == null) {
                    continue;
                }
                boolean created = knowledgeEdgeService.createIfAbsent(newNode, candidate, relationType);
                if (!created) {
                    continue;
                }
                hitCount++;
                if (knowledgeMiningProperties.isEmergenceEnabled()) {
                    String emergentNodeId = knowledgeEmergenceService.emerge(newNode, candidate, relationType);
                    if (emergentNodeId != null) {
                        emergenceCount++;
                    }
                }
            } catch (Exception ex) {
                failureCount++;
                log.warn("relation discovery candidate failed: newNodeId={}, candidateNodeId={}",
                        nodeId, candidate == null ? null : candidate.getId(), ex);
            }
        }
        log.info("relation discovery done: nodeId={}, candidateCount={}, relationHitCount={}, emergenceCount={}, failureCount={}",
                nodeId, candidates.size(), hitCount, emergenceCount, failureCount);
    }
}
