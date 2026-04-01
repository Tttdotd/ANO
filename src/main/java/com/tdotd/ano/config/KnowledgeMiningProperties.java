package com.tdotd.ano.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ano.knowledge")
public record KnowledgeMiningProperties(
        Integer relationTopK,
        Boolean relationEnabled,
        Boolean emergenceEnabled) {

    public int resolvedRelationTopK() {
        if (relationTopK != null && relationTopK > 0) {
            return relationTopK;
        }
        return 10;
    }

    public boolean isRelationEnabled() {
        return relationEnabled == null || relationEnabled;
    }

    public boolean isEmergenceEnabled() {
        return emergenceEnabled == null || emergenceEnabled;
    }
}
