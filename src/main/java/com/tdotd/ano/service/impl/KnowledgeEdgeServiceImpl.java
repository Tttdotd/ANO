package com.tdotd.ano.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tdotd.ano.domain.entity.KnowledgeEdge;
import com.tdotd.ano.domain.entity.KnowledgeNode;
import com.tdotd.ano.mapper.KnowledgeEdgeMapper;
import com.tdotd.ano.service.KnowledgeEdgeService;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeEdgeServiceImpl implements KnowledgeEdgeService {

    private final KnowledgeEdgeMapper knowledgeEdgeMapper;

    public KnowledgeEdgeServiceImpl(KnowledgeEdgeMapper knowledgeEdgeMapper) {
        this.knowledgeEdgeMapper = knowledgeEdgeMapper;
    }

    @Override
    public boolean createIfAbsent(KnowledgeNode fromNode, KnowledgeNode toNode, String relationType) {
        KnowledgeEdge existing = knowledgeEdgeMapper.selectOne(
                new LambdaQueryWrapper<KnowledgeEdge>()
                        .eq(KnowledgeEdge::getFromNodeId, fromNode.getId())
                        .eq(KnowledgeEdge::getToNodeId, toNode.getId())
                        .eq(KnowledgeEdge::getRelationType, relationType)
        );
        if (existing != null) {
            return false;
        }
        KnowledgeEdge edge = new KnowledgeEdge();
        edge.setFromNodeId(fromNode.getId());
        edge.setToNodeId(toNode.getId());
        edge.setRelationType(relationType);
        knowledgeEdgeMapper.insert(edge);
        return true;
    }
}
