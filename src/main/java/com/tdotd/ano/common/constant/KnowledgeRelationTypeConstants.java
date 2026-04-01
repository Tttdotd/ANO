package com.tdotd.ano.common.constant;

import java.util.Set;

public final class KnowledgeRelationTypeConstants {

    public static final String SUPPORTS = "SUPPORTS";
    public static final String CONTRADICTS = "CONTRADICTS";
    public static final String SIMILAR_TO = "SIMILAR_TO";
    public static final String CAUSES = "CAUSES";
    public static final String PREREQUISITE_OF = "PREREQUISITE_OF";
    public static final String TRADEOFF_WITH = "TRADEOFF_WITH";

    public static final Set<String> ALLOWED = Set.of(
            SUPPORTS,
            CONTRADICTS,
            SIMILAR_TO,
            CAUSES,
            PREREQUISITE_OF,
            TRADEOFF_WITH
    );

    private KnowledgeRelationTypeConstants() {
    }
}
