package com.tdotd.ano.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 向量维度须与 Embedding 模型输出、RediSearch 索引 {@code DIM} 一致。
 * 百炼 {@code text-embedding-v3} 常见为 1024 维。
 */
@ConfigurationProperties(prefix = "ano.knowledge")
public record KnowledgeVectorProperties(Integer vectorDimension) {

    public int resolvedDimension() {
        if (vectorDimension != null && vectorDimension > 0) {
            return vectorDimension;
        }
        return 1024;
    }
}
