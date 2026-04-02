package com.tdotd.ano.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdotd.ano.common.constant.KnowledgeMiningConstants;
import com.tdotd.ano.common.constant.KnowledgeRelationTypeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KnowledgeRelationJudge {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public KnowledgeRelationJudge(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String judgeRelationType(String newNodeContent, String candidateNodeContent) {
        String prompt = """
                节点A:
                %s

                节点B:
                %s
                """.formatted(nullSafe(newNodeContent), nullSafe(candidateNodeContent));
        try {
            String raw = chatClient.prompt()
                    .system(KnowledgeMiningConstants.RELATION_JUDGE_SYSTEM_PROMPT)
                    .user(prompt)
                    .call()
                    .content();
            JsonNode root = objectMapper.readTree(raw == null ? "{}" : raw);
            JsonNode relationTypeNode = root.get("relationType");

            log.info("relation judge result: {}", relationTypeNode);

            if (relationTypeNode == null || relationTypeNode.isNull()) {
                return null;
            }
            String relationType = relationTypeNode.asText(null);
            if (relationType == null) {
                return null;
            }
            String normalized = relationType.trim().toUpperCase();
            if (!KnowledgeRelationTypeConstants.ALLOWED.contains(normalized)) {
                return null;
            }
            return normalized;
        } catch (Exception ex) {
            log.warn("relation judge failed, fallback to null relation", ex);
            return null;
        }
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
