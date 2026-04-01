package com.tdotd.ano.domain.event;

/**
 * 知识节点成功提炼并入库后发布的事件。
 */
public record KnowledgeExtractedEvent(String nodeId) {
}
