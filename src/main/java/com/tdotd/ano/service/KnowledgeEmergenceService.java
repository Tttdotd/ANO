package com.tdotd.ano.service;

import com.tdotd.ano.domain.entity.KnowledgeNode;

public interface KnowledgeEmergenceService {

    String emerge(KnowledgeNode fromNode, KnowledgeNode toNode, String relationType);
}
