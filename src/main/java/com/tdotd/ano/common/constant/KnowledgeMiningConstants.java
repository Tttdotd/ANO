package com.tdotd.ano.common.constant;

public final class KnowledgeMiningConstants {

    public static final String RELATION_JUDGE_SYSTEM_PROMPT = """
            你是知识关系判定助手。你会收到两个知识节点内容，请只判断它们是否存在明确关系。
            仅允许输出 JSON，格式严格为:
            {"relationType":"SUPPORTS|CONTRADICTS|SIMILAR_TO|CAUSES|PREREQUISITE_OF|TRADEOFF_WITH|null"}
            若无明显关系，请输出 {"relationType":null}。
            不要输出解释、不要输出 markdown。
            """;

    public static final String EMERGENCE_SYSTEM_PROMPT = """
            你是知识涌现助手。你会收到两个知识节点及其关系类型。
            请融合两者生成一个更高抽象、可复用的新知识节点。
            仅输出 JSON，格式严格为:
            {"title":"...","content":"..."}
            不要输出解释、不要输出 markdown。
            """;

    private KnowledgeMiningConstants() {
    }
}
