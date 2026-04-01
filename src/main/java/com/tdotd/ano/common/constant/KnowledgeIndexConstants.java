package com.tdotd.ano.common.constant;

public final class KnowledgeIndexConstants {

    public static final String INDEX_NAME = "idx:knowledge";
    public static final String KEY_PREFIX = "node:";

    public static final String FIELD_ID = "id";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_VECTOR = "vector";

    /**
     * 文档参考；实际以 {@code ano.knowledge.vector-dimension} 与 Embedding 模型为准。
     */
    public static final int VECTOR_DIM_REFERENCE = 1024;
    public static final String VECTOR_DISTANCE_METRIC = "COSINE";
    public static final String VECTOR_ALGORITHM = "HNSW";
    public static final String VECTOR_TYPE = "FLOAT32";
    public static final String TITLE_WEIGHT = "5.0";

    public static final String COMMAND_FT_INFO = "FT.INFO";
    public static final String COMMAND_FT_CREATE = "FT.CREATE";
    public static final String UNKNOWN_INDEX_ERROR_TOKEN = "Unknown Index name";

    private KnowledgeIndexConstants() {
    }
}
