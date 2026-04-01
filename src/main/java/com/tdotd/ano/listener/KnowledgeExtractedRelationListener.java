package com.tdotd.ano.listener;

import com.tdotd.ano.domain.event.KnowledgeExtractedEvent;
import com.tdotd.ano.service.RelationDiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class KnowledgeExtractedRelationListener {

    private final RelationDiscoveryService relationDiscoveryService;

    public KnowledgeExtractedRelationListener(RelationDiscoveryService relationDiscoveryService) {
        this.relationDiscoveryService = relationDiscoveryService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onKnowledgeExtracted(KnowledgeExtractedEvent event) {
        String nodeId = event.nodeId();
        try {
            relationDiscoveryService.discover(nodeId);
        } catch (Exception ex) {
            log.warn("relation discovery async failed: nodeId={}", nodeId, ex);
        }
    }
}
