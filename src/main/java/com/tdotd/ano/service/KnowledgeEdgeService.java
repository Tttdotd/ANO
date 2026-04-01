package com.tdotd.ano.service;

import com.tdotd.ano.domain.entity.KnowledgeNode;

public interface KnowledgeEdgeService {

    boolean createIfAbsent(KnowledgeNode fromNode, KnowledgeNode toNode, String relationType);
}
