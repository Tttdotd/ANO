package com.tdotd.ano.common.constant;

public final class KnowledgeArchiveConstants {

    public static final String REDIS_KEY_PREFIX = "node:";
    public static final String REDIS_FIELD_ID = "id";
    public static final String REDIS_FIELD_TARGET = "target";
    public static final String REDIS_FIELD_CONTENT = "content";
    public static final String REDIS_FIELD_VECTOR = "vector";

    public static final String CHAT_SYSTEM_PROMPT = """
            你是知识提炼助手。请基于输入的任务标题、任务描述、任务笔记，输出中文 Markdown。
            输出必须包含以下二级标题：
            ## 核心原理
            ## 执行经验
            ## 避坑指南
            每节输出 2-5 条要点，内容务必可执行、可复用，不要输出无关寒暄。
            """;

    private KnowledgeArchiveConstants() {
    }
}
