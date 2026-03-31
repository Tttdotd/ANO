package com.tdotd.ano.config;

import com.tdotd.ano.service.KnowledgeIndexBootstrapService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeIndexBootstrapRunner implements ApplicationRunner {

    private final KnowledgeIndexBootstrapService knowledgeIndexBootstrapService;

    public KnowledgeIndexBootstrapRunner(KnowledgeIndexBootstrapService knowledgeIndexBootstrapService) {
        this.knowledgeIndexBootstrapService = knowledgeIndexBootstrapService;
    }

    @Override
    public void run(ApplicationArguments args) {
        knowledgeIndexBootstrapService.initializeKnowledgeIndex();
    }
}
