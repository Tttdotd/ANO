package com.tdotd.ano.infrastructure.ai;

import com.tdotd.ano.common.exception.BusinessException;
import com.tdotd.ano.config.KnowledgeVectorProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VectorService {

    private final EmbeddingModel embeddingModel;
    private final KnowledgeVectorProperties knowledgeVectorProperties;

    public VectorService(EmbeddingModel embeddingModel, KnowledgeVectorProperties knowledgeVectorProperties) {
        this.embeddingModel = embeddingModel;
        this.knowledgeVectorProperties = knowledgeVectorProperties;
    }

    public float[] getVector(String content) {
        long start = System.currentTimeMillis();
        float[] vector = embeddingModel.embed(content == null ? "" : content);
        int expectedDim = knowledgeVectorProperties.resolvedDimension();
        if (vector == null || vector.length != expectedDim) {
            throw new BusinessException("向量维度异常，期望 " + expectedDim + "，实际 "
                    + (vector == null ? "null" : String.valueOf(vector.length)));
        }
        log.info("knowledge vectorized: dim={}, costMs={}", vector.length, System.currentTimeMillis() - start);
        return vector;
    }
}
