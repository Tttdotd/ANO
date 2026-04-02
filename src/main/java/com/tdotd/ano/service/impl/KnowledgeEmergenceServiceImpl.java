package com.tdotd.ano.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tdotd.ano.common.constant.KnowledgeNodeTypeConstants;
import com.tdotd.ano.common.utils.VectorUtils;
import com.tdotd.ano.domain.entity.KnowledgeLineage;
import com.tdotd.ano.domain.entity.KnowledgeNode;
import com.tdotd.ano.infrastructure.ai.KnowledgeEmerger;
import com.tdotd.ano.infrastructure.ai.VectorService;
import com.tdotd.ano.infrastructure.persistence.RedisVectorRepository;
import com.tdotd.ano.mapper.KnowledgeLineageMapper;
import com.tdotd.ano.mapper.KnowledgeNodeMapper;
import com.tdotd.ano.service.KnowledgeEmergenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class KnowledgeEmergenceServiceImpl implements KnowledgeEmergenceService {

    private final KnowledgeEmerger knowledgeEmerger;
    private final VectorService vectorService;
    private final KnowledgeNodeMapper knowledgeNodeMapper;
    private final KnowledgeLineageMapper knowledgeLineageMapper;
    private final RedisVectorRepository redisVectorRepository;

    public KnowledgeEmergenceServiceImpl(
            KnowledgeEmerger knowledgeEmerger,
            VectorService vectorService,
            KnowledgeNodeMapper knowledgeNodeMapper,
            KnowledgeLineageMapper knowledgeLineageMapper,
            RedisVectorRepository redisVectorRepository) {
        this.knowledgeEmerger = knowledgeEmerger;
        this.vectorService = vectorService;
        this.knowledgeNodeMapper = knowledgeNodeMapper;
        this.knowledgeLineageMapper = knowledgeLineageMapper;
        this.redisVectorRepository = redisVectorRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String emerge(KnowledgeNode fromNode, KnowledgeNode toNode, String relationType) {
        String parentAId = fromNode.getId();
        String parentBId = toNode.getId();
        if (parentAId.compareTo(parentBId) > 0) {
            parentAId = toNode.getId();
            parentBId = fromNode.getId();
        }
        KnowledgeLineage existing = knowledgeLineageMapper.selectOne(
                new LambdaQueryWrapper<KnowledgeLineage>()
                        .eq(KnowledgeLineage::getParentAId, parentAId)
                        .eq(KnowledgeLineage::getParentBId, parentBId)
        );
        if (existing != null) {
            return existing.getChildNodeId();
        }

        KnowledgeEmerger.EmergenceResult result = knowledgeEmerger.emerge(
                fromNode.getContent(), toNode.getContent(), relationType
        );
        String content = result.content() == null ? "" : result.content().trim();
        if (content.isBlank()) {
            return null;
        }
        String title = result.title() == null ? "" : result.title().trim();
        if (title.isBlank()) {
            title = "涌现知识-" + relationType;
        }

        KnowledgeNode emergentNode = new KnowledgeNode();
        emergentNode.setSourceTaskId(null);
        emergentNode.setNodeType(KnowledgeNodeTypeConstants.EMERGENT);
        emergentNode.setTitle(title);
        emergentNode.setContent(content);
        emergentNode.setVector(VectorUtils.toBuffer(vectorService.getVector(content)));
        knowledgeNodeMapper.insert(emergentNode);
        redisVectorRepository.save(emergentNode);

        KnowledgeLineage lineage = new KnowledgeLineage();
        lineage.setChildNodeId(emergentNode.getId());
        lineage.setParentAId(parentAId);
        lineage.setParentBId(parentBId);
        knowledgeLineageMapper.insert(lineage);
        log.info("knowledge emerged: emergentNodeId={}, parentAId={}, parentBId={}, relationType={}",
                emergentNode.getId(), parentAId, parentBId, relationType);
        return emergentNode.getId();
    }
}
